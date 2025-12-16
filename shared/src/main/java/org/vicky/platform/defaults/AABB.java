package org.vicky.platform.defaults;
import org.vicky.platform.utils.Vec3;

import java.util.Objects;

public class AABB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (minX > maxX) throw new IllegalArgumentException("minX > maxX");
        if (minY > maxY) throw new IllegalArgumentException("minY > maxY");
        if (minZ > maxZ) throw new IllegalArgumentException("minZ > maxZ");
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AABB(double maxX, double maxY, double maxZ) {
        if (maxX < 0.0) throw new IllegalArgumentException("minX > maxX");
        if (maxY < 0.0) throw new IllegalArgumentException("minY > maxY");
        if (maxZ < 0.0) throw new IllegalArgumentException("minZ > maxZ");
        this.minX = 0.0;
        this.minY = 0.0;
        this.minZ = 0.0;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getDepth() {
        return maxZ - minZ;
    }

    public Vec3 getCenter() {
        return new Vec3(
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0
        );
    }

    /** Expand in all directions equally */
    public AABB inflate(double dx) {
        return inflate(dx, dx, dx);
    }

    public AABB inflate(double dx, double dy, double dz) {
        return new AABB(
                minX - dx, minY - dy, minZ - dz,
                maxX + dx, maxY + dy, maxZ + dz
        );
    }

    /** Expand only positive direction (max) */
    public AABB expand(double dx, double dy, double dz) {
        return new AABB(
                minX, minY, minZ,
                maxX + dx, maxY + dy, maxZ + dz
        );
    }

    /** Move the entire box */
    public AABB move(double dx, double dy, double dz) {
        return new AABB(
                minX + dx, minY + dy, minZ + dz,
                maxX + dx, maxY + dy, maxZ + dz
        );
    }

    /** Returns true when this box intersects (touches or overlaps) another */
    public boolean intersects(AABB other) {
        return maxX >= other.minX && minX <= other.maxX &&
                maxY >= other.minY && minY <= other.maxY &&
                maxZ >= other.minZ && minZ <= other.maxZ;
    }

    /** Returns true if a point is inside (including edges) */
    public boolean contains(Vec3 point) {
        return point.x >= minX && point.x <= maxX &&
                point.y >= minY && point.y <= maxY &&
                point.z >= minZ && point.z <= maxZ;
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    /** Clip a ray to the AABB – returns distance or null */
    public Double clipRay(Vec3 origin, Vec3 dir) {
        final double[] tMin = {Double.NEGATIVE_INFINITY};
        final double[] tMax = {Double.POSITIVE_INFINITY};

        // Helper function as a local method replacement
        class Helper {
            void update(double min, double max, double o, double d) {
                if (d == 0.0) {
                    // Ray parallel; does it start outside?
                    if (o < min || o > max) {
                        // Set tMin > tMax to indicate no intersection
                        tMin[0] = 1.0;
                        tMax[0] = 0.0;
                    }
                    return;
                }
                double t1 = (min - o) / d;
                double t2 = (max - o) / d;

                double tNear = Math.min(t1, t2);
                double tFar = Math.max(t1, t2);

                tMin[0] = Math.max(tMin[0], tNear);
                tMax[0] = Math.min(tMax[0], tFar);
            }
        }

        Helper helper = new Helper();
        helper.update(minX, maxX, origin.x, dir.x);
        helper.update(minY, maxY, origin.y, dir.y);
        helper.update(minZ, maxZ, origin.z, dir.z);

        if (tMax[0] < tMin[0] || tMax[0] < 0.0) return null;

        return Math.max(tMin[0], 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AABB)) return false;
        AABB aabb = (AABB) o;
        return Double.compare(aabb.minX, minX) == 0 &&
                Double.compare(aabb.minY, minY) == 0 &&
                Double.compare(aabb.minZ, minZ) == 0 &&
                Double.compare(aabb.maxX, maxX) == 0 &&
                Double.compare(aabb.maxY, maxY) == 0 &&
                Double.compare(aabb.maxZ, maxZ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return "AABB{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", minZ=" + minZ +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", maxZ=" + maxZ +
                '}';
    }

    public static AABB ofCenter(Vec3 center, double halfX, double halfY, double halfZ) {
        return new AABB(
                center.x - halfX, center.y - halfY, center.z - halfZ,
                center.x + halfX, center.y + halfY, center.z + halfZ
        );
    }

    public static AABB of(Vec3 pos1, Vec3 pos2) {
        return new AABB(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z),
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z)
        );
    }
}
