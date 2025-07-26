package org.vicky.deathMessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.jetbrains.annotations.NotNull
import org.vicky.utilities.ContextLogger.ContextLogger

// Data class representing a type of death cause
data class DeathCause(
    val causeId: String,
    val template: String,
    val requiresKiller: Boolean,
    val appendPhrase: String,
    val priority: Int = 0
)

// Registry to manage all available causes of death
object DeathCauseRegistry {
    private val causeMap = mutableMapOf<String, MutableList<DeathCause>>()
    private val logger = ContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "DEATH-REGISTRY")

    fun registerCause(@NotNull key: String, @NotNull cause: DeathCause) {
        causeMap.computeIfAbsent(key) { mutableListOf() }.add(cause)
        logger.printBukkit("Registered death cause: $key", ContextLogger.LogType.BASIC, false)
    }

    @JvmOverloads
    fun getCauses(@NotNull key: String, killerExists: Boolean = false): List<DeathCause> {
        val all = causeMap[key] ?: return emptyList()
        return all.filter { it.requiresKiller == killerExists || !it.requiresKiller }
    }
}

// Builder for generating complex and grammatically correct death messages
class DeathMessageBuilder(private val playerName: String) {
    private var killerName: String? = null
    private val causeKeys = mutableListOf<String>()

    fun setKiller(killerName: String?): DeathMessageBuilder {
        this.killerName = killerName
        return this
    }

    fun addCauseKey(key: String): DeathMessageBuilder {
        causeKeys.add(key)
        return this
    }

    fun build(): Component {
        val mm = MiniMessage.miniMessage()
        val killerExists = killerName != null
        val resolvedCauses = causeKeys.mapNotNull {
            val causes = DeathCauseRegistry.getCauses(it, killerExists)
            if (causes.isNotEmpty()) causes.random() else null
        }.sortedByDescending { it.priority }

        if (resolvedCauses.isEmpty()) {
            return Component.text("$playerName died mysteriously.")
        }

        val base = resolvedCauses.first()
        val rest = resolvedCauses.drop(1)

        val messageBuilder = StringBuilder()
        messageBuilder.append(base.template)
        for ((i, cause) in rest.withIndex()) {
            val connector = if (i == 0) " while " else " and then "
            messageBuilder.append(connector).append(cause.appendPhrase)
        }

        val raw = messageBuilder.toString()
            .replace("{player}", playerName)
            .replace("{killer}", killerName ?: "something mysterious")

        val final = grammarFixer(raw)

        return mm.deserialize(final)
    }
}

fun grammarFixer(text: String): String {
    var fixed = text

    // Collapse repetitive connectors
    fixed = fixed.replace(Regex("\\bwhile and then\\b"), "and then")
    fixed = fixed.replace(Regex("\\bwhile while\\b"), "while also")
    fixed = fixed.replace(Regex("\\band and\\b"), "and")
    fixed = fixed.replace(Regex("\\bthen then\\b"), "then")

    // Capitalize first letter
    fixed = fixed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    // Fix duplicate phrasing like "running while running"
    fixed = fixed.replace(Regex("\\brunning while running\\b"), "running even harder")

    // Combine repeated words (bleeding bleeding -> bleeding heavily)
    fixed = fixed.replace(Regex("\\b(\\w+)\\s+\\1\\b")) { matchResult ->
        when (matchResult.groupValues[1]) {
            "bleeding" -> "bleeding heavily"
            "fleeing" -> "fleeing frantically"
            else -> matchResult.groupValues[1]
        }
    }

    return fixed
}

