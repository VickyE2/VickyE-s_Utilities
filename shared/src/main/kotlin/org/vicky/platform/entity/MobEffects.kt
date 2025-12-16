package org.vicky.platform.entity

import org.vicky.platform.utils.ResourceLocation

data class UniversalEffect(
    val key: ResourceLocation,
    val displayName: String,
    val maxAmplifier: Int = 4,
    val isDebuff: Boolean = false,
    val isInstant: Boolean = false,
    val defaultDuration: Int = 200, // ticks
    val onTick: ((ctx: EffectTickContext) -> Unit)? = null,
    val onApply: ((ctx: EffectApplyContext) -> Unit)? = null,
    val onRemove: ((ctx: EffectRemoveContext) -> Unit)? = null,
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
    val effect: UniversalEffect,
    val amplifier: Int,
    val remainingDuration: Int
)

@ConsistentCopyVisibility
data class RegisteredUniversalEffect internal constructor(
    val effect: UniversalEffect
)

interface PlatformEffectBridge<T: PlatformLivingEntity> {
    fun registerEffect(effect: UniversalEffect) : RegisteredUniversalEffect
    fun getEffect(id: ResourceLocation) : RegisteredUniversalEffect?

    fun applyEffect(
        entity: T,
        effect: UniversalEffect,
        duration: Int,
        amplifier: Int
    )

    fun removeEffect(
        entity: T,
        effect: UniversalEffect
    )

    fun hasEffect(
        entity: T,
        effect: UniversalEffect
    ): Boolean

    fun getEffectData(
        entity: T,
        effect: UniversalEffect
    ): PlatformEffectInstance?
}
