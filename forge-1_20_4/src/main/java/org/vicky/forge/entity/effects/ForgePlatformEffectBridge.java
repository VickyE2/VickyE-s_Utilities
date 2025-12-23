/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.effects;

import static org.vicky.VickyUtilitiesForge.LOGGER;
import static org.vicky.VickyUtilitiesForge.MODID;
import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;
import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.platform.entity.*;
import org.vicky.platform.utils.ResourceLocation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// make universal effect an interface
public class ForgePlatformEffectBridge implements PlatformEffectBridge<ForgePlatformLivingEntity> {
	public static final ForgePlatformEffectBridge INSTANCE = new ForgePlatformEffectBridge();

	private ForgePlatformEffectBridge() {
		registerVanillaEffects();
	}

	private void registerVanillaEffects() {
		BuiltInRegistries.MOB_EFFECT.entrySet().stream().map(Map.Entry::getValue).forEach(effect -> {
			registry.put(toVicky(effect.builtInRegistryHolder().key().location()),
					new EffectDescriptor(toVicky(effect.builtInRegistryHolder().key().location()),
							MobEffectCategory.valueOf(effect.getCategory().name()), effect.getDisplayName().getString(),
							effect.getColor(), 225, effect.isInstantenous(), 200, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									effect.onEffectStarted(e.ordinal, ctx.getAmplifier());
							}, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									effect.applyEffectTick(e.ordinal, ctx.getAmplifier());
							}, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									effect.applyEffectTick(e.ordinal, ctx.getAmplifier());
							}));
		});
	}

	/**
	 * Registered descriptors by platform ResourceLocation.
	 */
	private final Map<ResourceLocation, EffectDescriptor> registry = new ConcurrentHashMap<>();

	/**
	 * Active effects per-entity (entity UUID -> (effectId -> instance)).
	 */
	private final Map<UUID, Map<ResourceLocation, PlatformEffectInstance>> active = new ConcurrentHashMap<>();

	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
			MODID);
	private final Map<ResourceLocation, RegistryObject<MobEffect>> registeredEffects = new ConcurrentHashMap<>();

	@Override
	public void registerEffect(@NotNull EffectDescriptor effectDescriptor) {
		ResourceLocation id = effectDescriptor.getKey();
		if (registeredEffects.containsKey(id)) {
			LOGGER.warn("Effect with id {}, has already been registered.", id);
			return;
		}

		registry.put(id, effectDescriptor);
		String name = id.getPath();

		if (!registeredEffects.containsKey(id)) {
			RegistryObject<MobEffect> ro = EFFECTS.register(name,
					() -> PlatformInstanceMobEffect.from(effectDescriptor));
			registeredEffects.put(id, ro);
		} else {
			LOGGER.warn(
					"Somehow, the effect wasn't registered in the local registry but was in the deferred registry.");
		}
		LOGGER.info("Registered effect: {}", id);
	}

	@Override
	public @Nullable RegisteredUniversalEffect getEffect(@NotNull ResourceLocation id) {
		RegistryObject<MobEffect> ro = registeredEffects.get(id);
		if (ro != null) {
			if (ro.isPresent()) {
				return new RegisteredUniversalEffect(ForgePlatformEffect.from(ro.get()));
			}
		}

		MobEffect found = ForgeRegistries.MOB_EFFECTS.getValue(fromVicky(id));
		if (found != null) {
			var holder = ForgeRegistries.MOB_EFFECTS.getHolder(found);
			if (holder.isPresent())
				if (!registry.containsKey(toVicky(holder.get().unwrapKey().get().location())))
					registry.put(toVicky(holder.get().unwrapKey().get().location()),
							new EffectDescriptor(toVicky(holder.get().unwrapKey().get().location()),
									MobEffectCategory.valueOf(found.getCategory().name()), found.getDisplayName().getString(),
									found.getColor(), 225, found.isInstantenous(), 200, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									found.onEffectStarted(e.ordinal, ctx.getAmplifier());
							}, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									found.applyEffectTick(e.ordinal, ctx.getAmplifier());
							}, (ctx) -> {
								if (ctx.getEntity() instanceof ForgePlatformLivingEntity e)
									found.applyEffectTick(e.ordinal, ctx.getAmplifier());
							}));
			return new RegisteredUniversalEffect(ForgePlatformEffect.from(found));
		}

		return null;
	}


	// -------------------------
	// Apply / Remove / Query
	// -------------------------
	@Override
	public void applyEffect(@NotNull ForgePlatformLivingEntity entity, @NotNull ResourceLocation effectId, int duration,
			int amplifier) {

		var effect = getEffect(effectId);
		if (effect == null) return;

		EffectDescriptor d = registry.get(effectId);
		if (d == null)
			return;

		UUID uuid = entity.getUuid();
		Map<ResourceLocation, PlatformEffectInstance> map = active.computeIfAbsent(uuid,
				k -> new ConcurrentHashMap<>());

		int amp = Math.min(amplifier, d.getMaxAmplifier());
		int dur = Math.max(0, duration > 0 ? duration : d.getDefaultDuration());

		PlatformEffectInstance inst = new PlatformEffectInstance(effect.getEffect(), amp, dur);
		map.put(effectId, inst);

		// fire start callback
		try {
			d.getOnEffectStarted().accept(new EffectApplyContext(entity, amp, dur));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if you want client-side visuals, send a packet here
	}

	@Override
	public void removeEffect(@NotNull ForgePlatformLivingEntity entity, @NotNull ResourceLocation effectId) {
		Map<ResourceLocation, PlatformEffectInstance> map = active.get(entity.getUuid());
		if (map == null)
			return;
		PlatformEffectInstance removed = map.remove(effectId);
		if (removed == null)
			return;

		try {
			removed.getEffect().onRemove().accept(new EffectRemoveContext(entity, removed.getAmplifier()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// cleanup attributes or client sync if needed
	}

	@Override
	public boolean hasEffect(@NotNull ForgePlatformLivingEntity entity, @NotNull ResourceLocation effectId) {
		Map<ResourceLocation, PlatformEffectInstance> map = active.get(entity.getUuid());
		return map != null && map.containsKey(effectId);
	}

	@Override
	public @Nullable PlatformEffectInstance getEffectData(@NotNull ForgePlatformLivingEntity entity,
			@NotNull ResourceLocation effectId) {
		Map<ResourceLocation, PlatformEffectInstance> map = active.get(entity.getUuid());
		return map == null ? null : map.get(effectId);
	}

	// -------------------------
	// Tick handling (call from entity.tick())
	// -------------------------
	public void tickEntityEffects(@NotNull ForgePlatformLivingEntity entity) {
		if (entity.ordinal.level().isClientSide())
			return;
		Map<ResourceLocation, PlatformEffectInstance> map = active.get(entity.getUuid());
		if (map == null || map.isEmpty())
			return;

		List<ResourceLocation> toRemove = new ArrayList<>();
		for (Map.Entry<ResourceLocation, PlatformEffectInstance> e : map.entrySet()) {
			ResourceLocation id = e.getKey();
			PlatformEffectInstance inst = e.getValue();
			int rem = inst.getRemainingDuration();
			if (rem <= 0) {
				toRemove.add(id);
				continue;
			}

			// call tick callback
			try {
				inst.getEffect().onTick().accept(new EffectTickContext(entity, inst.getAmplifier(), rem));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// decrement duration
			rem -= 1;
			if (rem <= 0) {
				toRemove.add(id);
			} else {
				// replace instance with updated remaining duration (immutable dataclass)
				map.put(id, new PlatformEffectInstance(inst.getEffect(), inst.getAmplifier(), rem));
			}
		}

		// remove expired
		for (ResourceLocation id : toRemove) {
			PlatformEffectInstance removed = map.remove(id);
			if (removed != null) {
				try {
					removed.getEffect().onRemove().accept(new EffectRemoveContext(entity, removed.getAmplifier()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
