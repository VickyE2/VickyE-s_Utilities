package org.vicky.platform.utils;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import static java.lang.Math.PI;

public class Vec3 implements Vector3, Cloneable, Serializable {
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

    public Vec3 subtract(Vector3 other) {
        return new Vec3(x - other.getX(), y - other.getY(), z - other.getZ());
    }

    public double angleTo(Vector3 other) {
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

    public Vec3 lerp(Vector3 b, double t) {
        double oneMinusT = 1.0 - t;
        double x = this.x * oneMinusT + b.getX() * t;
        double y = this.y * oneMinusT + b.getY() * t;
        double z = this.z * oneMinusT + b.getZ() * t;
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

    public Vec3 getMinimum(Vector3 v2) {
        return new Vec3(Math.min(this.x, v2.getX()), Math.min(this.y, v2.getY()), Math.min(this.z, v2.getZ()));
    }

    public Vec3 getMaximum(Vector3 v2) {
        return new Vec3(Math.max(this.x, v2.getX()), Math.max(this.y, v2.getY()), Math.max(this.z, v2.getZ()));
    }

    public Vec3 subtract(Vector3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        int var9 = others.length;

        for (Vector3 other : others) {
            newX -= other.getX();
            newY -= other.getY();
            newZ -= other.getZ();
        }

        return of(newX, newY, newZ);
    }

    public Vec3 multiply(Vector3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vector3 other : others) {
            newX *= other.getX();
            newY *= other.getY();
            newZ *= other.getZ();
        }

        return of(newX, newY, newZ);
    }

    public Vec3 divide(Vec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vector3 other : others) {
            newX /= other.getX();
            newY /= other.getY();
            newZ /= other.getZ();
        }

        return of(newX, newY, newZ);
    }

    public double length() {
        return Math.sqrt(this.lengthSq());
    }

    public double distance(Vector3 other) {
        return Math.sqrt(this.distanceSq(other));
    }

    public double distanceSq(Vector3 other) {
        double dx = other.getX() - this.x;
        double dy = other.getY() - this.y;
        double dz = other.getZ() - this.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean containedWithin(Vector3 min, Vector3 max) {
        return this.x >= min.getX() && this.x <= max.getX() &&
                this.y >= min.getY() && this.y <= max.getY() &&
                this.z >= min.getZ() && this.z <= max.getZ();
    }

    public Vec3 add(Vector3 other) {
        return new Vec3(x + other.getX(), y + other.getY(), z + other.getZ());
    }

    public Vec3 add(Double x, Double y, Double z) {
        return new Vec3(x + this.x, y + this.y, z + this.z);
    }

    public Vec3 add(Integer x, Integer y, Integer z) {
        return new Vec3(x + this.x, y + this.y, z + this.z);
    }

    public Vec3 add(Vector3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (Vector3 other : others) {
            newX += other.getX();
            newY += other.getY();
            newZ += other.getZ();
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

    public Vec3 multiply(Vector3 other) {
        return this.multiply(other.getX(), other.getY(), other.getZ());
    }

    public Vec3 multiply(double x, double y, double z) {
        return of(this.x * x, this.y * y, this.z * z);
    }

    public Vec3 divide(double scalar) {
        return new Vec3(x / scalar, y / scalar, z / scalar);
    }

    public Vec3 divide(Vector3 other) {
        return this.divide(other.getX(), other.getY(), other.getZ());
    }

    public Vec3 divide(double x, double y, double z) {
        return of(this.x / x, this.y / y, this.z / z);
    }

    public Vec3 crossProduct(Vector3 v) {
        return new Vec3(
                y * v.getX() - z * v.getY(),
                z * v.getX() - x * v.getZ(),
                x * v.getY() - y * v.getX()
        );
    }

    public Vec3 getOrtho() {
        // Find the coordinate axis the vector is LEAST aligned with
        Vec3 abs = new Vec3(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));

        // Cross product with that axis to get a guaranteed perpendicular vector
        if (abs.x < abs.y && abs.x < abs.z) return this.crossProduct(new Vec3(1, 0, 0)).normalize();
        if (abs.y < abs.z) return this.crossProduct(new Vec3(0, 1, 0)).normalize();
        return this.crossProduct(new Vec3(0, 0, 1)).normalize();
    }

    public double dot(Vector3 v) {
        return x * v.getX() + y * v.getY() + z * v.getZ();
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    public Vec3 abs() {
        return Vec3.of(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Vec3.class) return false;
        Vec3 vector = (Vec3) obj;
        return vector.x == this.x && vector.y == this.y && vector.z == this.z;
    }
}