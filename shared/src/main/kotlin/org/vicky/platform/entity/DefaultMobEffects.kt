/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

object DefaultMobEffects {
    @JvmStatic
    @RegisterEffect
    val marked =  EffectDescriptor(
        "marked".core(),
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