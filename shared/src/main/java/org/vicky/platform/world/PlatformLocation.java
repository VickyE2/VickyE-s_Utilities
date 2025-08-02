package org.vicky.platform.world;

import org.vicky.platform.utils.Location3D;
import org.vicky.platform.utils.Vec3;

public class PlatformLocation extends Location3D implements Cloneable {
    private final PlatformWorld world;

    public PlatformLocation(PlatformWorld world, double x, double y, double z) {
        super(x, y, z);
        this.world = world;
    }

    public PlatformWorld getWorld() { return world; }

    public PlatformLocation add(double dx, double dy, double dz) {
        return new PlatformLocation(world, x + dx, y + dy, z + dz);
    }

    public PlatformLocation add(Vec3 loc) {
        return new PlatformLocation(world, x + loc.x, y + loc.y, z + loc.z);
    }

    public PlatformLocation subtract(double dx, double dy, double dz) {
        return new PlatformLocation(world, x - dx, y - dy, z - dz);
    }

    public PlatformLocation subtract(Vec3 loc) {
        return new PlatformLocation(world, x - loc.x, y - loc.y, z - loc.z);
    }

    public Location3D toLocation3D() {
        return new Location3D(x, y, z);
    }

    public PlatformLocation getRelative(int x, int y, int z) {
        return new PlatformLocation(this.world, this.x - x, this.y - y, this.z - z);
    }

    public PlatformBlock getBlock() {
        return this.world.getBlockAt(x, y, z);
    }

    @Override
    public PlatformLocation clone() {
        return (PlatformLocation) super.clone();
    }
}