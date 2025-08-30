package org.vicky.forge.forgeplatform.forgeplatform;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgePlatformParticle;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformParticleProvider;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.PlatformParticle;
import org.vicky.platform.world.PlatformLocation;

public class ForgeParticleProvider implements PlatformParticleProvider {
    @Override
    public void spawnBasic(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY, double spreadZ, float speed, float size) {
        if (!(type instanceof ForgePlatformParticle)) throw new IllegalArgumentException("Expected ForgePlatformParticle type");
        Level world = ((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld(); // Get the actual Minecraft world
        if (world instanceof ServerLevel serverLevel) {
            ParticleOptions particle = ((ForgePlatformParticle) type).particleOptions(); // Your PlatformParticle maps to a ParticleOptions
            serverLevel.sendParticles(particle,
                    loc.x, loc.y, loc.z,
                    count,
                    spreadX, spreadY, spreadZ,
                    speed);
        }
    }

    @Override
    public void spawnColored(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY, double spreadZ, float speed, IColor color, float size) {
        if (!(type instanceof ForgePlatformParticle)) throw new IllegalArgumentException("Expected ForgePlatformParticle type");
        Level world = ((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld();
        if (world instanceof ServerLevel serverLevel && type.supportsColor()) {
            DustParticleOptions dust = (DustParticleOptions) ((ForgePlatformParticle) type).particleOptions();
            serverLevel.sendParticles(dust,
                    loc.x, loc.y, loc.z,
                    count,
                    spreadX, spreadY, spreadZ,
                    speed);
        }
    }
    
    @Override
    public void spawnTransition(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY, double spreadZ, float speed, IColor from, IColor to, float size) {
        if (!(type instanceof ForgePlatformParticle)) throw new IllegalArgumentException("Expected ForgePlatformParticle type");
        Level world = ((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld();
        if (world instanceof ServerLevel serverLevel && type.supportsTransition()) {
            DustColorTransitionOptions transition = (DustColorTransitionOptions) ((ForgePlatformParticle) type).particleOptions();
            serverLevel.sendParticles(transition,
                    loc.x, loc.y, loc.z,
                    count,
                    spreadX, spreadY, spreadZ,
                    speed);
        }
    }
}
