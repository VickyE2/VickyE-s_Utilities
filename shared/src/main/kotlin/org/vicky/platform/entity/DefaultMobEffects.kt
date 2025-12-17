package org.vicky.platform.entity

@RegisterEffect
class DefaultMarkedMobEffect : EffectProvider {
    override fun create(): EffectDescriptor {
        return EffectDescriptor(
            "core" rli "marked",
            MobEffectCategory.NEUTRAL,
            "A simple Marker",
            0x777777,
            4,
            false,
            200,
            { },
            { },
            { }
        )
    }
}