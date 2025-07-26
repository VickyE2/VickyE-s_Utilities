/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.vickyUtilsCompat

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import org.bukkit.entity.LivingEntity
import org.bukkit.plugin.java.JavaPlugin
import org.vicky.betterHUD.derived.Compatibility
import org.vicky.betterHUD.derived.createBukkitTrigger
import org.vicky.effectsSystem.EffectRegistry
import org.vicky.effectsSystem.StatusEffect
import org.vicky.effectsSystem.events.EntityGainStatusEffect
import org.vicky.effectsSystem.events.PlayerGainStatusEffect
import org.vicky.kotlinUtils.*
import org.vicky.utilities.ContextLogger.ContextLogger
import org.vicky.vicky_utils.plugin
import java.io.File
import java.util.function.Function

private const val mapping_key = "vicky_compat"

class VickyUtilsCompat : Compatibility {
    private var lastSentTime: Long = 0
    private val cooldown: Int = 1000
    private val contextLogger: ContextLogger =
        ContextLogger(ContextLogger.ContextType.SYSTEM, "BETTER-HUD-COMPAT")
    private val triggerMap = mutableMapOf<String, (YamlObject) -> HudTrigger<*>>()
    private val listenerMap = mutableMapOf<String, (YamlObject) -> (UpdateEvent) -> HudListener>()
    private val numberMap = mutableMapOf<String, HudPlaceholder<Number>>()
    private val stringMap = mutableMapOf<String, HudPlaceholder<String>>()
    private val booleanMap = mutableMapOf<String, HudPlaceholder<Boolean>>()

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = triggerMap
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = listenerMap
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = numberMap
    override val strings: Map<String, HudPlaceholder<String>>
        get() = stringMap
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = booleanMap

    fun addTrigger(name: String, trigger: (YamlObject) -> HudTrigger<*>) {
        triggerMap[name] = trigger
    }

    fun addListener(name: String, listener: (YamlObject) -> (UpdateEvent) -> HudListener) {
        listenerMap[name] = listener
    }

    fun addNumberPlaceholder(name: String, placeholder: HudPlaceholder<Number>) {
        numberMap[name] = placeholder
    }

    fun addStringPlaceholder(name: String, placeholder: HudPlaceholder<String>) {
        stringMap[name] = placeholder
    }

    fun addBooleanPlaceholder(name: String, placeholder: HudPlaceholder<Boolean>) {
        booleanMap[name] = placeholder
    }

    private fun addBuiltIns() {
        triggerMap.putAll(mapOf(
            "player_gained_status_effect" to {
                val shouldNotShow = it.getAsBoolean("ambient", false)
                createBukkitTrigger(PlayerGainStatusEffect::class.java, { e ->
                    if (shouldNotShow) null
                    else e.player.uniqueId
                }) { e ->
                    e.effect.key
                }
            },
            "entity_gained_status_effect" to {
                val shouldNotShow = it.getAsBoolean("ambient", false)
                createBukkitTrigger(EntityGainStatusEffect::class.java, { e ->
                    if (shouldNotShow) null
                    else e.entity.uniqueId
                }) { e ->
                    e.effect.key
                }
            },
        ))
        listenerMap.putAll(mapOf(
            "status_effect_duration" to search@ { c ->
                val effectName = c["effect"]?.asString().ifNull { "effect_name not set." }
                val effect = EffectRegistry.getInstance(EffectRegistry::class.java).getEffect(effectName)
                    .ifNull { "The status effect named \"$effectName\" doesn't exist." }

                return@search { _: UpdateEvent ->
                    HudListener { p ->
                        val player = p.bukkitPlayer
                        val currDur = effect.ifType<StatusEffect, Number> { it.getDuration(player.uniqueId) }?.toDouble()
                        val initialDur = player.getEffectDuration(plugin, effectName) ?: return@HudListener 0.0
                        if (initialDur <= 0.0) return@HudListener 0.0
                        currDur?.div(initialDur)?.coerceIn(0.0, 1.0) ?: 0.0
                    }
                }
            }
        ))
        numberMap.putAll(mapOf(
            "effect_duration" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function {args, _ ->
                    Function { p ->
                        var duration = 0.0;
                        EffectRegistry.getInstance(EffectRegistry::class.java).getEffect(args[0]).ifPresent { e ->
                            duration = e.getDuration(p.bukkitPlayer.uniqueId)
                        }
                        duration
                    }
                }
                .build(),
            "effect_potency" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function {args, _ ->
                    Function { p ->
                        var duration = 0;
                        EffectRegistry.getInstance(EffectRegistry::class.java).getEffect(args[0]).ifPresent { e ->
                            duration = e.getLevel(p.bukkitPlayer.uniqueId)
                        }
                        duration
                    }
                }
                .build(),
            "effect_duration_normalised" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        val current = EffectRegistry.getInstance(EffectRegistry::class.java)
                            .getEffect(args[0])
                            .map { it.getDuration(p.bukkitPlayer.uniqueId) }
                            .orElse(0.0)

                        val max = (p.bukkitPlayer as LivingEntity)
                            .getEffectDuration(plugin, args[0])
                            ?: 0.0

                        if (max == 0.0) 0.0 else (current / max).coerceIn(0.0, 1.0)
                    }
                }
                .build()
        ))
        booleanMap.putAll(mapOf(
            "has_effect" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function {args, _ ->
                    Function { p ->
                        val data = p.bukkitPlayer.getStatusEffectData(plugin as JavaPlugin)
                        /*
                        if (Instant.now().toEpochMilli() - lastSentTime >= cooldown) {
                            p.bukkitPlayer.sendMessage(data.toString())
                            lastSentTime = Instant.now().toEpochMilli()
                        }
                         */
                        data.first.contains(args[0])
                    }
                }
                .build(),
            "is_infinite_effect" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function {args, _ ->
                    Function { p ->
                        p.bukkitPlayer.isEffectInfinite(plugin as JavaPlugin, args[0])
                    }
                }
                .build()
        ))
        stringMap.putAll(mapOf(
            "effect_duration_formatted" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        var duration = 0.0
                        EffectRegistry.getInstance(EffectRegistry::class.java).getEffect(args[0]).ifPresent { e ->
                            duration = e.getDuration(p.bukkitPlayer.uniqueId)
                        }

                        val totalSeconds = duration.toInt()
                        val minutes = totalSeconds / 60
                        val seconds = totalSeconds % 60
                        String.format("%d:%02d", minutes, seconds)
                    }
                }
                .build()
        ))
    }

    fun register() {
        contextLogger.printBukkit("Registering vicky_utilities compats...", ContextLogger.LogType.PENDING, true)
        val strap = BetterHud.getInstance()
        addBuiltIns()
        plugin
            .extractFolderFromJar(
                "BetterHud",
                File(plugin.dataFolder.parent, "BetterHud/")
            )

        registerTriggers(strap)
        registerNumbers(strap)
        registerStrings(strap)
        registerBooleans(strap)
        registerListeners(strap)

        contextLogger.printBukkit("All vicky_utilities compats registered successfully.", ContextLogger.LogType.SUCCESS, true)
    }
    private fun registerTriggers(strap: BetterHud) {
        contextLogger.printBukkit("- Registering Hud Triggers", ContextLogger.LogType.PENDING, true)
        triggers.forEach { (key, value) ->
            strap.triggerManager.addTrigger("${mapping_key}_$key", value)
            contextLogger.printBukkit("√ Trigger registered: ${mapping_key}_$key", ContextLogger.LogType.SUCCESS, true)
        }
    }
    private fun registerNumbers(strap: BetterHud) {
        contextLogger.printBukkit("- Registering Number Placeholders", ContextLogger.LogType.PENDING, true)
        numbers.forEach { (key, value) ->
            strap.placeholderManager.numberContainer.addPlaceholder("${mapping_key}_$key", value)
            contextLogger.printBukkit("√ Number placeholder registered: ${mapping_key}_$key", ContextLogger.LogType.SUCCESS, true)
        }
    }
    private fun registerStrings(strap: BetterHud) {
        contextLogger.printBukkit("- Registering String Placeholders", ContextLogger.LogType.PENDING, true)
        strings.forEach { (key, value) ->
            strap.placeholderManager.stringContainer.addPlaceholder("${mapping_key}_$key", value)
            contextLogger.printBukkit("√ String placeholder registered: ${mapping_key}_$key", ContextLogger.LogType.SUCCESS, true)
        }
    }
    private fun registerBooleans(strap: BetterHud) {
        contextLogger.printBukkit("- Registering Boolean Placeholders", ContextLogger.LogType.PENDING, true)
        booleans.forEach { (key, value) ->
            strap.placeholderManager.booleanContainer.addPlaceholder("${mapping_key}_$key", value)
            contextLogger.printBukkit("√ Boolean placeholder registered: ${mapping_key}_$key", ContextLogger.LogType.SUCCESS, true)
        }
    }
    private fun registerListeners(strap: BetterHud) {
        contextLogger.printBukkit("- Registering Listeners", ContextLogger.LogType.PENDING, true)
        listeners.forEach { (key, listenerFactory) ->
            strap.listenerManager.addListener("${mapping_key}_$key") { config ->
                val function = listenerFactory(config)
                java.util.function.Function { event -> function(event) }
            }
            contextLogger.printBukkit("√ Listener registered: ${mapping_key}_$key", ContextLogger.LogType.SUCCESS, true)
        }
    }
}
