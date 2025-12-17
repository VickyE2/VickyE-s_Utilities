package org.vicky.platform.entity

import org.vicky.platform.utils.ResourceLocation
import java.util.function.Consumer


/** Mark a class that provides an EffectDescriptor to be auto-registered.  */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RegisterEffect(
    /** Optional priority (higher -> registered earlier).  */
    val priority: Int = 0
)

interface EffectProvider {
    /**
     * Called by bootstrap to create the descriptor to register.
     * Must be a cheap, side-effect-free creation method.
     */
    fun create(): EffectDescriptor
}

enum class MobEffectCategory {
    BENEFICIAL,
    HARMFUL,
    NEUTRAL
}

interface PlatformEffect {
    val key: ResourceLocation
    val category: MobEffectCategory
    val displayName: String
    val color: Int
    val maxAmplifier: Int
    val isInstant: Boolean
    val defaultDuration: Int
    fun onTick(): Consumer<EffectTickContext> = Consumer { }
    fun onEffectStarted(): Consumer<EffectApplyContext> = Consumer { }
    fun onRemove(): Consumer<EffectRemoveContext> = Consumer { }
}

data class EffectDescriptor(
    val key: ResourceLocation,
    val category: MobEffectCategory,
    val displayName: String,
    val color: Int,
    val maxAmplifier: Int,
    val isInstant: Boolean,
    val defaultDuration: Int,
    val onTick: Consumer<EffectTickContext>,
    val onEffectStarted: Consumer<EffectApplyContext>,
    val onRemove: Consumer<EffectRemoveContext>
)

data class EffectApplyContext(
    val entity: PlatformLivingEntity,
    val amplifier: Int,
    val duration: Int,
)

data class EffectTickContext(
    val entity: PlatformLivingEntity,
    val amplifier: Int,
    var remainingDuration: Int,
)

data class EffectRemoveContext(
    val entity: PlatformLivingEntity,
    val amplifier: Int,
)

data class PlatformEffectInstance(
    val effect: PlatformEffect,
    val amplifier: Int,
    val remainingDuration: Int
)

@ConsistentCopyVisibility
data class RegisteredUniversalEffect internal constructor(
    val effect: PlatformEffect
)

interface PlatformEffectBridge<T: PlatformLivingEntity> {
    fun registerEffect(effect: EffectDescriptor) : RegisteredUniversalEffect
    fun getEffect(id: ResourceLocation) : RegisteredUniversalEffect?

    fun applyEffect(
        entity: T,
        effect: ResourceLocation,
        duration: Int,
        amplifier: Int
    )

    fun removeEffect(
        entity: T,
        effect: ResourceLocation
    )

    fun hasEffect(
        entity: T,
        effect: ResourceLocation
    ): Boolean

    fun getEffectData(
        entity: T,
        effect: ResourceLocation
    ): PlatformEffectInstance?
}
