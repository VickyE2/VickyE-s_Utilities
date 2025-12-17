package org.vicky.platform.entity

import org.vicky.platform.utils.ResourceLocation

enum class MobEffectCategory {
    BENEFICIAL,
    HARMFUL,
    NEUTRAL
}

interface PlatformEffect {
    val key: ResourceLocation
    val displayName: String
    val color: Int
    val maxAmplifier: Int
    val isInstant: Boolean
    val defaultDuration: Int
    fun onTick(): ((ctx: EffectTickContext) -> Unit) = { }
    fun onEffectStarted(): ((ctx: EffectApplyContext) -> Unit) = { }
    fun onRemove(): ((ctx: EffectRemoveContext) -> Unit) = { }
}

data class EffectDescriptor(
    val key: ResourceLocation,
    val displayName: String,
    val color: Int,
    val maxAmplifier: Int,
    val isInstant: Boolean,
    val defaultDuration: Int,
    val onTick: ((ctx: EffectTickContext) -> Unit),
    val onEffectStarted: ((ctx: EffectApplyContext) -> Unit),
    val onRemove: ((ctx: EffectRemoveContext) -> Unit)
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
