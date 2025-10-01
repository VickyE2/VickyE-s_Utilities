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
import org.vicky.platform.utils.SoundCategory
import org.vicky.utilities.DatabaseManager.dao_s.MusicPieceDAO
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO
import java.util.*
import java.util.function.Consumer
import kotlin.math.abs

object MusicPlayer {
    private val playerStates = mutableMapOf<UUID, PlayerState>()
    private val noteUidMap = mutableMapOf<NoteKey, Integer>()
    // Java-style static map for reverse lookup (pitch → name)
    private val NOTE_ORDER = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val OCTAVE_SHIFTS = mapOf("--" to 2, "-" to 3, "" to 4, "+" to 5, "++" to 6)
    val NOTE_TO_MIDI: Map<String, Int> = buildMap {
        for ((octaveSuffix, octave) in OCTAVE_SHIFTS) {
            for ((index, noteName) in NOTE_ORDER.withIndex()) {
                val midi = octave * 12 + index
                val key = if (noteName.length > 1) {
                    // noteName like "C#": build "C{suffix}#"
                    "${noteName[0]}$octaveSuffix${noteName[1]}"
                } else {
                    // noteName like "C": build "C{suffix}"
                    "${noteName}$octaveSuffix"
                }
                put(key, midi)
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
                    )
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
                    val key = NoteKey(player.uniqueId(), event.sound?.name ?: "unknown", event.pitch, event.volume)
                    if (event.part != null) {
                        when (event.part) {
                            MusicBuilder.NotePart.OUT -> {
                                // find previously started uid and stop it
                                val uid = noteUidMap.remove(key)
                                if (uid != null) PlatformPlugin.soundBackend().stopNote(player, uid)
                                else {
                                    val name = resolveCustomSound(event)
                                    PlatformPlugin.soundBackend()
                                        .playNamed(player, name, event.category, event.volume, event.pitch)
                                }
                            }

                            else -> {
                                val uid = PlatformPlugin.soundBackend().playNote(player, event)
                                if (uid != null) noteUidMap[key] = uid
                            }
                        }
                    } else PlatformPlugin.soundBackend().playNoteFor(player, event, 0.3)
                }
            }, tickOffset)
        }

    }

    fun resolveCustomSound(event: MusicEvent): String {
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

    private fun resolvePitchName(pitch: Int): String {
        return NOTE_TO_MIDI
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
            val key = NoteKey(player.uniqueId(), event.sound?.name ?: "unknown", event.pitch, event.volume)
            if (event.part != null) {
                when (event.part) {
                    MusicBuilder.NotePart.OUT -> {
                        // find previously started uid and stop it
                        val uid = noteUidMap.remove(key)
                        if (uid != null) PlatformPlugin.soundBackend().stopNote(player, uid)
                        else {
                            val name = resolveCustomSound(event)
                            PlatformPlugin.soundBackend()
                                .playNamed(player, name, event.category, event.volume, event.pitch)
                        }
                    }
                    else -> {
                        val uid = PlatformPlugin.soundBackend().playNote(player, event)
                        if (uid != null) noteUidMap[key] = uid
                    }
                }
            } else PlatformPlugin.soundBackend().playNoteFor(player, event, 0.3)
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
    title: Component,
    subTitle: Component?,
    progress: Float = 1.0f,
    color: IColor? = null,
    overlay: BossBarOverlay? = null,
    genre: String? = null,
    var isPaused: Boolean = false,
    trackDuration: Long? = null,
    currentTick: Int? = null,
    icon: String? = null
) : BossBarDescriptor(title, subTitle, progress, color, overlay, "music") {
    init {
        addData("isPaused", isPaused)
        addData("type", "MusicBossBarDescriptor")
        if (genre != null) addData("genre", genre)
        if (trackDuration != null) addData("trackDuration", trackDuration)
        if (currentTick != null) addData("currentTick", currentTick)
        if (icon != null) addData("icon", icon)
        if (subTitle != null) addData("subTitle", subTitle)
    }
}

/**
 * Small abstraction for how we actually *play* notes.
 * playNote returns an Int uid you can later pass to stopNote (for synth/backends that need it).
 */
interface PlatformSoundBackend {
    /**
     * Play the given MusicEvent for this player. Return the event given uid for the note or your own logic.
     */
    fun playNote(player: PlatformPlayer, event: MusicEvent): Integer?

    /**
     * Play the given MusicEvent for this player for a given time in seconds.
     */
    fun playNoteFor(player: PlatformPlayer, event: MusicEvent, time: Double)

    /**
     * Stop a previously started note identified by uid.
     * If a backend doesn't use uids, it's fine to ignore or best-effort.
     */
    fun stopNote(player: PlatformPlayer, uid: Integer?)

    /**
     * Fallback to play an already-resolved sound name (useful for Bukkit/playSound usage).
     * Delegates to player.playSound or equivalent.
     */
    fun playNamed(player: PlatformPlayer, soundName: String, category: SoundCategory?, volume: Float, pitch: Int)
}

/**
 * Key used to correlate IN/MAIN -> OUT events when MusicEvent doesn't provide a unique id.
 * You can extend with more fields if your MusicEvent has a channel/voice id.
 */
data class NoteKey(val playerId: UUID, val instrument: String, val pitch: Int, val volume: Float)

data class ADSR(
    val attack: Float,
    val decay: Float,
    val sustain: Float,
    val release: Float,
    val sustainLoop: Boolean = false
)

/**
 * Very small mapping from MusicEvent.part to ADSR defaults.
 * You can replace this with per-instrument tables later.
 */
object DefaultAdsrMapper {
    fun map(event: MusicEvent): ADSR {
        return when (event.part) {
            // Quick attack, short decay, high sustain, short release
            MusicBuilder.NotePart.IN -> ADSR(
                attack = 0.005f,
                decay = 0.02f,
                sustain = 0.9f,
                release = 0.01f,
                sustainLoop = true // sustain until OUT arrives
            )

            // MAIN shouldn't retrigger, so minimal/no envelope change
            MusicBuilder.NotePart.MAIN -> ADSR(
                attack = 0.0f,
                decay = 0.0f,
                sustain = 1.0f,
                release = 0.0f,
                sustainLoop = true
            )

            // OUT kills off with short release
            MusicBuilder.NotePart.OUT -> ADSR(
                attack = 0.0f,
                decay = 0.0f,
                sustain = 0.0f,
                release = 0.3f,
                sustainLoop = false
            )

            // fallback for non-part notes
            else -> ADSR(
                attack = 0.01f,
                decay = 0.05f,
                sustain = 0.8f,
                release = 0.2f,
                sustainLoop = false
            )
        }
    }
}