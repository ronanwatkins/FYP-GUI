/**
 * This code has been taken from Openintents' Sensor Simulator. All rights are theirs
 */

package application.sensors.model;

import application.utilities.ThreeDimensionalVector;

public class AccelerometerModel extends SensorModel {

    /**
     * Current read-out value of accelerometer x-component.
     *
     * This value is updated only at the desired updateSensorRate().
     */
    private double mReadAccelx;
    /** Current read-out value of accelerometer y-component. */
    private double mReadAccely;
    /** Current read-out value of accelerometer z-component. */
    private double mReadAccelz;

    /**
     * Internal state value of accelerometer x-component.
     *
     * This value is updated regularly by updateSensorPhysics().
     */
    private double mAccelX;
    /** Internal state value of accelerometer x-component. */
    private double mAccelY;
    /** Internal state value of accelerometer x-component. */
    private double mAccelZ;

    private double aX; // acceleration
    private double aZ;

    private double mAccX; // accelerometer position x on screen
    private double mAccZ; // (DONT confuse with acceleration a!)

    /**
     * Partial read-out value of accelerometer x-component.
     *
     * This partial value is used to calculate the sensor average.
     */
    private double mPartialAccelX;
    /** Partial read-out value of accelerometer y-component. */
    private double mPartialAccelY;
    /** Partial read-out value of accelerometer z-component. */
    private double mPartialAccelZ;

    /** Number of summands in partial sum for accelerometer. */
    private int mPartialAccelN;

    /** Current position on screen. */
    private int mMoveX;
    /** Current position on screen. */
    private int mMoveZ;

    private double mVX; // velocity
    private double mVZ;

    /**
     * Mass of accelerometer test particle.
     *
     * This is set to 1, as only the ratio k/m enters the simulation.
     */
    private double mMass;

    public AccelerometerModel() {
        mAccX = 0;
        mAccZ = 0;

        mMoveX = 0;
        mMoveZ = 0;

        mMass = 1; // mass
    }

    public void setXYZ(ThreeDimensionalVector vec) {
        mAccelX = vec.x;
        mAccelY = vec.y;
        mAccelZ = vec.z;
    }

    public int getMoveX() {
        return mMoveX;
    }

    public int getMoveZ() {
        return mMoveZ;
    }

    public void setMoveX(int newmovex) {
        mMoveX = newmovex;
    }

    public void setMoveZ(int newmovez) {
        mMoveZ = newmovez;
    }

    public void limitate(double limit) {
        if (mAccelX > limit) {
            mAccelX = limit;
        }
        if (mAccelX < -limit) {
            mAccelX = -limit;
        }
        if (mAccelY > limit) {
            mAccelY = limit;
        }
        if (mAccelY < -limit) {
            mAccelY = -limit;
        }
        if (mAccelZ > limit) {
            mAccelZ = limit;
        }
        if (mAccelZ < -limit) {
            mAccelZ = -limit;
        }
    }

    @Override
    public void updateSensorReadoutValues() {
        long currentTime = System.currentTimeMillis();
        // Form the average
        if (mAverage) {
            mPartialAccelX += mAccelX;
            mPartialAccelY += mAccelY;
            mPartialAccelZ += mAccelZ;
            mPartialAccelN++;
        }

        // Update
        if (currentTime >= mNextUpdate) {
            mNextUpdate += mUpdateDuration;
            if (mNextUpdate < currentTime) {
                // Don't lag too much behind.
                // If we are too slow, then we are too slow.
                mNextUpdate = currentTime;
            }

            if (mAverage) {
                // form average
                computeAvg();

                // reset average
                resetAvg();
            } else {
                // Only take current value
                mReadAccelx = mAccelX;
                mReadAccely = mAccelY;
                mReadAccelz = mAccelZ;
            }
        }
    }

    private void resetAvg() {
        mPartialAccelX = 0;
        mPartialAccelY = 0;
        mPartialAccelZ = 0;
        mPartialAccelN = 0;
    }

    private void computeAvg() {
        mReadAccelx = mPartialAccelX / mPartialAccelN;
        mReadAccely = mPartialAccelY / mPartialAccelN;
        mReadAccelz = mPartialAccelZ / mPartialAccelN;
    }

    public double getReadAccelerometerX() {
        return -mReadAccelx;
    }

    public double getReadAccelerometerY() {
        return mReadAccely;
    }

    public double getReadAccelerometerZ() {
        return mReadAccelz;
    }

    public void refreshAcceleration(double kView, double gammaView, double dt) {
        // First calculate the force acting on the
        // sensor test particle, assuming that
        // the accelerometer is mounted by a string:
        // F = - k * x
        double Fx = kView * (mMoveX - mAccX);
        double Fz = gammaView * (mMoveZ - mAccZ);

        // a = F / m
        aX = Fx / mMass;
        aZ = Fz / mMass;

        mVX += aX * dt;
        mVZ += aZ * dt;

        // Now this is the force that tries to adjust
        // the accelerometer back
        // integrate dx/dt = v;
        mAccX += mVX * dt;
        mAccZ += mVZ * dt;

        // We put damping here: We don't want to damp for
        // zero motion with respect to the background,
        // but with respect to the mobile phone:
        mAccX += gammaView * (mMoveX - mAccX) * dt;
        mAccZ += gammaView * (mMoveZ - mAccZ) * dt;
    }

    public double getAx() {
        return aX;
    }

    public double getAz() {
        return aZ;
    }
}
