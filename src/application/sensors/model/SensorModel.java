/**
 * This code has been taken from Openintents' Sensor Simulator. All rights are theirs
 */

package application.sensors.model;

public abstract class SensorModel {

    /** Whether the sensor is enable or not. */
    protected boolean mEnabled;

    // Simulation update
    protected int mDefaultUpdateDelay;
    protected int mCurrentUpdateDelay;

    /** for measuring updates: */
    protected int mUpdateEmulatorCount;
    protected long mUpdateEmulatorTime;

    /**
     * Duration (in milliseconds) between two updates. This is the inverse of
     * the update rate.
     */
    protected long mUpdateDuration;
    /**
     * Whether to form the average over the last duration when reading out
     * sensors. Alternative is to just take the current value.
     */
    protected boolean mAverage;

    /**
     * Time of next update required. The time is compared to
     * System.currentTimeMillis().
     */
    protected long mNextUpdate;

    public SensorModel() {
        mEnabled = false;

        mUpdateEmulatorCount = 0;
        mUpdateEmulatorTime = System.currentTimeMillis();
        setUpdateRates();
    }

    /**
     * Sets the next values for the sensor (if the time for next update was
     * reached), by making the average or keeping the current value.
     */
    public abstract void updateSensorReadoutValues();

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enable) {
        mEnabled = enable;
    }

    public void setUpdateDuration(long value) {
        mUpdateDuration = value;
    }

    public void setUpdateRates() {
        mDefaultUpdateDelay = 200;
        mCurrentUpdateDelay = 200;
    }
}
