/* Licensed under Apache-2.0 2024. */
package org.vicky.music.utils;

import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.utils.SoundCategory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MusicBuilder is a chainable builder that produces a MusicTrack.
 * <p>
 * It supports adding single events, smooth progressions, custom progressions,
 * simultaneous events (duo, triad, quad), and tempo-modified blocks.
 * </p>
 */
public class MusicBuilder {
  public static final SoundCategory SOUND_CATEGORY = SoundCategory.MUSIC;
  private final MusicTrack track = new MusicTrack();
  private final Map<String, Long> markerMap = new HashMap<>();
  private static final Pattern BLOCK_MATCHER = Pattern.compile("^\\[(.*?)]$");
  private static final Pattern MARKER_PATTERN = Pattern.compile("@\\[([a-zA-Z0-9_]+)]\\[(.*?)]");

  // ===========================================================================
  // Inner helper class for volume parsing.
  // ===========================================================================

  /**
   * Parses a token and extracts an optional volume modifier.
   * <p>
   * A token may end with a volume modifier, such as:
   * <ul>
   *   <li>"C^2" or "C>2" – volume doubled</li>
   *   <li>"D∨2" or "D<2" – volume halved</li>
   *   <li>"[C,.,D,.,E]^3" – volume tripled for block</li>
   * </ul>
   * If no modifier is found, volumeMultiplier is 1.0.
   * </p>
   * Supported symbols:
   * <ul>
   *   <li><code>^</code> → increase</li>
   *   <li><code>∨</code> → decrease</li>
   * </ul>
   *   <li>Tempo modifier blocks: Use "[...]" followed by:
   *       <ul>
   *         <li>"*n" – tempo x n (faster)</li>
   *         <li>"/n" – tempo ÷ n (slower)</li>
   *       </ul>
   *       For example, "[C,.,D,.,E]^2" plays the block twice as fast.
   *   </li>
   *
   * @param token the musical token (note or block)
   * @return a TokenWithVolumeAndTempo containing the stripped token and volume multiplier.
   */
  private static TokenWithVolumeAndTempo parseToken(String token) {
    TokenWithVolumeAndTempo result = new TokenWithVolumeAndTempo();
    // Match anything ending with ^2, ∨2, >2, <2 etc.
    Pattern volPattern = Pattern.compile("^(.*?)([\\^∨*])(\\d+)$");
    Matcher m = volPattern.matcher(token);
    if (m.matches()) {
      result.token = m.group(1);
      int mod = Integer.parseInt(m.group(3));
      switch (m.group(2)) {
        case "^" -> result.volumeMultiplier = mod; // Volume increase
        case "∨" -> result.volumeMultiplier = mod > 0 ? 1.0f / mod : 1.0f; // Volume decrease
        case "/" -> result.tempoMultiplier = mod; // Tempo increase
        case "*" -> result.tempoMultiplier = mod > 0 ? 1.0f / mod : 1.0f; // Tempo decrease
      }
    } else {
      result.token = token;
      result.volumeMultiplier = 1.0f;
      result.tempoMultiplier = 1.0f;
    }
    return result;
  }

  /**
   * Creates a MusicTrack from a score string.
   * <p>
   * The score string supports:
   * </p>
   * <ul>
   *   <li>Single notes (e.g., "C", "D-", "F+"). Optionally, each token can have a volume modifier (e.g., "D^2" or "E∨2").</li>
   *   <li>Rests represented by ".".</li>
   *   <li>Smooth progressions (e.g., "E-8>E+"). The duration is absolute.</li>
   *   <li>Chords: notes separated by "-" (e.g., "E—D" for a duo, "E—D—F" for a triad, etc.).</li>
   *   <li>Tempo modifier blocks: tokens in the form "[...]" optionally followed by a tempo multiplier.
   *       For example, "[D,.,.,.,F,.,.,.,G]^2" applies a volume modifier to every note in the block and also adjusts timing.
   *   </li>
   *   <li>Streached Notes (e.g., "C->n"). n is the number of seconds the note is stretched for. also works for chords C—E—G->n</li>
   * </ul>
   * <p>
   * The totalTime (in ticks) is divided among non-absolute events (unless overridden by absolute durations
   * or tempo modifiers). The base volume (passed as a parameter) is modified on a per-token basis.
   * </p>
   * <p></p>
   * e.g.
   * <pre>
   * {@code
   * MusicTrack maryHadALittleLamb = MusicBuilder
   *   .ofScore(Sound.BLOCK_AMETHYST_BLOCK_CHIME,
   *            "E,.,D,.,C,.,D,.,E,.,E,.,E,.,.,.,D,.,D,.,D,.,.,.,E,.,G,.,G,.,F-2>D,.,E,.,D,.,C,.,D,.,E,.,E,.,E,.,C,.,D,.,D,.,E,.,D,.,C,.,.,.,G,.,.,.,C+"
   *            216, 1.0
   *   )
   * }
   * </pre>
   * @param sound      the sound to play for each note.
   * @param score      the musical score string (tokens separated by commas).
   * @param totalTime  the total duration (in ticks) allocated for non-absolute events.
   * @param baseVolume the base volume to use for events.
   * @return a MusicTrack containing the scheduled events.
   */
  public MusicTrack ofScore(Sound sound, String score, int totalTime, float baseVolume) {
    MusicBuilder builder = new MusicBuilder();
    List<String> tokens = tokenizeScore(score);
    long interval = computeDefaultInterval(tokens, totalTime);
    parseAndSchedule(tokens, 0, tokens.size(), interval, baseVolume, sound, builder, 0);
    return builder.build();
  }

  private static List<String> tokenizeScore(String rawScore) {
    List<String> tokens = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    boolean insideBlock = false;

    for (char c : rawScore.replaceAll("\\s+", "").toCharArray()) {
      if (c == ',' && !insideBlock) {
        tokens.add(buffer.toString());
        buffer.setLength(0);
      } else {
        if (c == '[') insideBlock = true;
        if (c == ']') insideBlock = false;
        buffer.append(c);
      }
    }
    if (!buffer.isEmpty()) tokens.add(buffer.toString());
    return tokens;
  }

  private void parseAndSchedule(
      List<String> tokens,
      int start,
      int end,
      long interval,
      float baseVolume,
      Sound sound,
      MusicBuilder builder,
      long currentTime) {
    for (int i = start; i < end; i++) {
      String rawToken = tokens.get(i);
      String token = rawToken; // will modify this
      Matcher m = MARKER_PATTERN.matcher(rawToken);
      if (m.find()) {
        String markerName = m.group(1); // e.g. "chorus"
        String note = m.group(2); // e.g. "F#"
        markerMap.put(markerName, currentTime); // Store marker time
        token = note; // Strip marker from token before parsing volume/tempo
      }

      TokenWithVolumeAndTempo twv = parseToken(token); // Now it’s just "F#^2" or "F#"
      token = twv.token;
      float volume = baseVolume * twv.volumeMultiplier;
      float tempo = twv.tempoMultiplier;

      if (token.equals(".")) {
        currentTime += (long) (interval / tempo);

      } else if (token.startsWith("[")) {
        currentTime = handleBlock(token, interval, volume, sound, builder, tempo, currentTime);

      } else if (token.contains("->")) {
        if (token.contains("—")) {
          currentTime = handleSustainedChord(token, sound, volume, builder, tempo, currentTime);
        } else {
          currentTime = handleSustainedNote(token, sound, volume, builder, tempo, currentTime);
        }

      } else if (token.contains(">")) {
        currentTime = handleSmoothProgression(token, sound, volume, builder, tempo, currentTime);

      } else if (token.contains("—")) {
        currentTime = handleChord(token, sound, volume, builder, currentTime, tempo, interval);

      } else {
        float pitch = pitchFor(token);
        builder.addSingle(currentTime, sound, pitch, volume, SoundCategory.MUSIC);
        currentTime += (long) (interval / tempo);
      }
    }
  }

  private long handleBlock(
      String token,
      long interval,
      float baseVolume,
      Sound sound,
      MusicBuilder builder,
      float tempo,
      long currentTime) {
    Matcher m = BLOCK_MATCHER.matcher(token);
    if (!m.matches()) return currentTime;

    String content = m.group(1);

    List<String> innerTokens = tokenizeScore(content);
    long totalBlockTime = (long) ((innerTokens.size() * interval) / tempo);
    long innerInterval = !innerTokens.isEmpty() ? totalBlockTime / innerTokens.size() : interval;

    parseAndSchedule(
        innerTokens, 0, innerTokens.size(), innerInterval, baseVolume, sound, builder, currentTime);

    return currentTime + totalBlockTime;
  }

  private long handleSustainedNote(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      float tempo,
      long currentTime) {
    String[] parts = token.split("->");
    String fromNote = parts[0];
    String toTarget = parts[1];
    int duration;
    if (toTarget.startsWith("@")) {
      Long markerTime = markerMap.get(toTarget.substring(1));
      if (markerTime == null) throw new IllegalStateException("Marker @" + toTarget + " not found");
      duration = (int) (markerTime - currentTime);
    } else {
      duration = (int) (Integer.parseInt(toTarget) / tempo);
    }
    if (fromNote.equals(".")) {
      return currentTime + duration;
    }
    float pitch = pitchFor(fromNote);
    builder.addSustainedNote(currentTime, sound, pitch, volume, duration, SoundCategory.MUSIC);
    return currentTime + duration;
  }

  private long handleSustainedChord(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      float tempo,
      long currentTime) {
    String[] parts = token.split("->");
    String toTarget = parts[1];
    int duration;
    if (toTarget.startsWith("@")) {
      Long markerTime = markerMap.get(toTarget.substring(1));
      if (markerTime == null) throw new IllegalStateException("Marker @" + toTarget + " not found");
      duration = (int) (markerTime - currentTime);
    } else {
      duration = (int) (Integer.parseInt(toTarget) / tempo);
    }
    String[] notes = parts[0].split("—");
    List<Float> pitches = Arrays.stream(notes).map(MusicBuilder::pitchFor).toList();
    builder.addSustainedChord(currentTime, sound, pitches, volume, duration, SoundCategory.MUSIC);
    return currentTime + duration;
  }

  private static long handleSmoothProgression(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      float tempo,
      long currentTime) {
    String[] parts = token.split(">");
    int duration = (int) (extractProgressionTime(parts[0]) / tempo);
    float start = pitchFor(parts[0].replaceAll("\\d+", ""));
    float end = pitchFor(parts[1]);
    builder.addSmoothProgression(currentTime, sound, start, end, duration, SoundCategory.MUSIC);
    return currentTime + duration;
  }

  private static long handleChord(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      long currentTime,
      float tempo,
      long interval) {
    String[] notes = token.split("—");
    List<Float> pitches = Arrays.stream(notes).map(MusicBuilder::pitchFor).toList();
    switch (pitches.size()) {
      case 2 ->
          builder.addDuo(
              currentTime, sound, pitches.get(0), pitches.get(1), volume, SoundCategory.MUSIC);
      case 3 ->
          builder.addTriple(
              currentTime,
              sound,
              pitches.get(0),
              pitches.get(1),
              pitches.get(2),
              volume,
              SOUND_CATEGORY);
      case 4 ->
          builder.addQuad(
              currentTime,
              sound,
              pitches.get(0),
              pitches.get(1),
              pitches.get(2),
              pitches.get(3),
              volume,
              SoundCategory.MUSIC);
      default -> builder.addSingle(currentTime, sound, pitches.get(0), volume, SoundCategory.MUSIC);
    }
    return (long) (currentTime + (interval / tempo));
  }

  private static long computeDefaultInterval(List<String> tokens, int totalTime) {
    int absoluteTime = 0;
    int slotCount = 0;

    for (String rawToken : tokens) {
      String token = parseToken(rawToken).token;
      if (token.contains("->") && !token.contains("—")) {
        absoluteTime += extractProgressionTime(token);
      } else if (!token.equals(".")) {
        slotCount++;
      }
    }
    return slotCount > 0 ? (totalTime - absoluteTime) / slotCount : 0;
  }

  /**
   * Extracts the absolute time from a smooth progression token (e.g., "D-8>D+").
   *
   * @param progression the progression token.
   * @return the duration (in ticks) extracted.
   */
  private static int extractProgressionTime(String progression) {
    Matcher m = Pattern.compile("\\d+").matcher(progression);
    return m.find() ? Integer.parseInt(m.group()) : 0;
  }

  private static final Map<Character, Integer> BASE_NOTE_INDEX =
      Map.of('C', 0, 'D', 2, 'E', 4, 'F', 5, 'G', 7, 'A', 9, 'B', 11);
  private static final int A4_INDEX = 9 + (4 * 12); // A4 in half-step index

  /**
   * Returns a pitch value based on the given note symbol.
   * <p>
   * Supports standard musical notes:
   * </p>
   * <ul>
   *   <li>C – base pitch 0.5f</li>
   *   <li>D – base pitch 0.6f</li>
   *   <li>E – base pitch 0.7f</li>
   *   <li>F – base pitch 0.8f</li>
   *   <li>G – base pitch 0.9f</li>
   *   <li>A – base pitch 1.0f</li>
   *   <li>B – base pitch 1.1f</li>
   *   <li>C+ – base pitch 1.2f (octave up)</li>
   * </ul>
   * <p>
   * The '+' modifier increases octave by 1 and '-' decreases it by 1.
   * </p>
   *
   * @param noteSymbol the note symbol.
   * @return the computed pitch.
   */
  public static float pitchFor(String noteSymbol) {
    noteSymbol = noteSymbol.toUpperCase();

    char noteChar = noteSymbol.charAt(0);
    if (!BASE_NOTE_INDEX.containsKey(noteChar)) {
      PlatformPlugin.logger().warn("Invalid note: " + noteChar + " will use base C...");
      return 0;
    }

    int semitone = BASE_NOTE_INDEX.get(noteChar);
    int octave = 4; // default to octave 4
    int index = 1;

    while (index < noteSymbol.length()) {
      char c = noteSymbol.charAt(index++);
      switch (c) {
        case '#' -> semitone += 1;
        // case 'B' -> semitone -= 1; // Optional: if you want to support 'B' for flat
        case '+' -> octave += 1;
        case '-' -> octave -= 1;
      }
    }

    int noteIndex = semitone + (octave * 12);
    int halfStepsFromA4 = noteIndex - A4_INDEX;

    return (float) Math.pow(2.0, halfStepsFromA4 / 12.0);
  }

  /**
   * Adds a single sound event.
   *
   * @param timeOffset the delay (in ticks) when the sound should play.
   * @param sound      the sound to play.
   * @param pitch      the pitch for the sound.
   * @param volume     the volume for the sound.
   * @param category   the sound category.
   * @return this builder for chaining.
   */
  public MusicBuilder addSingle(
      long timeOffset, Sound sound, float pitch, float volume, SoundCategory category) {
    track.addEvent(new MusicEvent(timeOffset, sound, pitch, volume, category));
    return this;
  }

  public MusicBuilder addSustainedNote(
      long startTime,
      Sound sound,
      float pitch,
      float volume,
      long durationTicks,
      SoundCategory category) {
    final int segmentSize = 8; // Each part is 9 ticks but 1 tick loop

    // If too short, fallback to original behavior
    if (durationTicks < 3 * segmentSize) {
      return addSingle(startTime, sound, pitch, volume, category);
    }

    long inEnd = startTime + segmentSize;
    long outStart = startTime + durationTicks - segmentSize;

    // Schedule IN every 7 ticks
    track.addEvent(new MusicEvent(startTime, sound, pitch, volume, category, NotePart.IN));

    // Schedule MAIN every 7 ticks between inEnd and outStart
    for (long tick = inEnd; tick < outStart; tick += segmentSize) {
      track.addEvent(new MusicEvent(tick, sound, pitch, volume, category, NotePart.MAIN));
    }

    // Schedule OUT at the end
    track.addEvent(new MusicEvent(outStart, sound, pitch, volume, category, NotePart.OUT));

    return this;
  }

  public MusicBuilder addSustainedChord(
      long startTime,
      Sound sound,
      List<Float> pitches,
      float volume,
      long duration,
      SoundCategory category) {
    for (float pitch : pitches) {
      addSustainedNote(startTime, sound, pitch, volume, duration, category);
    }
    return this;
  }

  // Placeholder: smooth progression method – implement as needed.
  private void addSmoothProgression(
      long startTime,
      Sound sound,
      float startPitch,
      float endPitch,
      int duration,
      SoundCategory category) {
    // For example, you might break the duration into smaller steps and call addSingle for each.
    int steps = 10; // example steps
    long stepDuration = duration / steps;
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      float pitch = (float) (startPitch + t * (endPitch - startPitch));
      addSingle(startTime + i * stepDuration, sound, pitch, 1.0f, category);
    }
  }

  /**
   * Adds a sequence of events with smooth (linear) pitch progression.
   *
   * @param startPitch the starting pitch.
   * @param endPitch   the ending pitch.
   * @param duration   the duration (in ticks) over which to interpolate.
   * @param sound      the sound to play.
   * @param volume     the volume.
   * @param category   the sound category.
   * @param steps      the number of events to generate.
   * @param startTime  the starting time offset.
   * @return this builder for chaining.
   */
  public MusicBuilder smoothProgress(
      double startPitch,
      double endPitch,
      long duration,
      Sound sound,
      float volume,
      SoundCategory category,
      int steps,
      long startTime) {
    if (steps <= 1) {
      addSingle(startTime, sound, (float) startPitch, volume, category);
      return this;
    }
    long stepDuration = duration / (steps - 1);
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      double pitch = startPitch + t * (endPitch - startPitch);
      long eventTime = startTime + i * stepDuration;
      addSingle(eventTime, sound, (float) pitch, volume, category);
    }
    return this;
  }

  /**
   * Adds a sequence of events with a custom progression equation for pitch.
   *
   * @param startPitch  the starting pitch.
   * @param endPitch    the ending pitch.
   * @param duration    the duration (in ticks) over which to interpolate.
   * @param sound       the sound to play.
   * @param volume      the volume.
   * @param category    the sound category.
   * @param steps       the number of events.
   * @param progression a function that takes a linear t in [0, 1] and returns an adjusted t.
   * @param startTime   the starting time offset.
   * @return this builder for chaining.
   */
  public MusicBuilder progress(
      double startPitch,
      double endPitch,
      long duration,
      Sound sound,
      float volume,
      SoundCategory category,
      int steps,
      Function<Double, Double> progression,
      long startTime) {
    if (steps <= 1) {
      addSingle(startTime, sound, (float) startPitch, volume, category);
      return this;
    }
    long stepDuration = duration / (steps - 1);
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      double adjusted = progression.apply(t);
      double pitch = startPitch + adjusted * (endPitch - startPitch);
      long eventTime = startTime + i * stepDuration;
      addSingle(eventTime, sound, (float) pitch, volume, category);
    }
    return this;
  }

  /**
   * Adds two simultaneous sound events (a duo) at the same time offset.
   *
   * @param timeOffset the time offset.
   * @param sound      the sound to play.
   * @param pitch1     the pitch for the first note.
   * @param pitch2     the pitch for the second note.
   * @param volume     the volume for both.
   * @param category   the sound category.
   * @return this builder for chaining.
   */
  public MusicBuilder addDuo(
      long timeOffset,
      Sound sound,
      float pitch1,
      float pitch2,
      float volume,
      SoundCategory category) {
    addSingle(timeOffset, sound, pitch1, volume, category);
    addSingle(timeOffset, sound, pitch2, volume, category);
    return this;
  }

  /**
   * Adds three simultaneous sound events (a triad) at the same time offset.
   *
   * @param timeOffset the time offset.
   * @param sound      the sound to play.
   * @param pitch1     the pitch for the first note.
   * @param pitch2     the pitch for the second note.
   * @param pitch3     the pitch for the third note.
   * @param volume     the volume for all.
   * @param category   the sound category.
   * @return this builder for chaining.
   */
  public MusicBuilder addTriple(
      long timeOffset,
      Sound sound,
      float pitch1,
      float pitch2,
      float pitch3,
      float volume,
      SoundCategory category) {
    addSingle(timeOffset, sound, pitch1, volume, category);
    addSingle(timeOffset, sound, pitch2, volume, category);
    addSingle(timeOffset, sound, pitch3, volume, category);
    return this;
  }

  /**
   * Adds four simultaneous sound events (a quad) at the same time offset.
   *
   * @param timeOffset the time offset.
   * @param sound      the sound to play.
   * @param pitch1     the pitch for the first note.
   * @param pitch2     the pitch for the second note.
   * @param pitch3     the pitch for the third note.
   * @param pitch4     the pitch for the fourth note.
   * @param volume     the volume for all.
   * @param category   the sound category.
   * @return this builder for chaining.
   */
  public MusicBuilder addQuad(
      long timeOffset,
      Sound sound,
      float pitch1,
      float pitch2,
      float pitch3,
      float pitch4,
      float volume,
      SoundCategory category) {
    addSingle(timeOffset, sound, pitch1, volume, category);
    addSingle(timeOffset, sound, pitch2, volume, category);
    addSingle(timeOffset, sound, pitch3, volume, category);
    addSingle(timeOffset, sound, pitch4, volume, category);
    return this;
  }

  /**
   * Builds and returns the MusicTrack.
   *
   * @return the constructed MusicTrack.
   */
  public MusicTrack build() {
    return track;
  }

  /**
   * Holds a token string stripped of its volume modifier and the multiplier to apply.
   */
  private static class TokenWithVolumeAndTempo {
    String token;
    float volumeMultiplier = 1.0f;
    float tempoMultiplier = 1.0f;
  }

  public enum NotePart {
    IN,
    MAIN,
    OUT
  }
}
