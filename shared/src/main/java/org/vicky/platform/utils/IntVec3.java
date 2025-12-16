package org.vicky.platform.utils;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import static java.lang.Math.PI;

public class IntVec3 implements Cloneable, Serializable {
    public final int x, y, z;

    public IntVec3(int x, int y, int z) {
        this.x = x; this.y = y; this.z = z;
    }

    public IntVec3(double x, double y, double z) {
        this.x = (int) Math.round(x); this.y = (int) Math.round(y); this.z = (int) Math.round(z);
    }

    public static IntVec3 of(int x, int y, int z) {
        return new IntVec3(x, y, z);
    }

    public static IntVec3 randomUnit(RandomGenerator rnd) {
        double theta = rnd.nextDouble() * 2 * Math.PI; // azimuth
        double z = rnd.nextDouble() * 2 - 1;           // height (−1 to 1)
        double r = Math.sqrt(1 - z * z);
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new IntVec3((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
    }

    /**
     * Creates a 3D direction vector from yaw and pitch angles.
     * <p>
     * Yaw and pitch follow the standard right-handed coordinate convention:
     * <ul>
     *     <li><b>Yaw</b> (rotation around the Y-axis):
     *         <br>Defines the horizontal rotation — 0° points along +Z,
     *         90° points along -X, 180° along -Z, and so on (like a compass heading).</li>
     *     <li><b>Pitch</b> (rotation around the X-axis):
     *         <br>Defines the vertical rotation — 0° is level (horizontal),
     *         positive pitch angles look upward, negative pitch angles look downward.</li>
     * </ul>
     *
     * @param yawDegrees   the horizontal rotation in degrees
     * @param pitchDegrees the vertical rotation in degrees
     * @return a normalized {@link IntVec3} representing the direction
     */
    public static IntVec3 fromYawPitch(float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new IntVec3((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)).normalize();
    }

    public IntVec3 randomPerturbated(RandomGenerator rnd, double strength) {
        IntVec3 rand = IntVec3.randomUnit(rnd);

        return this.add(rand.multiply(strength)).normalize();
    }

    public MutableVec3 mutable() {
        return new MutableVec3(x, y, z);
    }

    public IntVec3 withX(int x) {
        return of(x, this.y, this.z);
    }

    public IntVec3 withY(int y) {
        return of(this.x, y, this.z);
    }

    public IntVec3 withZ(int z) {
        return of(this.x, this.y, z);
    }

    public IntVec3 subtract(IntVec3 other) {
        return new IntVec3(x - other.x, y - other.y, z - other.z);
    }

    public double angleTo(IntVec3 other) {
        double dot = this.dot(other);
        double magA = this.length();
        double magB = other.length();

        if (magA == 0 || magB == 0) return 0;
        double cosTheta = dot / (magA * magB);

        // Clamp to handle floating-point precision errors
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));
        return Math.acos(cosTheta);
    }

    public IntVec3 subtract(Integer x, Integer y, Integer z) {
        return new IntVec3(this.x - x, this.y - y, this.z - z);
    }

    public double toPitch() {
        double x = this.getX();
        double z = this.getZ();
        if (x == 0.0 && z == 0.0) {
            return this.getY() > 0.0 ? -90.0 : 90.0;
        } else {
            double x2 = x * x;
            double z2 = z * z;
            double xz = Math.sqrt(x2 + z2);
            return Math.toDegrees(Math.atan(-this.getY() / xz));
        }
    }

    public double toYaw() {
        double x = this.getX();
        double z = this.getZ();
        double t = Math.atan2(-x, z);
        double tau = 2.0 * PI;
        return Math.toDegrees((t + tau) % tau);
    }

    public IntVec3 lerp(IntVec3 b, double t) {
        double oneMinusT = 1.0 - t;
        double x = this.x * oneMinusT + b.x * t;
        double y = this.y * oneMinusT + b.y * t;
        double z = this.z * oneMinusT + b.z * t;
        return new IntVec3((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public String toParserString() {
        return this.x + "," + this.y + "," + this.z;
    }

    public String toJsonString() {
        return "[" + this.x + "," + this.y + "," + this.z + "]";
    }

    public IntVec3 getMinimum(IntVec3 v2) {
        return new IntVec3(Math.min(this.x, v2.x), Math.min(this.y, v2.y), Math.min(this.z, v2.z));
    }

    public IntVec3 getMaximum(IntVec3 v2) {
        return new IntVec3(Math.max(this.x, v2.x), Math.max(this.y, v2.y), Math.max(this.z, v2.z));
    }

    public IntVec3 subtract(IntVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        int var9 = others.length;

        for (IntVec3 other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return of((int) Math.round(newX), (int) Math.round(newY), (int) Math.round(newZ));
    }

    public IntVec3 multiply(IntVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (IntVec3 other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return of((int) Math.round(newX), (int) Math.round(newY), (int) Math.round(newZ));
    }

    public IntVec3 divide(IntVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (IntVec3 other : others) {
            newX /= other.x;
            newY /= other.y;
            newZ /= other.z;
        }

        return of((int) Math.round(newX), (int) Math.round(newY), (int) Math.round(newZ));
    }

    public double length() {
        return Math.sqrt(this.lengthSq());
    }

    public double distance(IntVec3 other) {
        return Math.sqrt(this.distanceSq(other));
    }

    public double distanceSq(IntVec3 other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean containedWithin(IntVec3 min, IntVec3 max) {
        return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
    }

    public IntVec3 add(IntVec3 other) {
        return new IntVec3(x + other.x, y + other.y, z + other.z);
    }

    public IntVec3 add(Double x, Double y, Double z) {
        return new IntVec3((int) Math.round(x + this.x), (int) Math.round(y + this.y), (int) Math.round(z + this.z));
    }

    public IntVec3 add(Integer x, Integer y, Integer z) {
        return new IntVec3(x + this.x, y + this.y, z + this.z);
    }

    public IntVec3 add(IntVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (IntVec3 other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return of((int) Math.round(newX), (int) Math.round(newY), (int) Math.round(newZ));
    }
    
    public IntVec3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new IntVec3(0, 0, 0);
        return new IntVec3((int) Math.round(x / length), (int) Math.round(y / length), (int) Math.round(z / length));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public IntVec3 clone() {
        return new IntVec3(x, y, z);
    }

    public IntVec3 multiply(double scalar) {
        return new IntVec3((int) Math.round(x * scalar), (int) Math.round(y * scalar), (int) Math.round(z * scalar));
    }

    public IntVec3 multiply(IntVec3 other) {
        return this.multiply(other.x, other.y, other.z);
    }

    public IntVec3 multiply(double x, double y, double z) {
        return of((int) Math.round(this.x * x), (int) Math.round(this.y * y), (int) Math.round(this.z * z));
    }

    public IntVec3 divide(double scalar) {
        return new IntVec3((int) Math.round(x / scalar), (int) Math.round(y / scalar), (int) Math.round(z / scalar));
    }

    public IntVec3 divide(IntVec3 other) {
        return this.divide(other.x, other.y, other.z);
    }

    public IntVec3 divide(double x, double y, double z) {
        return of((int) Math.round(this.x / x),(int) Math.round(this.y / y), (int) Math.round(this.z / z));
    }

    public IntVec3 crossProduct(IntVec3 v) {
        return new IntVec3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }

    public double dot(IntVec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != IntVec3.class) return false;
        IntVec3 vector = (IntVec3) obj;
        return vector.x == this.x && vector.y == this.y && vector.z == this.z;
    }
}