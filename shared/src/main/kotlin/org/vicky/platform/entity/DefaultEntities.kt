/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

import org.vicky.platform.entity.distpacher.Trigger
import org.vicky.platform.items.Animation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterMob

object DefaultEntities {
    @JvmField
    @field:RegisterMob
    val testDummy = mob(
        key = "test_dummy_mob".core(),
        handler = DefaultHandlers.MobDefaultHandler,
        "test_dummy".core(),
        "test_dummy".core(),
        "test_dummy".core()
    ) {
        defaults("Dummy") {
            spawnOverride {  }
            attributes {
                maxHealth = 40.0
                baseArmor = 0.43
                baseArmorToughness = 0.12
                entityGravity = 0.1
            }
            animations(
                EntityAnimationLayer("default", 0) { ctx ->
                    if (ctx.isMoving)
                        return@EntityAnimationLayer Animation("animation.test_dummy.walk", true, blendTime = 6)

                    Animation("animation.test_dummy.idle", true, blendTime = 6)
                },
            ) {
                headTracking(HeadTrackingConfiguration(
                    bone = "hi_head",
                    maxYaw = 360f,
                    maxPitch = 60f
                ))
            }
        }

        physical {
            hitBox(width = 1.7, depth = 0.8, height = 2.3)
        }

        ai {
            goal(DefaultTasks.LookAtNearestPlayer)
            goal(DefaultTasks.LookAtAttackerTillOutOfCombat, trigger = Trigger.Attacked)
            goal(DefaultTasks.PassiveWander)
        }
    }
}