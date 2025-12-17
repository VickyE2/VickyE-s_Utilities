package org.vicky.platform.entity

import org.vicky.coreregistry.CoreEntityRegistry
import org.vicky.platform.PlatformPlugin
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class RegisterFactory

interface MobRegisteringClass {
    fun register(registry: PlatformPlugin)
}

@RegisterFactory
class DefaultEntities : MobRegisteringClass {
    val testDummy = mob(
        key = "core" rli "test_dummy_mob",
        handler = DefaultHandlers.MobDefaultHandler
    ) {
        defaults("Dummy") {
            maxHealth = 40.0
            movementSpeed = 0.22
            modelId = PlatformPlugin.id() rli "default_dummy"
            spawn {
                condition { _ -> false }
            }
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