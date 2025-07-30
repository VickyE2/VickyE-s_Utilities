package org.vicky.forgeplatform.useables;

import net.minecraft.world.level.Level;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformWorld;

public class ForgeVec3 extends PlatformLocation {
    private final Level world;
    private final PlatformWorld platworld;

    public ForgeVec3(Level level, double x, double y, double z) {
        super(new ForgePlatformWorldAdapter(level), x, y, z);
        this.world = level;
        this.platworld = new ForgePlatformWorldAdapter(level);
    }

    public PlatformWorld getWorld() {
        return platworld;
    }

    public Level getForgeWorld() {
        return world;
    }
}
