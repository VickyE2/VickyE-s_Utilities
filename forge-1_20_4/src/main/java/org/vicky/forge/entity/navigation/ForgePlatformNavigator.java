package org.vicky.forge.entity.navigation;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.ForgePlatformEntity;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.AbstractPath;
import org.vicky.platform.entity.PathNavigator;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.utils.IntVec3;

import java.util.Set;
import java.util.stream.Collectors;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;

public class ForgePlatformNavigator implements PathNavigator {

    private final PathNavigation ordinal;

    private ForgePlatformNavigator(PathNavigation nav) {
        this.ordinal = nav;
    }

    public static ForgePlatformNavigator from(PathNavigation nav) { return new ForgePlatformNavigator(nav); }


    @Override
    public boolean getCanFloat() {
        return ordinal.canFloat();
    }

    @Override
    public boolean canUpdatePath() {
        // no op...
        return false;
    }

    @Override
    public void setSpeed(double v) {
        ordinal.setSpeedModifier(v);
    }

    @Override
    public @Nullable AbstractPath getPath() {
        if (ordinal.getPath() == null) return null;
        return ForgePlatformPath.from(ordinal.getPath());
    }

    @Override
    public boolean isDone() {
        return ordinal.isDone();
    }

    @Override
    public boolean isInProgress() {
        return ordinal.isInProgress();
    }

    @Override
    public void tick() {
        ordinal.tick();
    }

    @Override
    public @Nullable IntVec3 getTargetPos() {
        if (ordinal.getTargetPos() == null) return null;
        return ForgeHacks.toVicky(ordinal.getTargetPos());
    }

    @Override
    public @Nullable AbstractPath createPath(@NotNull PlatformEntity platformEntity, int i) {
        if (platformEntity instanceof ForgePlatformEntity e) {
            return ForgePlatformPath.from(ordinal.createPath(e.ordinal, i));
        }
        return null;
    }

    @Override
    public @Nullable AbstractPath createPath(@NotNull IntVec3 intVec3, int i) {
        return ForgePlatformPath.from(ordinal.createPath(fromVicky(intVec3), i));
    }

    @Override
    public @Nullable AbstractPath createPath(@NotNull Set<? extends IntVec3> set, int i) {
        var BlockPosMap = set.stream().map(ForgeHacks::fromVicky)
                .collect(Collectors.toSet());
        return ForgePlatformPath.from(ordinal.createPath(BlockPosMap, i));
    }

    @Override
    public void moveTo(@NotNull AbstractPath abstractPath, double v) {
        if (abstractPath instanceof ForgePlatformPath e) {
            ordinal.moveTo(e.ordinal, v);
        }
    }

    @Override
    public void moveTo(@Nullable PlatformEntity platformEntity, double v) {
        if (platformEntity instanceof ForgePlatformEntity e) {
            ordinal.moveTo(e.ordinal, v);
        }
    }

    @Override
    public void stop() {
        ordinal.stop();
    }

    @Override
    public boolean isStuck() {
        return ordinal.isStuck();
    }

    @Override
    public void setCanFloat(boolean b) {
        ordinal.setCanFloat(b);
    }
}
