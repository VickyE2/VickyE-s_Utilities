package org.vicky.platform.utils;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import static java.lang.Math.PI;

public class Vec3 implements Cloneable, Serializable {
    public final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public static Vec3 of(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vec3 randomUnit(RandomGenerator rnd) {
        double theta = rnd.nextDouble() * 2 * Math.PI; // azimuth
        double z = rnd.nextDouble() * 2 - 1;           // height (−1 to 1)
        double r = Math.sqrt(1 - z * z);
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new Vec3(x, y, z);
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
     * @return a normalized {@link Vec3} representing the direction
     */
    public static Vec3 fromYawPitch(float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vec3(x, y, z).normalize();
    }

    public Vec3 randomPerturbated(RandomGenerator rnd, double strength) {
        Vec3 rand = Vec3.randomUnit(rnd);

        return this.add(rand.multiply(strength)).normalize();
    }

    public MutableVec3 mutable() {
        return new MutableVec3(x, y, z);
    }

    public Vec3 withX(double x) {
        return of(x, this.y, this.z);
    }

    public Vec3 withY(double y) {
        return of(this.x, y, this.z);
    }

    public Vec3 withZ(double z) {
        return of(this.x, this.y, z);
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    public double angleTo(Vec3 other) {
        double dot = this.dot(other);
        double magA = this.length();
        double magB = other.length();

        if (magA == 0 || magB == 0) return 0;
        double cosTheta = dot / (magA * magB);

        // Clamp to handle floating-point precision errors
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));
        return Math.acos(cosTheta);
    }

    public Vec3 subtract(Double x, Double y, Double z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    public Vec3 subtract(Integer x, Integer y, Integer z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
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

    public Vec3 lerp(Vec3 b, double t) {
        double oneMinusT = 1.0 - t;
        double x = this.x * oneMinusT + b.x * t;
        double y = this.y * oneMinusT + b.y * t;
        double z = this.z * oneMinusT + b.z * t;
        return new Vec3(x, y, z);
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

    public Vec3 getMinimum(Vec3 v2) {
        return new Vec3(Math.min(this.x, v2.x), Math.min(this.y, v2.y), Math.min(this.z, v2.z));
    }

    public Vec3 getMaximum(Vec3 v2) {
        return new Vec3(Math.max(this.x, v2.x), Math.max(this.y, v2.y), Math.max(this.z, v2.z));
    }

    public Vec3 subtract(Vec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        int var9 = others.length;

        for (Vec3 other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return of(newX, newY, newZ);
    }

    public Vec3 multiply(Vec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vec3 other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return of(newX, newY, newZ);
    }

    public Vec3 divide(Vec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vec3 other : others) {
            newX /= other.x;
            newY /= other.y;
            newZ /= other.z;
        }

        return of(newX, newY, newZ);
    }

    public double length() {
        return Math.sqrt(this.lengthSq());
    }

    public double distance(Vec3 other) {
        return Math.sqrt(this.distanceSq(other));
    }

    public double distanceSq(Vec3 other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean containedWithin(Vec3 min, Vec3 max) {
        return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
    }

    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 add(Double x, Double y, Double z) {
        return new Vec3(x + this.x, y + this.y, z + this.z);
    }

    public Vec3 add(Integer x, Integer y, Integer z) {
        return new Vec3(x + this.x, y + this.y, z + this.z);
    }

    public Vec3 add(Vec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vec3 other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return of(newX, newY, newZ);
    }
    
    public Vec3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new Vec3(0, 0, 0);
        return new Vec3(x / length, y / length, z / length);
    }

    public Vec3 round() {
        return new Vec3(Math.round(x), Math.round(y), Math.round(z));
    }

    public int getIntX() {
        return (int) x;
    }

    public int getIntY() {
        return (int) y;
    }

    public int getIntZ() {
        return (int) z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public Vec3 clone() {
        return new Vec3(x, y, z);
    }

    public Vec3 multiply(double scalar) {
        return new Vec3(x * scalar, y * scalar, z * scalar);
    }

    public Vec3 multiply(Vec3 other) {
        return this.multiply(other.x, other.y, other.z);
    }

    public Vec3 multiply(double x, double y, double z) {
        return of(this.x * x, this.y * y, this.z * z);
    }

    public Vec3 divide(double scalar) {
        return new Vec3(x / scalar, y / scalar, z / scalar);
    }

    public Vec3 divide(Vec3 other) {
        return this.divide(other.x, other.y, other.z);
    }

    public Vec3 divide(double x, double y, double z) {
        return of(this.x / x, this.y / y, this.z / z);
    }

    public Vec3 crossProduct(Vec3 v) {
        return new Vec3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }

    public double dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Vec3.class) return false;
        Vec3 vector = (Vec3) obj;
        return vector.x == this.x && vector.y == this.y && vector.z == this.z;
    }
}