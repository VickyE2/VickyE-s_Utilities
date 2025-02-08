package org.vicky.utilities;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class QuaternionRotation {

    public Location rotateAroundArrow(Location particleLoc, Location arrowLoc, Vector direction) {
        Vector relativePos = particleLoc.toVector().subtract(arrowLoc.toVector());
        direction = direction.clone().normalize();

        double yaw = Math.toRadians(arrowLoc.getYaw());
        double pitch = Math.toRadians(arrowLoc.getPitch());

        // Convert Euler angles to quaternion
        Quaternion yawQuat = Quaternion.fromAxisAngle(new Vector(0, 1, 0), -yaw);
        Quaternion pitchQuat = Quaternion.fromAxisAngle(new Vector(1, 0, 0), pitch);

        // Apply rotations
        Quaternion rotationQuat = yawQuat.multiply(pitchQuat);
        Vector rotatedPos = rotationQuat.rotateVector(relativePos);

        return arrowLoc.clone().add(rotatedPos);
    }
}

class Quaternion {
    private final double x, y, z, w;

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Quaternion fromAxisAngle(Vector axis, double angle) {
        double halfAngle = angle / 2.0;
        double sinHalf = Math.sin(halfAngle);
        return new Quaternion(axis.getX() * sinHalf, axis.getY() * sinHalf, axis.getZ() * sinHalf, Math.cos(halfAngle));
    }

    public Quaternion multiply(Quaternion q) {
        return new Quaternion(
                w * q.x + x * q.w + y * q.z - z * q.y,
                w * q.y - x * q.z + y * q.w + z * q.x,
                w * q.z + x * q.y - y * q.x + z * q.w,
                w * q.w - x * q.x - y * q.y - z * q.z
        );
    }

    public Vector rotateVector(Vector v) {
        Quaternion vectorQuat = new Quaternion(v.getX(), v.getY(), v.getZ(), 0);
        Quaternion conjugate = new Quaternion(-x, -y, -z, w);
        Quaternion result = this.multiply(vectorQuat).multiply(conjugate);
        return new Vector(result.x, result.y, result.z);
    }
}