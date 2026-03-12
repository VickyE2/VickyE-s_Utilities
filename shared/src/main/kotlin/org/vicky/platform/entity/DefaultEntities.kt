/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

import org.vicky.platform.PlatformPlugin
import org.vicky.platform.entity.distpacher.Trigger

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterMob

object DefaultEntities {
    @JvmStatic
    @RegisterMob
    private val testDummy = mob(
        key = "test_dummy_mob".core(),
        handler = DefaultHandlers.MobDefaultHandler,
        "test_dummy".core(),
        "test_dummy".core(),
        "test_dummy".core()
    ) {
        defaults("Dummy") {
            spawn {
                condition { _ -> false }
            }
            attributes {
                maxHealth = 40.0
                movementSpeed = 0.22
            }
            animations(
                "animation.test_dummy.idle",
                "animation.test_dummy.walk"
            )
        }

        physical {
            hitBox(width = 0.8, depth = 0.8, height = 2.3)
        }

        ai {
            goal(DefaultTasks.LookAtNearestPlayer)
            goal(DefaultTasks.LookAtAttackerTillOutOfCombat, trigger = Trigger.Attacked)
            goal(DefaultTasks.PassiveWander)
        }
    }
}