package org.vicky.platform;

import org.vicky.platform.utils.Location3D;

public interface ParticleProvider {
    void spawnParticle(String particleId, double x, double y, double z, IColor color);
    void spawnDustTransition(Location3D pos, IColor from, IColor to, float size, float spreadX, float spreadY, float spreadZ, float speed);
}