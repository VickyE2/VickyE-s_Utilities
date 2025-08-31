package org.vicky.musicPlayer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.vicky.music.MusicRegistry.genreColors
import org.vicky.music.utils.MusicBuilder
import org.vicky.music.utils.MusicEvent
import org.vicky.music.utils.MusicPiece
import org.vicky.music.utils.MusicTrack
import org.vicky.platform.IColor
import org.vicky.platform.PlatformBossBar
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.PlatformPlugin
import org.vicky.platform.defaults.BossBarOverlay
import org.vicky.platform.defaults.VanillaColor
import org.vicky.platform.utils.BossBarDescriptor
import org.vicky.utilities.DatabaseManager.dao_s.MusicPieceDAO
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO
import java.util.*
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.pow

object MusicPlayer {
    private val playerStates = mutableMapOf<UUID, PlayerState>()
    // Java-style static map for reverse lookup (pitch → name)
    private val NOTE_ORDER = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val OCTAVE_SHIFTS = mapOf("--" to 2, "-" to 3, "" to 4, "+" to 5, "++" to 6)
    private val NOTE_TO_PITCH: Map<String, Float> = buildMap {
        for ((octaveSuffix, octave) in OCTAVE_SHIFTS) {
            for ((index, noteName) in NOTE_ORDER.withIndex()) {
                val semitoneIndex = (octave * 12) + index
                val halfStepsFromA4 = semitoneIndex - 57 // A4 = 57
                val pitch = 2.0.pow(halfStepsFromA4 / 12.0).toFloat()
                if (noteName.length > 1 || noteName.contains("#"))
                    put(noteName[0] + octaveSuffix + noteName[1], pitch)
                else
                    put(noteName + octaveSuffix, pitch)
            }
        }
    }

    private var loggingEnabled: Boolean = false
    fun toggleLogging(): Boolean {
        loggingEnabled = !loggingEnabled
        PlatformPlugin.logger().info("MusicPlayer logging is now ${if (loggingEnabled) "ENABLED" else "DISABLED"}")
        return loggingEnabled
    }
    private fun log(message: String) {
        if (loggingEnabled) {
            PlatformPlugin.logger().info("[MusicPlayer] $message")
        }
    }
    private fun log(player: PlatformPlayer, message: String) {
        if (loggingEnabled) {
            PlatformPlugin.logger()
                .info("[MusicPlayer] [${PlainTextComponentSerializer.plainText().serialize(player.name())}] $message")
        }
    }

    data class PlayerState(
        var track: MusicPiece,
        var tick: Int = 0,
        var paused: Boolean = false,
        var queue: MutableList<MusicPiece> = mutableListOf(),
        var bossBar: PlatformBossBar,
        var tickEvents: MutableMap<Long, MutableList<MusicEvent>> = mutableMapOf()
    )

    @JvmOverloads
    fun play(player: PlatformPlayer, track: MusicPiece, iconResourceLocation: String = "minecraft:block/dirt") {
        log(player, "Started playing track '${track.pieceName}' with ${track.trackList.sumOf { it.events.size }} total events.")
        val tickEvents = mutableMapOf<Long, MutableList<MusicEvent>>()
        track.trackList.forEach { t ->
            t.events.forEach { e ->
                tickEvents.getOrPut(e.timeOffset) { mutableListOf() }.add(e)
            }
        }
        val state = playerStates.getOrPut(player.uniqueId()) {
            val genre = track.genre?.lowercase() ?: "default"
            val color = genreColors[genre] ?: TextColor.color(0xAAAAAA)

            val status = "▶ Now Playing : ${track.pieceName}"
            val title = Component.text("$status: ${track.pieceName}", color)
            val progress = (0).toFloat().coerceIn(0f, 1f)
            PlayerState(
                track = track, bossBar = PlatformPlugin.bossBarFactory().createBossBar(
                    MusicBossBarDescriptor(
                        title,
                        Component.text(track.authors.toString().replace("[", "").replace("]", ""), NamedTextColor.GRAY),
                        progress,
                        VanillaColor.decode(color.asHexString()),
                        BossBarOverlay.PROGRESS,
                        genre,
                        true,
                        tickEvents.keys.max(),
                        0,
                        iconResourceLocation
                    ).toPlainBossBarDescriptor()
                )
            )
        }
        state.tickEvents = tickEvents
        state.track = track
        state.tick = 0
        state.paused = false
        log("[MusicPlayer.trace] about to call player.showBossBar for ${player.uniqueId()} bossbar=${state.bossBar}")
        try {
            player.showBossBar(state.bossBar)
            log("[MusicPlayer.trace] player.showBossBar returned normally")
        } catch (t: Throwable) {
            t.printStackTrace()
            log("[MusicPlayer.trace] player.showBossBar threw: " + t.message)
        }
        playTick(player)
    }

    fun playInstrumentTracks(player: PlatformPlayer, instrumentTracks: List<MusicTrack>) {
        instrumentTracks.forEach(Consumer { t: MusicTrack -> playTrack(player, t) })
    }

    /**
     * Plays a MusicTrack for the given player.
     *
     * @param player the player to play the track for.
     * @param track  the MusicTrack to play.
     */
    fun playTrack(player: PlatformPlayer, track: MusicTrack) {
        log(player, "Playing raw track with ${track.events.size} events.")
        val arrangedEvents: MutableMap<Long, MutableList<MusicEvent>> = HashMap()

        for (event in track.events) {
            arrangedEvents.computeIfAbsent(event.timeOffset) { ArrayList() }.add(event)
        }

        for ((tickOffset, events) in arrangedEvents) {
            PlatformPlugin.scheduler().runScheduled(Runnable {
                for (event in events) {
                    val soundName = resolveCustomSound(event)
                    player.playSound(player.location, soundName, event.category, event.volume, event.pitch)
                }
            }, tickOffset)
        }
    }

    private fun resolveCustomSound(event: MusicEvent): String {
        val instrument = event.sound.name.lowercase() // e.g., "piano"
        val pitchName = resolvePitchName(event.pitch) // e.g., "c_plus_plus"
        val partSuffix = when (event.part) {
            MusicBuilder.NotePart.IN -> "_1"
            MusicBuilder.NotePart.MAIN -> "_2"
            MusicBuilder.NotePart.OUT -> "_3"
            else -> ""
        }

        log("Resolved sound name: vicky_music:vicky_note_${instrument}_${pitchName.lowercase()}$partSuffix")
        return "vicky_music:vicky_note_${instrument}_${pitchName.lowercase()}$partSuffix"
    }

    private fun resolvePitchName(pitch: Float): String {
        return NOTE_TO_PITCH
            .entries
            .associate { it.value to it.key } // Flip to Map<Float, String>
            .minByOrNull { abs(it.key - pitch) } // Compare keys (the pitch floats)
            ?.value
            ?.replace("#", "_sharp")
            ?.replace("-", "_minus")
            ?.replace("+", "_plus") ?: "unknown"
    }

    fun togglePause(player: PlatformPlayer) {
        playerStates[player.uniqueId()]?.let {
            it.paused = !it.paused
            log(player, if (it.paused) "Paused at tick ${it.tick}" else "Resumed at tick ${it.tick}")
            updateBossBar(player, it.track, it.tick, it.paused)
            val dbMusic = MusicPlayerDAO().findById(player.uniqueId()).get()
            dbMusic.lastPiece = MusicPieceDAO().findById(it.track.key)
            dbMusic.lastTick = it.tick
            MusicPlayerDAO().update(dbMusic)
        }
    }

    fun tickAll() {
        // Use iterator so we can remove safely while iterating
        val it = playerStates.entries.iterator()
        while (it.hasNext()) {
            val (uuid, state) = it.next()

            // If it's already paused, skip
            if (state.paused) continue

            // Try to get the platform player; if not present, pause this state
            val optPlayer = PlatformPlugin.getPlayer(uuid)
            if (optPlayer.isEmpty) {
                // Player disconnected — pause their music state instead of dropping it
                state.paused = true
                log("Paused music for disconnected player: $uuid")
                continue
            }

            val player = optPlayer.get()

            // Protect playTick from throwing and killing the loop
            try {
                playTick(player)
            } catch (t: Throwable) {
                t.printStackTrace()
                log("playTick threw for $uuid: ${t.message}")
                // optionally pause to avoid repeated exceptions
                state.paused = true
                continue
            }

            state.tick++

            if (state.tick >= state.track.totalDuration()) {
                if (state.queue.isNotEmpty()) {
                    // start next queued track
                    play(player, state.queue.removeFirst())
                } else {
                    // finished and no queue: hide bossbar and remove state safely via iterator
                    try {
                        player.hideBossBar(state.bossBar)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        log("hideBossBar threw for $uuid: ${t.message}")
                    }
                    it.remove() // safe removal while iterating
                }
            } else {
                updateBossBar(player, state.track, state.tick, false)
            }
        }
    }

    private fun playTick(player: PlatformPlayer) {
        val state = playerStates[player.uniqueId()] ?: return
        val events = state.tickEvents[state.tick.toLong()] ?: return
        events.forEach { event ->
            log(player, "volume: ${event.volume}")
            player.playSound(player.location, resolveCustomSound(event), event.category, event.volume, 1f)
        }
    }


    private fun updateBossBar(player: PlatformPlayer, track: MusicPiece, tick: Int, paused: Boolean) {
        val genre = track.genre?.uppercase() ?: "default"
        val color = genreColors[genre] ?: TextColor.color(0xe5e49d)
        val status = if (paused) "⏸ Paused" else "▶ Now Playing"
        val title = Component.text("$status: ", color, TextDecoration.BOLD).append(Component.text(track.pieceName, TextColor.color(track.themeColorHex)))
        val progress = (tick.toDouble() / track.totalDuration()).toFloat().coerceIn(0f, 1f)
        val bossBar = playerStates[player.uniqueId()]?.bossBar ?: return

        // ⬇ Update descriptor if necessary
        if (bossBar.descriptor.information["type"] === "MusicBossBarDescriptor") {
            val cloned = bossBar.descriptor.clone()
            cloned.information["isPaused"] = paused
            cloned.information["currentTick"] = tick
            cloned.progress = progress
            cloned.title = title
            bossBar.descriptor = cloned
            bossBar.updateFromDescriptor()
        }
    }
}

class MusicBossBarDescriptor(
    private var title: Component,
    private var subTitle: Component?,
    private var progress: Float = 1.0f,
    private var color: IColor? = null,
    private val overlay: BossBarOverlay? = null,

    val genre: String? = null,
    var isPaused: Boolean = false,
    val trackDuration: Long? = null,
    var currentTick: Int? = null,
    var icon: String? = null
) : BossBarDescriptor(title, subTitle, progress, color, overlay, "music") {
    fun toPlainBossBarDescriptor(): BossBarDescriptor {
        val bossBar = BossBarDescriptor(title)
            .progress(progress)
            .color(color)
            .overlay(overlay)
            .context("music")
            .addData("isPaused", isPaused)

        bossBar.addData("type", "MusicBossBarDescriptor")
        if (genre != null) bossBar.addData("genre", genre)
        if (trackDuration != null) bossBar.addData("trackDuration", trackDuration)
        if (currentTick != null) bossBar.addData("currentTick", currentTick)
        if (icon != null) bossBar.addData("icon", icon)
        if (subTitle != null) bossBar.addData("subTitle", subTitle)

        return bossBar
    }
}
