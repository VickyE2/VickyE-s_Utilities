package org.vicky.forge.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.entity.*;
import org.vicky.platform.entity.distpacher.EntityTaskManager;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlatformBasedLivingEntity extends PathfinderMob implements GeoEntity {
    private final MobEntityDescriptor descriptor;
    private final PlatformEntityFactory.RegisteredMobEntityEventHandler handler;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PlatformBasedLivingEntity(MobEntityDescriptor descriptor,
                                     EntityType<? extends PathfinderMob> type,
                                     Level level) {
        super(type, level);
        this.descriptor = descriptor;
        this.handler = descriptor.getEventHandler();

        this.setHealth((float) descriptor.getMobDetails().getMaxHealth());
        if (descriptor.getMobDetails().getPersistent())
            this.setPersistenceRequired();
        this.setNoAi(false);
        this.setCustomNameVisible(false);
    }

    public MobEntityDescriptor getDescriptor() {
        return descriptor;
    }

    public ForgePlatformLivingEntity asPlatform() {
        return ForgePlatformLivingEntity.from(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (handler != null) {
            EventResult res = handler.getHandler().onTick(asPlatform());
            if (res == EventResult.CONSUME) return;
        }

        if (!level().isClientSide) {
            long gameTime = level().getGameTime();
            EntityTaskManager.INSTANCE.tickEntity(asPlatform(),
                    gameTime);
        }
    }



    @Override
    public void onEnterCombat() {
        if (handler != null) {
            handler.getHandler().onEnterCombat(asPlatform());
        }
    }

    @Override
    public void onLeaveCombat() {
        if (handler != null) {
            handler.getHandler().onLeaveCombat(asPlatform());
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        // build AntagonisticDamageSource wrapper from DamageSource as needed
        AntagonisticDamageSource wrap = convert(source);
        if (handler != null) {
            EventResult r = handler.getHandler().onHurt(asPlatform(), wrap, amount);
            if (r == EventResult.CONSUME) return true; // consumed
            if (r == EventResult.CANCEL) return false; // cancelled
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        if (handler != null) {
            EventResult r = handler.getHandler().onDeath(asPlatform(), convert(cause));
            if (r == EventResult.CONSUME) {
                super.die(cause);
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer pp) {
            PlatformPlayer p = ForgePlatformPlayer.adapt(pp);
            if (handler != null) {
                EventResult r = handler.getHandler().onInteract(asPlatform(), p);
                if (r == EventResult.CONSUME) return InteractionResult.SUCCESS;
                if (r == EventResult.CANCEL) return InteractionResult.FAIL;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        // install AI goals from descriptor.ai
        MobEntityAIBasedGoals ai = descriptor.getAi();
        installGoals(ai);
        // call onSpawn
        if (handler != null) handler.getHandler().onSpawn(asPlatform());
        return super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
    }

    private void installGoals(MobEntityAIBasedGoals ai) {
        // translate descriptor goals into actual Goal instances and add to goalSelector/targetSelector
        // e.g. goalSelector.addGoal(priority, new MeleeAttackGoal(this, speed, useLongMemory));
        // implement a mapping from your ProducerIntendedTask -> Goal factory
    }

    private AntagonisticDamageSource convert(DamageSource s) {
        return ForgeDamageSource.from(s);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        final var animations = descriptor.getMobDetails().getAnimations();

        // Idle: plays when the entity is NOT moving (stationary)
        controllers.add(new AnimationController<>(this,
                "Idle", 5,
                (event) -> animationControllerOf(event,
                        animations.getIdle(),
                        /* shouldBeStationary = */ true)));

        // Walk: plays when the entity IS moving
        controllers.add(new AnimationController<>(this,
                "Walk", 5,
                (event) -> animationControllerOf(event,
                        animations.getWalk(),
                        /* shouldBeStationary = */ false)));

        if (animations.getHurt() != null) {
            controllers.add(new AnimationController<>(this,
                    "Hurt", 2,
                    (event) -> {
                        if (isForcedAnimation(event, animations.getHurt())) {
                            return playForcedAnimation(event, animations.getHurt(), false);
                        }
                        return PlayState.STOP;
                    }));
        }

        if (animations.getAttack() != null) {
            controllers.add(new AnimationController<>(this,
                    "Attack", 2,
                    (event) -> {
                        if (isForcedAnimation(event, animations.getAttack())) {
                            return playForcedAnimation(event, animations.getAttack(), false);
                        }
                        return PlayState.STOP;
                    }));
        }

        if (animations.getShoot() != null) {
            controllers.add(new AnimationController<>(this,
                    "Shoot", 4,
                    (event) -> {
                        if (isForcedAnimation(event, animations.getShoot())) {
                            return playForcedAnimation(event, animations.getShoot(), false);
                        }
                        return PlayState.STOP;
                    }));
        }

        if (animations.getStep() != null) {
            controllers.add(new AnimationController<>(this,
                    "Step", 4,
                    (event) -> {
                        if (isForcedAnimation(event, animations.getStep())) {
                            return playForcedAnimation(event, animations.getStep(), false);
                        }
                        return PlayState.STOP;
                    }));
        }

        if (animations.getFlap() != null) {
            controllers.add(new AnimationController<>(this,
                    "Fly", 2,
                    (event) -> animationControllerOf(event,
                            animations.getFlap(),
                            false)));
        }

        if (animations.getSwim() != null) {
            controllers.add(new AnimationController<>(this,
                    "Swim", 2,
                    (event) -> animationControllerOf(event,
                            animations.getSwim(),
                            false)));
        }

        if (animations.getFall() != null) {
            controllers.add(new AnimationController<>(this,
                    "Swim", 2,
                    (event) -> animationControllerOf(event,
                            animations.getFall(),
                            false)));
        }

        // Add any custom animations as controllers — keys used as controller names
        animations.getCustom().forEach((key, rl) -> {
            controllers.add(new AnimationController<>(this,
                    key, 5,
                    (event) -> animationControllerOf(event, rl, /* stationary? */ false)));
        });
    }

    protected <E extends PlatformBasedLivingEntity> PlayState animationControllerOf(
            final AnimationState<E> event,
            String animationLocation,
            boolean shouldBeStationary) {

        // Protect against bad input
        if (animationLocation == null || animationLocation.isEmpty()) return PlayState.STOP;

        final boolean moving = event.isMoving();

        // If a forced animation is present (from network), run it now.
        if (isForcedAnimation(event, animationLocation)) {
            // forced plays should honour loop choice done by the caller; we default to non-loop for "hurt"-like names
            boolean loopForced = !looksLikeOneShot(animationLocation);
            return playForcedAnimation(event, animationLocation, loopForced);
        }

        // Decide whether this controller should play based on movement vs stationary
        final boolean shouldPlay = shouldBeStationary != moving;
        if (!shouldPlay) return PlayState.STOP;

        // Choose whether to loop or play once based on animation name heuristics.
        // (You can replace heuristics with explicit metadata if you prefer.)
        final boolean loop = !looksLikeOneShot(animationLocation);

        RawAnimation raw = loop ? RawAnimation.begin().thenLoop(animationLocation) : RawAnimation.begin().thenPlay(animationLocation);
        return event.setAndContinue(raw);
    }

    /**
     * Heuristic to detect animations that should be played once (hurt/attack/shoot/fall).
     * Replace or extend this with explicit metadata if you have it.
     */
    private boolean looksLikeOneShot(String animationLocation) {
        String s = animationLocation.toLowerCase();
        return s.contains("hurt") || s.contains("attack") || s.contains("shoot") || s.contains("fall") || s.contains("hit");
    }

    /**
     * Return true if the Animatable has a forced/externally requested animation matching this key.
     * Implementation expects the animatable instance to expose a 'forced animation' field that the client-side
     * adapter sets when a PlayAnimationPacket arrives.
     *
     * You need to implement getForcedAnimationName() / getAndClearForcedAnimationName() in your animatable wrapper.
     */
    private <E extends PlatformBasedLivingEntity> boolean isForcedAnimation(AnimationState<E> event, String animationLocation) {
        // Example: check a small runtime override field
        String forced = getForcedAnimationForThisInstance();
        return forced != null && !forced.isEmpty() && forced.equals(animationLocation);
    }

    /**
     * Play a forced animation immediately. For non-looping animations we clear the forced animation
     * so the predicate won't try to restart it repeatedly.
     */
    private <E extends PlatformBasedLivingEntity> PlayState playForcedAnimation(AnimationState<E> event, String animationLocation, boolean loop) {
        RawAnimation raw = loop ? RawAnimation.begin().thenLoop(animationLocation) : RawAnimation.begin().thenPlay(animationLocation);
        // If it's non-looping we should clear the forced flag so it doesn't replay every tick.
        if (!loop) {
            clearForcedAnimationForThisInstance();
        }
        return event.setAndContinue(raw);
    }

    private volatile String forcedAnimation = null;
    private volatile Boolean forcedAnimationLooped = false;

    public void forcePlay(String animation, Boolean loop) {
        this.forcedAnimation = animation;
        this.forcedAnimationLooped = loop;
    }

    private String getForcedAnimationForThisInstance() { return forcedAnimation; }
    private void clearForcedAnimationForThisInstance() { forcedAnimation = null; }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

}
