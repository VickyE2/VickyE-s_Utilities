/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import kotlin.ranges.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.vicky.forge.entity.navigation.ForgeAdaptablePathNavigator;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.forge.forgeplatform.useables.ForgePlatformWorldAdapter;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.entity.*;
import org.vicky.platform.entity.distpacher.EntityTaskManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformBasedLivingEntity extends PathfinderMob implements GeoEntity {
	private final MobEntityDescriptor descriptor;
	private final PlatformEntityFactory.RegisteredMobEntityEventHandler handler;
	private final Logger LOGGER = LogUtils.getLogger();
	private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
	private final ForgePlatformLivingEntity PLATFORM = ForgePlatformLivingEntity.from(this);

	protected int mobCount = 0;

	public PlatformBasedLivingEntity(@NotNull MobEntityDescriptor descriptor, EntityType<? extends PathfinderMob> type,
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

	public ForgePlatformLivingEntity getPlatform() {
		return PLATFORM;
	}

	@Override
	protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
		return new ForgeAdaptablePathNavigator(
				this,
				level,
                GroundPathNavigation::new,
                FlyingPathNavigation::new,
				WaterBoundPathNavigation::new,
				AmphibiousPathNavigation::new,
				WallClimberNavigation::new,
				MovementMode.GROUND
		);
	}


	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		descriptor.getMobDetails().getMetadata().forEach((key, value) -> {
			nbt.put(key, ForgeHacks.toNBT(value));
		});
	}

	@Override
	public boolean isDeadOrDying() {
		mobCount--;
		return super.isDeadOrDying();
	}

	@Override
	public void tick() {
		if (handler != null) {
			EventResult res = handler.getHandler().onTick(getPlatform());
			if (res == EventResult.CANCEL)
				return;
		}

		super.tick();
	}

	@Override
	protected void customServerAiStep() {
		long gameTime = level().getGameTime();
		EntityTaskManager.INSTANCE.tickEntity(getPlatform(), gameTime);
	}

	@Override
	public void onEnterCombat() {
		if (handler != null) {
			handler.getHandler().onEnterCombat(getPlatform());
		}
	}

	@Override
	public void onLeaveCombat() {
		if (handler != null) {
			handler.getHandler().onLeaveCombat(getPlatform());
		}
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		AntagonisticDamageSource wrap = convert(source);
		if (handler != null) {
			EventResult r = handler.getHandler().onHurt(getPlatform(), wrap, amount);
			if (r == EventResult.CONSUME)
				return true;
			if (r == EventResult.CANCEL)
				return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public void die(@NotNull DamageSource cause) {
		if (handler != null) {
			EventResult r = handler.getHandler().onDeath(getPlatform(), convert(cause));
			if (r == EventResult.CONSUME || r == EventResult.PASS) {
				super.die(cause);
			}
			EntityTaskManager.INSTANCE.clearTasks(getPlatform());
			EntityTaskManager.INSTANCE.removeEntity(getPlatform());
		}
	}

	@Override
	public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
		if (player instanceof ServerPlayer pp) {
			PlatformPlayer p = ForgePlatformPlayer.adapt(pp);
			if (handler != null) {
				EventResult r = handler.getHandler().onInteract(getPlatform(), p);
				if (r == EventResult.CONSUME)
					return InteractionResult.SUCCESS;
				if (r == EventResult.CANCEL)
					return InteractionResult.FAIL;
			}
		}
		return super.mobInteract(player, hand);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty,
			@NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
		onLoadOrReload();
		if (handler != null)
			handler.getHandler().onSpawn(getPlatform());

		return super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
		// The descriptor is where your Kotlin MobSpawnSettings lives
		MobSpawnSettings settings = descriptor.getMobDetails().getSpawn();
		if (settings == null) return super.checkSpawnRules(world, reason);
		BlockPos pos = this.blockPosition(); // entity's candidate spawn position

		// 1) Spawn type allowlist (example: prevent spawner spawns if you want)
		if (reason == MobSpawnType.SPAWNER && !settings.getTags().contains("allow_spawner")) {
			return false;
		}

		switch (settings.getSpawnHeight()) {
			case ON_GROUND:
				if (!isValidGroundSpawn(world, pos)) return false;
				break;
			case IN_WATER:
				if (!isInWaterSpawn(world, pos)) return false;
				break;
			case UNDERGROUND:
				if (!isUndergroundSpawn(world, pos)) return false;
				break;
			case IN_AIR:
				if (!isInAirSpawn(world, pos)) return false;
				break;
		}

		// 3) Light level
		int light = getBlockLightLevel(world, pos);
		IntRange range = settings.getLightLevel();
		if (!range.contains(light)) return false;

		// 4) Biome checks (allowed / prohibited)
		String biomeId = getBiomeId(world, pos);
		if (!settings.getAllowedBiomes().isEmpty() && !settings.getAllowedBiomes().contains(biomeId)) {
			return false;
		}
		if (settings.getProhibitedBiomes().contains(biomeId)) {
			return false;
		}

		// 5) Custom spawn conditions (user-defined predicates)
		for (SpawnCondition cond : settings.getConditions()) {
			// assume SpawnCondition has a boolean test(...) method you implement
			if (!cond.canSpawn(
					new SpawnContext(
							pos.getX(), pos.getY(), pos.getZ(),
							biomeId, light, world.dayTime(), new ForgePlatformWorldAdapter(world)
					)
			)) return false;
		}

		// 6) Population caps: per-chunk and global
		if (settings.getMaxPerChunk() > 0 && countSameMobInChunk(world, pos, settings.getMobId()) >= settings.getMaxPerChunk()) {
			return false;
		}
		if (settings.getMaxGlobal() > 0 && mobCount >= settings.getMaxGlobal()) {
			return false;
		}

		// 7) Modifiers could be used to tweak spawn (e.g., chance), but typically don't block:
		for (SpawnModifier mod : settings.getModifiers()) {
			mod.apply(getPlatform(),
					new SpawnContext(
							pos.getX(), pos.getY(), pos.getZ(),
							biomeId, light, world.dayTime(),
							new ForgePlatformWorldAdapter(world)
					));
		}

		// finally, fall back to the vanilla check to preserve standard rules like collision and pathfinding spots
		return super.checkSpawnRules(world, reason);
	}

	protected boolean isValidGroundSpawn(LevelAccessor world, BlockPos pos) {
		// ensure block below is solid and target pos is not submerged
		BlockPos below = pos.below();
		BlockState belowState = world.getBlockState(below);
		if (!belowState.isSolidRender(world, below)) return false; // API name might differ
		if (world.getFluidState(pos).is(FluidTags.WATER)) return false;
		// ensure spawn position is not colliding with blocks
		return world.noCollision(this);
	}
	protected boolean isInWaterSpawn(LevelAccessor world, BlockPos pos) {
		return world.getFluidState(pos).is(FluidTags.WATER);
	}
	protected boolean isUndergroundSpawn(LevelAccessor world, BlockPos pos) {
		// Must be air where entity spawns
		if (!world.getBlockState(pos).isAir()) return false;

		// No direct sky access (this is key)
		if (world.canSeeSky(pos)) return false;

		// Light should be low-ish (optional, but very common)
		int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
		if (blockLight > 7) return false;

		// Require solid ground somewhere nearby (below)
		BlockPos below = pos.below();
		BlockState belowState = world.getBlockState(below);
		if (!belowState.isSolidRender(world, below)) return false;

		// Collision safety
		return world.noCollision(this);
	}
	protected boolean isInAirSpawn(LevelAccessor world, BlockPos pos) {
		// Must be air
		if (!world.getBlockState(pos).isAir()) return false;

		// Block below must NOT be solid (otherwise it's ground)
		BlockPos below = pos.below();
		if (world.getBlockState(below).isSolidRender(world, below)) return false;

		// Require some vertical clearance (avoid spawning inside trees)
		for (int i = 1; i <= 2; i++) {
			if (!world.getBlockState(pos.above(i)).isAir()) return false;
		}

		// Optional: prevent cave air spawns
		if (!world.canSeeSky(pos)) return false;

		return world.noCollision(this);
	}
	private int getBlockLightLevel(LevelAccessor world, BlockPos pos) {
		try {
			Method m = world.getClass().getMethod("getMaxLocalRawBrightness", BlockPos.class);
			return (int) m.invoke(world, pos);
		} catch (Exception ignored) { }

		try {
			return world.getBrightness(LightLayer.BLOCK, pos);
		} catch (Exception ignored) { }
		// safest fallback
		return world.getMaxLocalRawBrightness(pos); // if available, else might throw — adapt accordingly
	}
	private String getBiomeId(LevelAccessor world, BlockPos pos) {
		try {
			Holder<Biome> holder = world.getBiome(pos);
			Optional<ResourceKey<Biome>> opt = holder.unwrapKey();
			if (opt.isPresent()) {
				return opt.get().location().toString();
			}
		} catch (Exception ignored) {}
		return "";
	}

	private int countSameMobInChunk(LevelAccessor world, BlockPos pos, org.vicky.platform.utils.ResourceLocation mobId) {
		ChunkAccess chunk = world.getChunk(pos);
		if (!(chunk instanceof LevelChunk levelChunk)) {
			return 0;
		}
		AtomicInteger count = new AtomicInteger();
		ChunkPos cp = levelChunk.getPos();
		AABB box = new AABB(
				cp.getMinBlockX(), levelChunk.getMinBuildHeight(), cp.getMinBlockZ(),
				cp.getMaxBlockX() + 1, levelChunk.getMaxBuildHeight(), cp.getMaxBlockZ() + 1
		);

		levelChunk.getLevel().getEntities(getType(), box, (p_312249_) -> true)
				.forEach((p_313067_) -> {
			count.getAndIncrement();
		});

		return count.get();
	}

	protected void installGoals(MobEntityAIBasedGoals ai) {
		LOGGER.info("Installing goals");
		ai.applyToEntity(getPlatform());
	}

	private AntagonisticDamageSource convert(DamageSource s) {
		return ForgeDamageSource.from(s);
	}

	@Override
	public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
		final var animations = descriptor.getMobDetails().getAnimations();

		// Idle: plays when the entity is NOT moving (stationary)
		controllers.add(new AnimationController<>(this, "Idle", 5,
				(event) -> animationControllerOf(event, animations.getIdle(), /* shouldBeStationary = */ true)));

		// Walk: plays when the entity IS moving
		controllers.add(new AnimationController<>(this, "Walk", 5,
				(event) -> animationControllerOf(event, animations.getWalk(), /* shouldBeStationary = */ false)));

		if (animations.getHurt() != null) {
			controllers.add(new AnimationController<>(this, "Hurt", 2, (event) -> {
				if (isForcedAnimation(event, animations.getHurt())) {
					return playForcedAnimation(event, animations.getHurt(), false);
				}
				return PlayState.STOP;
			}));
		}

		if (animations.getAttack() != null) {
			controllers.add(new AnimationController<>(this, "Attack", 2, (event) -> {
				if (isForcedAnimation(event, animations.getAttack())) {
					return playForcedAnimation(event, animations.getAttack(), false);
				}
				return PlayState.STOP;
			}));
		}

		if (animations.getShoot() != null) {
			controllers.add(new AnimationController<>(this, "Shoot", 4, (event) -> {
				if (isForcedAnimation(event, animations.getShoot())) {
					return playForcedAnimation(event, animations.getShoot(), false);
				}
				return PlayState.STOP;
			}));
		}

		if (animations.getStep() != null) {
			controllers.add(new AnimationController<>(this, "Step", 4, (event) -> {
				if (isForcedAnimation(event, animations.getStep())) {
					return playForcedAnimation(event, animations.getStep(), false);
				}
				return PlayState.STOP;
			}));
		}

		if (animations.getFlap() != null) {
			controllers.add(new AnimationController<>(this, "Fly", 2,
					(event) -> animationControllerOf(event, animations.getFlap(), false)));
		}

		if (animations.getSwim() != null) {
			controllers.add(new AnimationController<>(this, "Swim", 2,
					(event) -> animationControllerOf(event, animations.getSwim(), false)));
		}

		if (animations.getFall() != null) {
			controllers.add(new AnimationController<>(this, "Swim", 2,
					(event) -> animationControllerOf(event, animations.getFall(), false)));
		}

		// Add any custom animations as controllers — keys used as controller names
		animations.getCustom().forEach((key, rl) -> {
			controllers.add(new AnimationController<>(this, key, 5,
					(event) -> animationControllerOf(event, rl, /* stationary? */ false)));
		});
	}

	protected <E extends PlatformBasedLivingEntity> PlayState animationControllerOf(final AnimationState<E> event,
			String animationLocation, boolean shouldBeStationary) {

		// Protect against bad input
		if (animationLocation == null || animationLocation.isEmpty())
			return PlayState.STOP;

		final boolean moving = event.isMoving();

		// If a forced animation is present (from network), run it now.
		if (isForcedAnimation(event, animationLocation)) {
			// forced plays should honour loop choice done by the caller; we default to
			// non-loop for "hurt"-like names
			boolean loopForced = !looksLikeOneShot(animationLocation);
			return playForcedAnimation(event, animationLocation, loopForced);
		}

		// Decide whether this controller should play based on movement vs stationary
		final boolean shouldPlay = shouldBeStationary != moving;
		if (!shouldPlay)
			return PlayState.STOP;

		// Choose whether to loop or play once based on animation name heuristics.
		// (You can replace heuristics with explicit metadata if you prefer.)
		final boolean loop = !looksLikeOneShot(animationLocation);

		RawAnimation raw = loop
				? RawAnimation.begin().thenLoop(animationLocation)
				: RawAnimation.begin().thenPlay(animationLocation);
		return event.setAndContinue(raw);
	}

	/**
	 * Heuristic to detect animations that should be played once
	 * (hurt/attack/shoot/fall). Replace or extend this with explicit metadata if
	 * you have it.
	 */
	private boolean looksLikeOneShot(String animationLocation) {
		String s = animationLocation.toLowerCase();
		return s.contains("hurt") || s.contains("attack") || s.contains("shoot") || s.contains("fall")
				|| s.contains("hit");
	}

	/**
	 * Return true if the Animatable has a forced/externally requested animation
	 * matching this key. Implementation expects the animatable instance to expose a
	 * 'forced animation' field that the client-side adapter sets when a
	 * PlayAnimationPacket arrives.
	 *
	 * You need to implement getForcedAnimationName() /
	 * getAndClearForcedAnimationName() in your animatable wrapper.
	 */
	private <E extends PlatformBasedLivingEntity> boolean isForcedAnimation(AnimationState<E> event,
			String animationLocation) {
		// Example: check a small runtime override field
		String forced = getForcedAnimationForThisInstance();
		return forced != null && !forced.isEmpty() && forced.equals(animationLocation);
	}

	/**
	 * Play a forced animation immediately. For non-looping animations we clear the
	 * forced animation so the predicate won't try to restart it repeatedly.
	 */
	private <E extends PlatformBasedLivingEntity> PlayState playForcedAnimation(AnimationState<E> event,
			String animationLocation, boolean loop) {
		RawAnimation raw = loop
				? RawAnimation.begin().thenLoop(animationLocation)
				: RawAnimation.begin().thenPlay(animationLocation);
		// If it's non-looping, we should clear the forced flag so it doesn't replay
		// every tick.
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

	private String getForcedAnimationForThisInstance() {
		return forcedAnimation;
	}
	private void clearForcedAnimationForThisInstance() {
		forcedAnimation = null;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	private boolean initialized = false;

	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();

		if (!initialized) {
			initialized = true;

			MovementMode mode = descriptor.getMobDetails().getMovementModes().iterator().next();
			if (getNavigation() instanceof ForgeAdaptablePathNavigator nav) {
				nav.setMovementMode(mode);
			}

			onLoadOrReload();
		}
	}

	private void onLoadOrReload() {
		if (!level().isClientSide) {
			mobCount++;
			installGoals(descriptor.getAi());
		}
	}

	public MobEntityDescriptor descriptor() {
		return descriptor;
	}

    public void setNavigation(@NotNull PathNavigation navigation) {
        this.navigation = navigation;
    }
}
