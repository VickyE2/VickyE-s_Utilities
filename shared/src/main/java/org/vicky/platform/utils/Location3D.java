package org.vicky.platform.utils;

public class Location3D extends Vec3 {
    public float yaw, pitch;

    public Location3D(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location3D clone() {
        return new Location3D(x, y, z, yaw, pitch);
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }
}