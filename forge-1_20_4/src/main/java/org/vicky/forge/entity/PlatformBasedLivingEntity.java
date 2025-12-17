package org.vicky.forge.entity;

import net.minecraft.nbt.CompoundTag;
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

public class PlatformBasedLivingEntity extends PathfinderMob {
    private final MobEntityDescriptor descriptor;
    private final PlatformEntityFactory.RegisteredMobEntityEventHandler handler;

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
        PlatformPlayer p = ForgePlatformPlayer.adapt(player);
        if (handler != null) {
            EventResult r = handler.getHandler().onInteract(asPlatform(), p);
            if (r == EventResult.CONSUME) return InteractionResult.SUCCESS;
            if (r == EventResult.CANCEL) return InteractionResult.FAIL;
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
}
