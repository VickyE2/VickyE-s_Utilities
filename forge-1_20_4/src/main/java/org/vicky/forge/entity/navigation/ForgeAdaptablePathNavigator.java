package org.vicky.forge.entity.navigation;

import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.platform.entity.MovementMode;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

public class ForgeAdaptablePathNavigator extends PathNavigation {

    @FunctionalInterface
    public interface NavFactory {
        PathNavigation create(Mob mob, Level level);
    }

    private final PlatformBasedLivingEntity mob;
    private final Level level;

    private final NavFactory groundFactory;
    private final NavFactory flyingFactory;
    private final NavFactory swimmingFactory;
    private final NavFactory amphibiousFactory;
    private final NavFactory climbingFactory;

    private MovementMode currentMode;
    private PathNavigation active;

    public ForgeAdaptablePathNavigator(
            @NotNull PlatformBasedLivingEntity mob,
            @NotNull Level level,
            @NotNull NavFactory groundFactory,
            @NotNull NavFactory flyingFactory,
            @NotNull NavFactory swimmingFactory,
            @NotNull NavFactory amphibiousFactory,
            @NotNull NavFactory climbingFactory,
            @NotNull MovementMode initialMode
    ) {
        super(mob, level);
        this.mob = mob;
        this.level = level;
        this.groundFactory = groundFactory;
        this.flyingFactory = flyingFactory;
        this.swimmingFactory = swimmingFactory;
        this.amphibiousFactory = amphibiousFactory;
        this.climbingFactory = climbingFactory;
        setMovementMode(initialMode);
    }

    public MovementMode getMovementMode() {
        return currentMode;
    }

    protected void bindToMob(@NotNull PathNavigation navigation) {
        this.mob.setNavigation(navigation);
    }

    public void setMovementMode(@NotNull MovementMode mode) {
        if (this.currentMode == mode && this.active != null) return;

        if (this.active != null) {
            this.active.stop();
        }

        this.currentMode = mode;
        this.active = createNavigation(mode);
        bindToMob(this.active);
    }

    private MovementMode resolveFallback(MovementMode requested, Set<MovementMode> allowed) {

        // 1. If it's allowed, just use it
        if (allowed.contains(requested)) {
            return requested;
        }

        // 2. Smart fallback logic (priority-based)
        return switch (requested) {
            case FLYING -> allowed.contains(MovementMode.GROUND) ? MovementMode.GROUND :
                    allowed.contains(MovementMode.SWIMMING) ? MovementMode.SWIMMING :
                            allowed.iterator().next();

            case SWIMMING -> allowed.contains(MovementMode.AMPHIBIOUS) ? MovementMode.AMPHIBIOUS :
                    allowed.contains(MovementMode.GROUND) ? MovementMode.GROUND :
                            allowed.iterator().next();

            case AMPHIBIOUS -> allowed.contains(MovementMode.SWIMMING) ? MovementMode.SWIMMING :
                    allowed.contains(MovementMode.GROUND) ? MovementMode.GROUND :
                            allowed.iterator().next();

            case CLIMBING -> allowed.contains(MovementMode.GROUND) ? MovementMode.GROUND :
                    allowed.iterator().next();

            case GROUND -> allowed.contains(MovementMode.AMPHIBIOUS) ? MovementMode.AMPHIBIOUS :
                    allowed.iterator().next();
        };
    }

    private int modeCooldown = 0;

    public void autoResolveMovementMode() {
        if (modeCooldown > 0) {
            modeCooldown--;
            return;
        }

        Set<MovementMode> allowed = mob.descriptor()
                .getMobDetails()
                .getMovementModes();

        MovementMode resolved = MovementModeResolver.resolve(mob, allowed);

        if (resolved != currentMode) {
            setMovementMode(resolved);
            modeCooldown = 10; // ~0.5 sec
        }
    }

    private @NotNull PathNavigation createNavigation(@NotNull MovementMode mode) {
        if (mob.descriptor() == null) return groundFactory.create(mob, level);
        if (mob.descriptor().getMobDetails().getMovementModes().isEmpty()) return groundFactory.create(mob, level);
        return switch (resolveFallback(mode, mob.descriptor().getMobDetails().getMovementModes())) {
            case FLYING -> flyingFactory.create(mob, level);
            case SWIMMING -> swimmingFactory.create(mob, level);
            case AMPHIBIOUS -> amphibiousFactory.create(mob, level);
            case CLIMBING -> climbingFactory.create(mob, level);
            case GROUND -> groundFactory.create(mob, level);
        };
    }

    private @NotNull PathNavigation nav() {
        if (active == null) {
            active = createNavigation(currentMode == null ? MovementMode.GROUND : currentMode);
        }
        return active;
    }

    @Override
    protected double getGroundY(@NotNull Vec3 p_186132_) {
        return super.getGroundY(p_186132_) - 1;
    }

    @Override
    public boolean canFloat() {
        return nav().canFloat();
    }

    @Override
    public void setSpeedModifier(double p_26518_) {
        nav().setSpeedModifier(p_26518_);
    }

    @Override
    public boolean canUpdatePath() {
        return switch (currentMode) {
            case GROUND -> mob.onGround() || mob.isInLiquid() || mob.isPassenger();
            case SWIMMING -> mob.isInLiquid();
            case AMPHIBIOUS -> mob.onGround() || mob.isInLiquid();
            case CLIMBING, FLYING -> true;
        };
    }

    @Override
    public @Nullable Path getPath() {
        return nav().getPath();
    }

    @Override
    public boolean isDone() {
        return nav().isDone();
    }

    @Override
    public boolean isInProgress() {
        return nav().isInProgress();
    }

    @Override
    public void tick() {
        autoResolveMovementMode();
        nav().tick();
    }

    @Override
    public @Nullable BlockPos getTargetPos() {
        return nav().getTargetPos();
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int range) {
        // fallback – not actually used because we delegate everything
        return new PathFinder(new net.minecraft.world.level.pathfinder.WalkNodeEvaluator(), range);
    }

    @Override
    public @Nullable Path createPath(@NotNull Entity e, int range) {
         return nav().createPath(e, range);
    }

    @Override
    public @Nullable Path createPath(@NotNull BlockPos e, int range) {
         return nav().createPath(e, range);
    }

    @Override
    public @Nullable Path createPath(@NotNull Set<BlockPos> e, int range) {
         return nav().createPath(e, range);
    }

    @Override
    public boolean moveTo(@Nullable Path p, double speed) {
        return nav().moveTo(p, speed);
    }

    @Override
    public boolean moveTo(@NotNull Entity p, double speed) {
        return nav().moveTo(p, speed);
    }

    @Override
    public boolean moveTo(double p_26520_, double p_26521_, double p_26522_, double p_26523_) {
        return nav().moveTo(p_26520_, p_26521_, p_26522_, p_26523_);
    }

    @Override
    public void recomputePath() {
        nav().recomputePath();
    }

    @Override
    public @Nullable Path createPath(@NotNull Stream<BlockPos> p_26557_, int p_26558_) {
        return nav().createPath(p_26557_, p_26558_);
    }

    @Override
    public @Nullable Path createPath(@NotNull BlockPos p_148219_, int p_148220_, int p_148221_) {
        return nav().createPath(p_148219_, p_148220_, p_148221_);
    }

    @Override
    public boolean canCutCorner(@NotNull BlockPathTypes p_265292_) {
        return nav().canCutCorner(p_265292_);
    }

    @Override
    public @NotNull NodeEvaluator getNodeEvaluator() {
        return nav().getNodeEvaluator();
    }

    @Override
    public void stop() {
        nav().stop();
    }

    @Override
    protected @NotNull Vec3 getTempMobPos() {
        return mob.position();
    }

    @Override
    public boolean isStuck() {
        return nav().isStuck();
    }

    @Override
    public void setCanFloat(boolean b) {
        nav().setCanFloat(b);
    }
}