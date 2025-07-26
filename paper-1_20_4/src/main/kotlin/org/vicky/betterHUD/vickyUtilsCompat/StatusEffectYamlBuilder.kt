package org.vicky.betterHUD.vickyUtilsCompat

import org.vicky.effectsSystem.enums.EffectArrangementType
import org.vicky.effectsSystem.enums.EffectType
import org.vicky.utilities.ConfigManager
object Statics {
    var MAX_POPUP_AMOUNT = 20
    var X_EQUATION = "(t % 10) * 40"
    var Y_EQUATION = "floor(t / 10) * 10"

    fun reloadFromConfig(config: ConfigManager) {
        MAX_POPUP_AMOUNT = (config.getConfigValue("effect_hud.max-popups") as? Int) ?: 20
        val arrangement : EffectArrangementType = (config.getConfigValue("effect_hud.arrangementType") as? EffectArrangementType) ?: EffectArrangementType.LEFT
        X_EQUATION = arrangement.getxEquation()
        Y_EQUATION = arrangement.getyEquation()
    }
}

@JvmOverloads
fun buildStatusEffectYaml(
    effectId: String,                   // e.g. "bleeding"
    effectKey: String,                 // e.g. "vicky_utils_bleeding"
    effectType: EffectType,
    useBar: Boolean = true,
    useTimer: Boolean = true,
    font: String = "vcompat",
    iconPath: String = "vcompat_effects/icons/$effectId.png",
    durationFormattedPlaceholder: String = "[vicky_compat_effect_duration_formatted:$effectKey]",
    levelFormattedPlaceholder: String = "[vicky_compat_effect_potency:$effectKey]",
    durationNormalizedPlaceholder: String = "vicky_compat_effect_duration_normalised:$effectKey",
): Triple<String, String, String> {
    val bars = listOf("green", "yellow", "orange", "red")
    val hexForBars = mapOf(
        "green" to "#008800",
        "yellow" to "#FFFF00",
        "orange" to "#FF8800",
        "red" to "#AA0000",
    )
    val thresholds = listOf(0.65 to Double.MAX_VALUE, 0.45 to 0.65, 0.15 to 0.45, 0.0 to 0.15)
    val layout = buildString {
        appendLine("vcompat_$effectId:")
        appendLine("  images:")
        // Icons
        appendLine("    1:")
        appendLine("      name: vcompat_${effectType.name.lowercase()}")
        appendLine("      scale: 0.8")
        appendLine("    2:")
        appendLine("      name: vcompat_${effectId}_icon")
        appendLine("      scale: 0.8")
        appendLine("      y: ${0.8 * 3}")
        if (useBar)
            bars.forEachIndexed { i, color ->
                val index = i + 3
                appendLine("    $index:")
                appendLine("      name: vcompat_${color}_bar_$effectId")
                appendLine("      scale: 0.8")
                appendLine("      x: ${0.8 * 3}")
                appendLine("      y: ${0.8 * 33}")
                appendLine("      layer: 2")
                appendLine("      conditions:")
                if (thresholds[i].second != Double.MAX_VALUE) {
                    appendLine("        1:")
                    appendLine("          first: $durationNormalizedPlaceholder")
                    appendLine("          second: ${thresholds[i].second}")
                    appendLine("          operation: \"<\"")
                }
                appendLine("        2:")
                appendLine("          first: $durationNormalizedPlaceholder")
                appendLine("          second: ${thresholds[i].first}")
                appendLine("          operation: \">=\"")
            }

        appendLine("  texts:")
        appendLine("    1:")
        appendLine("      name: $font")
        appendLine("      pattern: \"<b>$levelFormattedPlaceholder\"")
        appendLine("      align: right")
        appendLine("      x: ${0.8 * 36}")
        appendLine("      y: ${0.8 * 2}")
        appendLine("      scale: 0.5")
        appendLine("      outline: true")
        if (useTimer) {
            // Texts
            bars.forEachIndexed { i, color ->
                val index = i + 2
                appendLine("    $index:")
                appendLine("      name: $font")
                appendLine("      pattern: \"<${hexForBars[color]}><b>$durationFormattedPlaceholder\"")
                appendLine("      align: center")
                appendLine("      x: ${0.8 * 18}")
                appendLine("      y: ${0.8 * 40}")
                appendLine("      scale: 0.5")
                appendLine("      outline: true")
                appendLine("      conditions:")
                if (thresholds[i].second != Double.MAX_VALUE) {
                    appendLine("        1:")
                    appendLine("          first: $durationNormalizedPlaceholder")
                    appendLine("          second: ${thresholds[i].second}")
                    appendLine("          operation: \"<\"")
                }
                appendLine("        2:")
                appendLine("          first: $durationNormalizedPlaceholder")
                appendLine("          second: ${thresholds[i].first}")
                appendLine("          operation: \">=\"")
            }
            appendLine("  conditions:")
            appendLine("    1:")
            appendLine("      first: vicky_compat_has_effect:$effectKey")
            appendLine("      second: true")
            appendLine("      operation: \"==\"")
        }
    }

    val image = buildString {
        appendLine("vcompat_${effectId}_icon:")
        appendLine("  type: single")
        appendLine("  file: $iconPath")
        if (useBar)
            bars.forEach { color ->
                appendLine("vcompat_${color}_bar_$effectId:")
                appendLine("  type: listener")
                appendLine("  file: vcompat_effects/green_bar.png")
                appendLine("  split-type: right")
                appendLine("  setting:")
                appendLine("    listener:")
                appendLine("      class: vicky_compat_status_effect_duration")
                appendLine("      effect: $effectKey")
            }
    }

    val popup = """
        $effectId:
          group: vcompat_effect
          unique: true
          triggers:
            1:
              class: vicky_compat_player_gained_status_effect
          move:
            duration: ${Statics.MAX_POPUP_AMOUNT}
            pixel:
              x-equation: ${Statics.X_EQUATION}
              y-equation: ${Statics.Y_EQUATION}
          layouts:
            1:
              name: vcompat_$effectId
    """.trimIndent()

    return Triple(image.trim(), layout.trim(), popup)
}
