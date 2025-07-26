package org.vicky.betterHUD.events

import org.bukkit.entity.LivingEntity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

class StatusEffectTickEvent(
    val entity: LivingEntity,
    val effectName: String,
    val currentDuration: Double,
    val maxDuration: Double
) : EntityEvent(entity) {

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
