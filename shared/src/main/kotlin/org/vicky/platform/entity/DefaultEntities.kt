/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

import org.vicky.coreregistry.CoreEntityRegistry
import org.vicky.platform.PlatformPlugin
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RegisterFactory

interface MobRegisteringClass {
    fun register(registry: PlatformPlugin)
}

@RegisterFactory
class DefaultEntities : MobRegisteringClass {
    private val testDummy = mob(
        key = "core" rli "test_dummy_mob",
        handler = DefaultHandlers.MobDefaultHandler,
        "core" rli "test_dummy",
        "core" rli "test_dummy",
        "core" rli "test_dummy"
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
            goal(DefaultTasks.LookAtNearestPlayer, emptyMap())
            goal(DefaultTasks.PassiveWander, emptyMap())
        }
    }

    override fun register(registry: PlatformPlugin) {
        registry.registerMobEntityDescriptor(
            testDummy
        )
    }
}