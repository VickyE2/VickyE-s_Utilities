/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.derived

import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerEvent
import org.vicky.vicky_utils
import java.io.InputStream
import java.util.*
import java.util.function.BiConsumer

interface Compatibility {
    val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
    val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
    val numbers: Map<String, HudPlaceholder<Number>>
    val strings: Map<String, HudPlaceholder<String>>
    val booleans: Map<String, HudPlaceholder<Boolean>>
    fun start() {}
}

class Quad<W, X, Y, Z>(
    val w: W,
    val x: X,
    val y: Y,
    val z: Z
) {
    constructor(w: W, triple: Triple<X, Y, Z>) : this(
        w,
        triple.first,
        triple.second,
        triple.third
    )
}


fun <T : Event> createBukkitTrigger(
    clazz: Class<T>,
    valueMapper: (T) -> UUID? = { if (it is PlayerEvent) it.player.uniqueId else null },
    keyMapper: (T) -> Any = { UUID.randomUUID() }
): HudBukkitEventTrigger<T> {
    return object : HudBukkitEventTrigger<T> {
        override fun getEventClass(): Class<T> = clazz
        override fun getKey(t: T): Any = keyMapper(t)
        override fun registerEvent(eventConsumer: BiConsumer<UUID, UpdateEvent>) {
            Bukkit.getPluginManager().registerEvent(clazz, vicky_utils.getGlobalListener(), EventPriority.MONITOR, { _, e ->
                if (clazz.isAssignableFrom(e.javaClass)) {
                    val cast = clazz.cast(e)
                    valueMapper(cast)?.let { uuid ->
                        val wrapper = BukkitEventUpdateEvent(
                                cast,
                                keyMapper(cast)
                        )
                        eventConsumer.accept(uuid, wrapper)
                    }
                }
            }, vicky_utils.getPlugin(), true)
        }
    }
}