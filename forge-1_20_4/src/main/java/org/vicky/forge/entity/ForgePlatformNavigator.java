package org.vicky.forge.entity;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.jetbrains.annotations.Nullable;
import org.vicky.platform.entity.Path;
import org.vicky.platform.entity.PathNavigator;
import org.vicky.platform.entity.PathPoint;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.utils.IntVec3;

public class ForgePlatformNavigator implements PathNavigator {

    private final PathNavigation ordinal;

    private ForgePlatformNavigator(PathNavigation nav) {
        this.ordinal = nav;
    }

    public static ForgePlatformNavigator from(PathNavigation nav) { return new ForgePlatformNavigator(nav); }

    @Override
    public boolean canUpdatePath() {
        return ordinal.;
    }

    @Override
    public void setSpeed(double v) {
        ordinal.setSpeedModifier(v);
    }

    @Override
    public @Nullable Path getPath() {
        return ForgePath.from(ordinal.getPath());
    }

    @Override
    public boolean isDone() {
        return ordinal.isDone();
    }

    @Override
    public void tick() {
        ordinal.tick();
    }

    @Override
    public @Nullable Path createPath(@Nullable IntVec3 intVec3, int i) {
        return null;
    }

    @Override
    public @Nullable Path createPath(@Nullable PlatformEntity platformEntity, int i) {
        return null;
    }

    @Override
    public void moveTo(@Nullable Path path, double v) {
        ordinal.moveTo(path, v);
    }

    @Override
    public void stop() {
        ordinal.stop();
    }

    @Override
    public boolean noPath() {
        return ordinal.isDone();
    }

    @Override
    public @Nullable PathPoint getPathEnd() {
        return ordinal.getTargetPos();
    }

    @Override
    public void setCanFloat(boolean b) {
        ordinal.setCanFloat(b);
    }

    @Override
    public void setCanPassDoors(boolean b) {
        ordinal.
    }

    @Override
    public void setAvoidsWater(boolean b) {
        ordinal.
    }

    static Path forgePath(net.minecraft.world.level.pathfinder.Path p) {
        return new Path(
                p.
        )
    }
}
