/* Licensed under Apache-2.0 2024. */
package org.vicky.kotlinUtils

import kr.toxicity.hud.api.bukkit.nms.NMS
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import org.apache.maven.artifact.repository.metadata.Plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.vicky.utilities.ContextLogger.ContextLogger
import org.vicky.vicky_utils
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.util.*
import javax.imageio.ImageIO

inline fun <T> T?.ifNull(default: () -> T): T {
    return this ?: default()
}

inline fun <T> Optional<T>.ifNull(default: () -> Any): Any {
    return if (this.isPresent) this.get() else default()
}

inline fun <reified T, R> Any?.ifType(block: (T) -> R): R? {
    return if (this is T) block(this) else null
}
inline fun <reified T> Any?.asTypeOrNull(): T? = this as? T
fun Any?.toUUID(): UUID? =
    try {
        UUID.fromString(this.toString())
    }
    catch (e: IllegalArgumentException) {
        null
    }
val HudPlayer.bukkitPlayer
    get() = handle() as Player
inline fun <reified T, R> Any?.mapIfType(block: (T) -> R): R? = (this as? T)?.let(block)
fun UUID.toBukkitPlayer(): Player? = Bukkit.getPlayer(this)

@Suppress("UNCHECKED_CAST")
@JvmOverloads
fun LivingEntity.getStatusEffectData(plugin: JavaPlugin,
                                     effectsKey: String = "statusEffects",
                                     durationsKey: String = "statusEffectsDurations"): Pair<List<String>, Map<String, Double>> {
    // Helper to get plugin metadata for a key
    fun getPluginMetadataValue(key: String): Any? {
        if (!hasMetadata(key)) return null
        return getMetadata(key)
            .firstOrNull { it.owningPlugin == plugin }
            ?.value()
    }

    val effects = getPluginMetadataValue(effectsKey) as? List<String> ?: listOf()
    val durations = getPluginMetadataValue(durationsKey) as? Map<String, Double> ?: mapOf()

    return (effects to durations)
}

fun LivingEntity.getEffectDuration(plugin: JavaPlugin, effectKey: String): Double? {
    val durations = this.getMetadata("statusEffectsDurations")
        .firstOrNull { it.owningPlugin == plugin }
        ?.value() as? Map<*, *>

    return durations?.get(effectKey) as? Double
}

fun mortalise(player: Player) {
    player.apply {
        isInvulnerable = false
        noDamageTicks = 0
        fireTicks = 0
        activePotionEffects.forEach { removePotionEffect(it.type) }
    }
}

fun LivingEntity.isEffectInfinite(plugin: JavaPlugin, effectKey: String): Boolean {
    return getEffectDuration(plugin, effectKey) == -1.0
}

inline val <reified T : Entity> T.adapt
    get() = this

inline fun <reified T : Event, R : Any> UpdateEvent.unwrap(block: (T) -> R): R {
    val evt = source()
    return if (evt is BukkitEventUpdateEvent) {
        val e = evt.event
        if (e is T) block(e)
        else throw RuntimeException("Unsupported event found: ${e.javaClass.simpleName}")
    } else throw RuntimeException("Unsupported update found: ${javaClass.simpleName}")
}


@JvmOverloads
fun writeImage(
    iconStream: InputStream,
    outputFolder: File,
    outputName: String,
    contextLogger: ContextLogger = ContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "IMAGE-PROCESSING"),
    outputExtension: String = "png",
    cropTo32: Boolean = false
) {
    if (!outputFolder.exists()) outputFolder.mkdirs()
    val outFile = File(outputFolder, "$outputName.$outputExtension")
    if (!outFile.exists()) {
        iconStream.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        if (cropTo32)
            resizeIconTo32x32(outFile, outFile)
        contextLogger.print("Image '$outputName' created", ContextLogger.LogType.SUCCESS, true)
    } else {
        contextLogger.print("Image '$outputName' already exists: ${outFile.name}", ContextLogger.LogType.WARNING, true)
    }
}


fun resizeIconTo32x32(source: File, target: File) {
    val originalImage = ImageIO.read(source)
    val resizedImage = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
    val g = resizedImage.createGraphics()
    g.drawImage(originalImage, 0, 0, 32, 32, null)
    g.dispose()
    ImageIO.write(resizedImage, "png", target)
}