package org.vicky.platform.utils;

import static java.lang.Math.PI;

public class MutableVec3 implements Cloneable {
    public double x, y, z;

    public MutableVec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static MutableVec3 of(double x, double y, double z) {
        return new MutableVec3(x, y, z);
    }

    public Vec3 immutable() {
        return new Vec3(x, y, z);
    }


    public MutableVec3 round() {
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.z = Math.round(z);
        return this;
    }

    public MutableVec3 withX(double x) {
        this.x = x;
        return this;
    }

    public MutableVec3 withY(double y) {
        this.y = y;
        return this;
    }

    public MutableVec3 withZ(double z) {
        this.z = z;
        return this;
    }

    public MutableVec3 subtract(MutableVec3 other) {
        this.x = x - other.x;
        this.y = y - other.y;
        this.z = z - other.z;
        return this;
    }

    public MutableVec3 subtract(Double x, Double y, Double z) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.z = this.z - z;
        return this;
    }

    public MutableVec3 subtract(Integer x, Integer y, Integer z) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.z = this.z - z;
        return this;
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

    public MutableVec3 lerp(MutableVec3 b, double t) {
        double oneMinusT = 1.0 - t;
        double x = this.x * oneMinusT + b.x * t;
        double y = this.y * oneMinusT + b.y * t;
        double z = this.z * oneMinusT + b.z * t;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public String toParserString() {
        return this.x + "," + this.y + "," + this.z;
    }

    public MutableVec3 getMinimum(MutableVec3 v2) {
        return new MutableVec3(Math.min(this.x, v2.x), Math.min(this.y, v2.y), Math.min(this.z, v2.z));
    }

    public MutableVec3 getMaximum(MutableVec3 v2) {
        return new MutableVec3(Math.max(this.x, v2.x), Math.max(this.y, v2.y), Math.max(this.z, v2.z));
    }

    public MutableVec3 subtract(MutableVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        int var9 = others.length;

        for (MutableVec3 other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }
        this.x = newX;
        this.y = newY;
        this.z = newZ;

        return this;
    }

    public MutableVec3 multiply(MutableVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (MutableVec3 other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }
        this.x = newX;
        this.y = newY;
        this.z = newZ;

        return this;
    }

    public MutableVec3 divide(MutableVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (MutableVec3 other : others) {
            newX /= other.x;
            newY /= other.y;
            newZ /= other.z;
        }

        this.x = newX;
        this.y = newY;
        this.z = newZ;

        return this;
    }

    public double length() {
        return Math.sqrt(this.lengthSq());
    }

    public double distance(MutableVec3 other) {
        return Math.sqrt(this.distanceSq(other));
    }

    public double distanceSq(MutableVec3 other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean containedWithin(MutableVec3 min, MutableVec3 max) {
        return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
    }

    public MutableVec3 add(MutableVec3 other) {
        this.x = this.x + other.x;
        this.y = this.y + other.y;
        this.z = this.z + other.z;
        return this;
    }

    public MutableVec3 add(Double x, Double y, Double z) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        return this;
    }

    public MutableVec3 add(Integer x, Integer y, Integer z) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        return this;
    }

    public MutableVec3 add(MutableVec3... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;

        for (MutableVec3 other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        this.x = newX;
        this.y = newY;
        this.z = newZ;

        return this;
    }

    public MutableVec3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new MutableVec3(0, 0, 0);
        this.x = x / length;
        this.y = this.y / length;
        this.z = this.z / length;
        return this;
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
    public MutableVec3 clone() {
        return new MutableVec3(x, y, z);
    }

    public MutableVec3 multiply(double scalar) {
        this.x = this.x * scalar;
        this.y = this.y * scalar;
        this.z = this.z * scalar;
        return this;
    }

    public MutableVec3 multiply(MutableVec3 other) {
        this.x = this.x * other.x;
        this.y = this.y * other.y;
        this.z = this.z * other.z;
        return this;
    }

    public MutableVec3 multiply(double x, double y, double z) {
        this.x = this.x * x;
        this.y = this.y * y;
        this.z = this.z * z;
        return this;
    }

    public MutableVec3 divide(double scalar) {
        this.x = this.x / scalar;
        this.y = this.y / scalar;
        this.z = this.z / scalar;
        return this;
    }

    public MutableVec3 divide(MutableVec3 other) {
        this.x = this.x / other.x;
        this.y = this.y / other.y;
        this.z = this.z / other.z;
        return this;
    }

    public MutableVec3 divide(double x, double y, double z) {
        this.x = this.x / x;
        this.y = this.y / y;
        this.z = this.z / z;
        return this;
    }

    public MutableVec3 crossProduct(MutableVec3 v) {
        this.x = y * v.z - z * v.y;
        this.y = z * v.x - x * v.z;
        this.z = x * v.y - y * v.x;
        return this;
    }

    public double dot(MutableVec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != MutableVec3.class) return false;
        MutableVec3 vector = (MutableVec3) obj;
        return vector.x == this.x && vector.y == this.y && vector.z == this.z;
    }
}