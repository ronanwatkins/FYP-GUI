/**
 * This code has been taken from Openintents' Sensor Simulator. All rights are theirs
 */

package application.utilities;

public class ThreeDimensionalVector {
    public double x;
    public double y;
    public double z;

    public ThreeDimensionalVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public ThreeDimensionalVector(ThreeDimensionalVector v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public ThreeDimensionalVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ThreeDimensionalVector(double[] vec) {
        x = vec[0];
        y = vec[1];
        z = vec[2];
    }

    /**
     * Scale the vector by a factor.
     *
     * @param factor Common factor.
     */
    public void scale(double factor) {
        x = factor * x;
        y = factor * y;
        z = factor * z;
    }

    /**
     * Yaw the vector (rotate around z-axis)
     *
     * @param yaw yaw in Degree.
     */
    public void yaw(double yaw) {
        ThreeDimensionalVector v = new ThreeDimensionalVector(this); // temporary vector
        double yawRad = Math.toRadians(yaw);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        x = cos * v.x + sin * v.y;
        y = -sin * v.x + cos * v.y;
        z = v.z;
    }

    public void pitch(double pitch) {
        ThreeDimensionalVector v = new ThreeDimensionalVector(this); // temporary vector
        double pitchRad = Math.toRadians(pitch); // negative sign => positive as
        // defined in SDK.
        double cos = Math.cos(pitchRad);
        double sin = Math.sin(pitchRad);
        x = v.x;
        y = cos * v.y + sin * v.z;
        z = -sin * v.y + cos * v.z;
    }

    public void roll(double roll) {
        ThreeDimensionalVector v = new ThreeDimensionalVector(this); // temporary vector
        double rollRad = Math.toRadians(roll);
        double cos = Math.cos(rollRad);
        double sin = Math.sin(rollRad);
        x = cos * v.x + sin * v.z;
        y = v.y;
        z = -sin * v.x + cos * v.z;
    }

    public void rollpitchyaw(double roll, double pitch, double yaw) {
        roll(roll);
        pitch(pitch);
        yaw(yaw);
    }

    public void reverserollpitchyaw(double roll, double pitch, double yaw) {
        yaw(-yaw);
        pitch(-pitch);
        roll(-roll);
    }

    @Override
    public String toString() {
        return String.format("%.2f", x) + ", "
                + String.format("%.2f", y) + ", "
                + String.format("%.2f", z);
    }

    public static ThreeDimensionalVector addVectors(ThreeDimensionalVector a, ThreeDimensionalVector b) {
        return new ThreeDimensionalVector(a.x + b.x, a.y + b.y, a.z + b.z);
    }
}