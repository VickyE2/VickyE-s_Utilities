package org.vicky.forge.entity;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.Path;
import org.vicky.platform.entity.PathNavigator;
import org.vicky.platform.entity.PathPoint;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.utils.IntVec3;

import java.util.Set;

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
    public @Nullable org.vicky.platform.entity.AbstractPath getPath() {
        return null;
    }

    @Override
    public boolean isDone() {
        return false;
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
    public @Nullable org.vicky.platform.entity.AbstractPath createPath(@NotNull PlatformEntity platformEntity, int i) {
        return null;
    }

    @Override
    public @Nullable org.vicky.platform.entity.AbstractPath createPath(@NotNull IntVec3 intVec3, int i) {
        return null;
    }

    @Override
    public @Nullable org.vicky.platform.entity.AbstractPath createPath(@NotNull Set<? extends IntVec3> set, int i) {
        return null;
    }

    @Override
    public void moveTo(@NotNull org.vicky.platform.entity.AbstractPath abstractPath, double v) {

    }

    @Override
    public void moveTo(@Nullable PlatformEntity platformEntity, double v) {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStuck() {
        return false;
    }

    @Override
    public void setCanFloat(boolean b) {

    }
}
