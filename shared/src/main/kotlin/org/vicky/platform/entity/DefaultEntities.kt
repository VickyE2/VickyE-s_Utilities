package org.vicky.platform.entity

import org.vicky.coreregistry.CoreEntityRegistry
import org.vicky.platform.PlatformPlugin

object DefaultEntities {
    val testDummy = mob(
        key = "core:test_dummy_mob",
        handler = DefaultHandlers.MobDefaultHandler
    ) {
        defaults("Dummy") {
            maxHealth = 40.0
            movementSpeed = 0.22
            modelId = "${PlatformPlugin.id()}:default_dummy"
        }

        physical {
            hitBox(width = 0.8, depth = 0.8, height = 2.3)
        }

        ai {
            goal(DefaultTasks.LookAtNearestPlayer, emptyMap())
            goal(DefaultTasks.PassiveWander, emptyMap())
        }
    }

    fun register() {
        CoreEntityRegistry.register(
            testDummy
        )
    }
}