/* Licensed under Apache-2.0 2024. */
package org.vicky.music.utils;

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

  private static long handleSmoothProgression(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      float tempo,
      long currentTime) {
    String[] parts = token.split(">");
    int duration = (int) (extractProgressionTime(parts[0]) / tempo);
    Integer start = noteSymbolToMidi(parts[0].replaceAll("\\d+", ""));
    Integer end = noteSymbolToMidi(parts[1]);
    builder.addSmoothProgression(currentTime, sound, start, end, duration, SoundCategory.MUSIC);
    return currentTime + duration;
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

  private static long handleChord(
      String token,
      Sound sound,
      float volume,
      MusicBuilder builder,
      long currentTime,
      float tempo,
      long interval) {
    String[] notes = token.split("—");
    List<Integer> pitches = Arrays.stream(notes).map(MusicBuilder::noteSymbolToMidi).toList();
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

  /**
   * Convert a note token like "C", "C+", "A-#", "G++" to a MIDI note number.
   * Uses the same +/- octave suffixes as your builder:
   *  -- -> octave 2, - -> octave 3, "" -> octave 4, + -> octave 5, ++ -> octave 6
   */
  public static int noteSymbolToMidi(String noteSymbol) {
    if (noteSymbol == null || noteSymbol.isEmpty()) throw new IllegalArgumentException("noteSymbol null/empty");
    noteSymbol = noteSymbol.toUpperCase();

    // Count octave shift suffix characters (+ or - repeated)
    int idx = 1;
    char noteChar = noteSymbol.charAt(0);
    if (!BASE_NOTE_INDEX.containsKey(noteChar)) {
      throw new IllegalArgumentException("Invalid note: " + noteSymbol);
    }

    int semitone = BASE_NOTE_INDEX.get(noteChar);
    int octave = 4; // default (matches your current builder)
    // parse rest of string for sharps and octave markers
    while (idx < noteSymbol.length()) {
      char c = noteSymbol.charAt(idx++);
      switch (c) {
        case '#':
          semitone += 1;
          break;
        case '+':
          octave += 1;
          break;
        case '-':
          octave -= 1;
          break;
        default:
          // ignore unknown characters or throw
      }
    }

    int noteIndex = semitone + (octave * 12);           // your internal half-step index
    int midi = noteIndex - A4_INDEX + 69;               // map A4-index -> MIDI 69
    return midi;
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
        Integer pitch = noteSymbolToMidi(token);
        builder.addSingle(currentTime, sound, pitch, volume, SoundCategory.MUSIC);
        currentTime += (long) (interval / tempo);
      }
    }
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
    Integer pitch = noteSymbolToMidi(fromNote);
    builder.addSustainedNote(currentTime, sound, pitch, volume, duration, SoundCategory.MUSIC);
    return currentTime + duration;
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
    List<Integer> pitches = Arrays.stream(notes).map(MusicBuilder::noteSymbolToMidi).toList();
    builder.addSustainedChord(currentTime, sound, pitches, volume, duration, SoundCategory.MUSIC);
    return currentTime + duration;
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
          long timeOffset, Sound sound, Integer pitch, float volume, SoundCategory category) {
    track.addEvent(new MusicEvent(timeOffset, sound, pitch, volume, category));
    return this;
  }

  public MusicBuilder addSustainedNote(
          long startTime,
          Sound sound,
          Integer pitch,
          float volume,
          long durationTicks,
          SoundCategory category) {

    final int segmentSize = 8; // keep your segmentation

    // If too short, fallback to original single-shot behavior
    if (durationTicks < 3 * segmentSize) {
      return addSingle(startTime, sound, pitch, volume, category);
    }

    long inEnd = startTime + segmentSize;
    long outStart = startTime + durationTicks - segmentSize;

    // create a stable UUID for this sustained note so IN/MAIN/OUT can be correlated
    UUID noteUuid = UUID.randomUUID();

    // create IN event with the noteId
    MusicEvent inEvent = new MusicEvent(startTime, sound, pitch, volume, category, NotePart.IN, noteUuid);
    track.addEvent(inEvent);

    // MAIN events: reuse same noteId
    for (long tick = inEnd; tick < outStart; tick += segmentSize) {
      MusicEvent mainEvent = new MusicEvent(tick, sound, pitch, volume, category, NotePart.MAIN, noteUuid);
      track.addEvent(mainEvent);
    }

    // OUT event with same noteId
    MusicEvent outEvent = new MusicEvent(outStart, sound, pitch, volume, category, NotePart.OUT, noteUuid);
    track.addEvent(outEvent);

    return this;
  }

  public MusicBuilder addSustainedChord(
          long startTime,
          Sound sound,
          List<Integer> pitches,
          float volume,
          long duration,
          SoundCategory category) {

    // Create a single UUID for the chord so OUT stops the whole chord at once
    UUID chordUuid = UUID.randomUUID();

    // if too short for sustain, addSingle for each note
    final int segmentSize = 8;
    if (duration < 3 * segmentSize) {
      for (Integer p : pitches) addSingle(startTime, sound, p, volume, category);
      return this;
    }

    long inEnd = startTime + segmentSize;
    long outStart = startTime + duration - segmentSize;

    // Add IN events for each pitch with the same chordUuid
    for (Integer p : pitches) {
      track.addEvent(new MusicEvent(startTime, sound, p, volume, category, NotePart.IN, chordUuid));
    }

    // MAIN events
    for (long tick = inEnd; tick < outStart; tick += segmentSize) {
      for (Integer p : pitches) {
        track.addEvent(new MusicEvent(tick, sound, p, volume, category, NotePart.MAIN, chordUuid));
      }
    }

    // OUT events
    for (Integer p : pitches) {
      track.addEvent(new MusicEvent(outStart, sound, p, volume, category, NotePart.OUT, chordUuid));
    }

    return this;
  }


  // Placeholder: smooth progression method – implement as needed.
  private void addSmoothProgression(
      long startTime,
      Sound sound,
      Integer startPitch,
      Integer endPitch,
      int duration,
      SoundCategory category) {
    // For example, you might break the duration into smaller steps and call addSingle for each.
    int steps = 10; // example steps
    long stepDuration = duration / steps;
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      Integer pitch = Math.toIntExact(Math.round(startPitch + t * (endPitch - startPitch)));
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
          Integer startPitch,
          Integer endPitch,
      long duration,
      Sound sound,
      float volume,
      SoundCategory category,
      int steps,
      long startTime) {
    if (steps <= 1) {
      addSingle(startTime, sound, startPitch, volume, category);
      return this;
    }
    long stepDuration = duration / (steps - 1);
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      Integer pitch = (int) (startPitch + t * (endPitch - startPitch));
      long eventTime = startTime + i * stepDuration;
      addSingle(eventTime, sound, pitch, volume, category);
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
          Integer startPitch,
          Integer endPitch,
      long duration,
      Sound sound,
      float volume,
      SoundCategory category,
      int steps,
      Function<Double, Double> progression,
      long startTime) {
    if (steps <= 1) {
      addSingle(startTime, sound, startPitch, volume, category);
      return this;
    }
    long stepDuration = duration / (steps - 1);
    for (int i = 0; i < steps; i++) {
      double t = i / (double) (steps - 1);
      double adjusted = progression.apply(t);
      Integer pitch = (int) (startPitch + adjusted * (endPitch - startPitch));
      long eventTime = startTime + i * stepDuration;
      addSingle(eventTime, sound, pitch, volume, category);
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
      Integer pitch1,
      Integer pitch2,
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
      Integer pitch1,
      Integer pitch2,
      Integer pitch3,
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
      Integer pitch1,
      Integer pitch2,
      Integer pitch3,
      Integer pitch4,
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
