/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.music.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vicky.platform.utils.SoundCategory;

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
	private static final Pattern REPEAT_PATTERN = Pattern.compile("^(.*?)[xX](\\d+)$");

	private final AtomicInteger uid = new AtomicInteger(0);
	private final Map<String, List<String>> sections = new HashMap<>();

	// ===========================================================================
	// Inner helper class for volume parsing.
	// ===========================================================================

	/**
	 * Parses a token and extracts an optional volume modifier.
	 * <p>
	 * A token may end with a volume modifier, such as:
	 * <ul>
	 * <li>"C^2" or "C>2" – volume doubled</li>
	 * <li>"D∨2" or "D<2" – volume halved</li>
	 * <li>"[C,.,D,.,E]^3" – volume tripled for block</li>
	 * </ul>
	 * If no modifier is found, volumeMultiplier is 1.0.
	 * </p>
	 * Supported symbols:
	 * <ul>
	 * <li><code>^</code> → increase</li>
	 * <li><code>∨</code> → decrease</li>
	 * </ul>
	 * <li>Tempo modifier blocks: Use "[...]" followed by:
	 * <ul>
	 * <li>"*n" – tempo x n (faster)</li>
	 * <li>"/n" – tempo ÷ n (slower)</li>
	 * </ul>
	 * For example, "[C,.,D,.,E]^2" plays the block twice as fast.</li>
	 *
	 * @param token
	 *            the musical token (note or block)
	 * @return a TokenWithVolumeAndTempo containing the stripped token and volume
	 *         multiplier.
	 */
	private static TokenWithVolumeAndTempo parseToken(String token) {
		TokenWithVolumeAndTempo result = new TokenWithVolumeAndTempo();

		Pattern volPattern = Pattern.compile("^(.*?)([\\^∨*/])(\\d+)$");
		Matcher m = volPattern.matcher(token);

		if (m.matches()) {
			result.token = m.group(1);
			int mod = Integer.parseInt(m.group(3));

			switch (m.group(2)) {
				case "^" -> result.volumeMultiplier = mod;
				case "∨" -> result.volumeMultiplier = 1.0f / mod;
				case "*" -> result.tempoMultiplier = mod; // faster
				case "/" -> result.tempoMultiplier = 1.0f / mod; // slower
			}
		} else {
			result.token = token;
			result.volumeMultiplier = 1.0f;
			result.tempoMultiplier = 1.0f;
		}

		return result;
	}

	/**
	 * This allows for registering of sections (that can later be reused) like
	 * {@code chorus = [C, E, G]*4}
	 * 
	 * @param name
	 *            the name of the section in the example chorus
	 * @param rawScore
	 *            the raw valid score to assign to the name of the section
	 * @return this {@link MusicBuilder} instance
	 */
	public MusicBuilder section(String name, String rawScore) {
		sections.put(name, tokenizeScore(rawScore));
		return this;
	}

	private List<String> expandSectionToken(String token) {
		if (!token.startsWith("@") || token.contains("["))
			return List.of(token);

		String name = token.substring(1);
		List<String> sec = sections.get(name);
		if (sec == null)
			throw new IllegalStateException("Section @" + name + " not found");
		return sec;
	}

	private static RepeatToken parseRepeat(String token) {
		Matcher m = REPEAT_PATTERN.matcher(token);
		RepeatToken out = new RepeatToken();
		if (m.matches()) {
			out.body = m.group(1);
			out.count = Integer.parseInt(m.group(2));
		} else {
			out.body = token;
		}
		return out;
	}

	public MusicBuilder transposeSection(String name, int semitones) {
		List<String> tokens = sections.get(name);
		if (tokens == null)
			throw new IllegalStateException("Section @" + name + " not found");

		List<String> shifted = new ArrayList<>();
		for (String token : tokens) {
			shifted.add(transposeToken(token, semitones));
		}
		sections.put(name, shifted);
		return this;
	}

	private String transposeToken(String token, int semitones) {
		if (token.startsWith("[") && token.endsWith("]"))
			return token; // optionally recurse
		if (token.contains("->") || token.contains(">"))
			return token; // keep progression logic simple for now

		if (token.contains("—")) {
			String[] notes = token.split("—");
			return Arrays.stream(notes).map(n -> transposeNote(n, semitones)).reduce((a, b) -> a + "—" + b)
					.orElse(token);
		}

		return transposeNote(token, semitones);
	}

	private String transposeNote(String note, int semitones) {
		int midi = noteSymbolToMidi(note);
		int shifted = midi + semitones;
		return midiToNoteSymbol(shifted);
	}

	private List<String> normalizeTokens(List<String> rawTokens) {
		List<String> out = new ArrayList<>();

		for (String rawToken : rawTokens) {
			RepeatToken repeat = parseRepeat(rawToken);

			List<String> expanded = expandSectionToken(repeat.body);
			if (expanded.size() > 1) {
				for (int i = 0; i < repeat.count; i++) {
					out.addAll(normalizeTokens(expanded));
				}
			} else {
				for (int i = 0; i < repeat.count; i++) {
					out.add(repeat.body);
				}
			}
		}

		return out;
	}

	/**
	 * Creates a MusicTrack from a score string.
	 * <p>
	 * The score string supports:
	 * </p>
	 * <ul>
	 * <li>Single notes (e.g., "C", "D-", "F+"). Optionally, each token can have a
	 * volume modifier (e.g., "D^2" or "E∨2").</li>
	 * <li>Rests represented by "."</li>
	 * <li>Groups are denoted by [...]</li>
	 * <li>Smooth progressions * Smooth progressions (e.g., "E-2>E+" or
	 * "A-2>B-4>C"). Each segment duration is expressed as a multiple of
	 * {@code noteTime}. For example, "A-2>B" means interpolate from A to B over 2 ×
	 * noteTime.</li>
	 * <li>Chords: notes separated by "—" (e.g., "E—D" for a duo, "E—D—F" for a
	 * triad, etc.).</li>
	 * <li>Volume modifiers: append {@code ^n} to increase or {@code ∨n} to decrease
	 * volume. Example: "C^2", "[C,E,G]^2"</li>
	 * <li>Tempo modifiers: append {@code *n} to speed up or {@code /n} to slow down
	 * timing. Example: "C*2", "[C,E,G]/2"</li>
	 * <li>Stretched Notes (e.g., "C->n"). The duration {@code n} is expressed in
	 * noteTime units or refers to a marker. also works for chords C—E—G->n</li>
	 * <li>Repeated Notes Repeats: append {@code xN} to repeat a token or block N
	 * times. Example: "C x2", "[C,E,G]x3"</li>
	 * <li>Markers: {@code @[name][token]} marks the current time with {@code name}
	 * and then schedules the given token. Example: "@[chorus][C]"</li>
	 * <li>Sections: denoted by @section_name and registered prior using
	 * {@link MusicBuilder#section(String name, String score)}</li>
	 * </ul>
	 * <p>
	 * This is a more structured table:
	 * </p>
	 * 
	 * <pre>{@code
	 * C                 single note
	 * C+ / C-           octave shift
	 * C#                sharp
	 * .                 rest
	 * C—E—G             chord
	 * C->24             sustained note
	 * C—E—G->24         sustained chord
	 * A-8>B             smooth progression
	 * A-8>B-6>C-4>D     smooth progression 2
	 * C^2               louder note
	 * C∨2               quieter note
	 * C*2               faster note/block
	 * C/2               slower note/block
	 * [C,D,E]           block
	 * [C,D,E]x2         repeat block
	 * &#64;intro named section
	 * &#64;[mark][C] marker at current time then note
	 * C->@mark          sustain until marker time
	 * } </pre>
	 * <p>
	 * All timing is derived from {@code noteTime}, which represents the base
	 * duration of a single note. Durations are expressed as multiples of this unit.
	 * </p>
	 * <p>
	 * </p>
	 * e.g.
	 * 
	 * <pre>
	 * {@code
	 * MusicBuilder builder = new MusicBuilder();
	 *
	 * builder.section("intro", "C,.,E,.,G,.");
	 * builder.section("chorus", "A,B,C+,.,A,B,C+");
	 * builder.section("bridge", "F,.,G,.,A,.");
	 *
	 * MusicTrack track = builder.ofScore(sound, "@intro,.,@chorusx2,.,@[solo][E-8>G+],.,@bridge", 240, 1.0f);
	 * }
	 * </pre>
	 * 
	 * @param sound
	 *            the sound to play for each note.
	 * @param score
	 *            the musical score string (tokens separated by commas).
	 * @param noteTime
	 *            the duration (in ticks) of a single base note (e.g., "C")
	 * @param baseVolume
	 *            the base volume to use for events.
	 * @apiNote in favour of using {@link MusicComposition}, use a possible builder
	 *          chain like:
	 * 
	 *          <pre>
	 * {@code
	 * var symphony1Builder = new MusicBuilder();
	 * MusicComposition comp = new MusicComposition()
	 *     .addVoice(symphony1Builder.ofScore("C,E,G,.,C", sound, 10, 1.0f))
	 *     .addVoice(symphony1Builder.ofScore("C,.,C,.,C", sound, 10, 0.7f));
	 * }
	 * </pre>
	 * 
	 * @return a MusicTrack containing the scheduled events.
	 */
	public MusicTrack ofScore(Sound sound, String score, long noteTime, float baseVolume) {
		MusicBuilder builder = new MusicBuilder();
		List<String> tokens = normalizeTokens(tokenizeScore(score));
		parseAndSchedule(tokens, 0, tokens.size(), noteTime, baseVolume, sound, builder, 0);
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
				if (c == '[')
					insideBlock = true;
				if (c == ']')
					insideBlock = false;
				buffer.append(c);
			}
		}
		if (!buffer.isEmpty())
			tokens.add(buffer.toString());
		return tokens;
	}

	private static ProgressionStep parseProgressionStep(String token) {
		Matcher m = Pattern.compile("^([A-G][#+\\-]*)(?:-(\\d+))?$").matcher(token);
		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid progression step: " + token);
		}

		ProgressionStep step = new ProgressionStep();
		step.note = m.group(1);
		step.duration = m.group(2) != null ? Integer.parseInt(m.group(2)) : -1;
		return step;
	}
	private static long handleSmoothProgression(String token, Sound sound, float volume, MusicBuilder builder,
			float tempo, long noteTime, long currentTime) {
		String[] parts = token.split(">");
		if (parts.length < 2) {
			throw new IllegalArgumentException("Invalid smooth progression: " + token);
		}

		List<ProgressionStep> steps = new ArrayList<>();
		for (String part : parts) {
			steps.add(parseProgressionStep(part));
		}

		for (int i = 0; i < steps.size() - 1; i++) {
			ProgressionStep from = steps.get(i);
			ProgressionStep to = steps.get(i + 1);

			int duration = to.duration > 0 ? to.duration : 10;
			long segmentDuration = Math.max(1L, Math.round(duration * noteTime / tempo));

			Integer startPitch = noteSymbolToMidi(from.note);
			Integer endPitch = noteSymbolToMidi(to.note);

			builder.addSmoothProgression(currentTime, sound, startPitch, endPitch, (int) segmentDuration, volume,
					SOUND_CATEGORY);

			currentTime += segmentDuration;
		}

		return currentTime;
	}

	private long handleBlock(String token, long noteTime, float baseVolume, Sound sound, MusicBuilder builder,
			float tempo, long currentTime) {
		Matcher m = BLOCK_MATCHER.matcher(token);
		if (!m.matches())
			return currentTime;

		String content = m.group(1);
		List<String> innerTokens = tokenizeScore(content);

		long totalBlockTime = (long) ((innerTokens.size() * noteTime) / tempo);
		long innerNoteTime = !innerTokens.isEmpty() ? totalBlockTime / innerTokens.size() : noteTime;

		parseAndSchedule(innerTokens, 0, innerTokens.size(), innerNoteTime, baseVolume, sound, builder, currentTime);

		return currentTime + totalBlockTime;
	}

	private static long handleChord(String token, Sound sound, float volume, MusicBuilder builder, long currentTime,
			float tempo, long noteTime) {
		String[] notes = token.split("—");
		List<Integer> pitches = Arrays.stream(notes).map(MusicBuilder::noteSymbolToMidi).toList();
		switch (pitches.size()) {
			case 2 -> builder.addDuo(currentTime, sound, pitches.get(0), pitches.get(1), volume, SOUND_CATEGORY);
			case 3 -> builder.addTriple(currentTime, sound, pitches.get(0), pitches.get(1), pitches.get(2), volume,
					SOUND_CATEGORY);
			case 4 -> builder.addQuad(currentTime, sound, pitches.get(0), pitches.get(1), pitches.get(2),
					pitches.get(3), volume, SOUND_CATEGORY);
			default -> builder.addSingle(currentTime, sound, pitches.get(0), volume, SOUND_CATEGORY);
		}
		return (long) (currentTime + (noteTime / tempo));
	}

	/**
	 * Convert a note token like "C", "C+", "A-#", "G++" to a MIDI note number. Uses
	 * the same +/- octave suffixes as your builder: -- -> octave 2, - -> octave 3,
	 * "" -> octave 4, + -> octave 5, ++ -> octave 6
	 */
	public static int noteSymbolToMidi(String noteSymbol) {
		if (noteSymbol == null || noteSymbol.isEmpty())
			throw new IllegalArgumentException("noteSymbol null/empty");
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
				case '#' :
					semitone += 1;
					break;
				case '+' :
					octave += 1;
					break;
				case '-' :
					octave -= 1;
					break;
				default :
					// ignore unknown characters or throw
			}
		}

		int noteIndex = semitone + (octave * 12); // your internal half-step index

		// map A4-index -> MIDI 69
		return noteIndex - A4_INDEX + 69;
	}

	/**
	 * Converts a MIDI note number like 43, 67 to a note symbol Uses the same +/-
	 * octave suffixes as your builder: -- -> octave 2, - -> octave 3, "" -> octave
	 * 4, + -> octave 5, ++ -> octave 6
	 */
	public static String midiToNoteSymbol(int midi) {
		int noteIndex = midi - 69 + A4_INDEX;
		int octave = noteIndex / 12;
		int semitone = noteIndex % 12;

		String base = switch (semitone) {
			case 1 -> "C#";
			case 2 -> "D";
			case 3 -> "D#";
			case 4 -> "E";
			case 5 -> "F";
			case 6 -> "F#";
			case 7 -> "G";
			case 8 -> "G#";
			case 9 -> "A";
			case 10 -> "A#";
			case 11 -> "B";
			default -> "C";
		};

		return base + switch (octave - 4) {
			case -2 -> "--";
			case -1 -> "-";
			case 1 -> "+";
			case 2 -> "++";
			default -> "";
		};
	}

	private long scheduleToken(String rawToken, long noteTime, float baseVolume, Sound sound, MusicBuilder builder,
			long currentTime) {
		String token = rawToken;

		Matcher m = MARKER_PATTERN.matcher(rawToken);
		if (m.find()) {
			String markerName = m.group(1);
			String note = m.group(2);
			markerMap.put(markerName, currentTime);
			token = note;
		}

		TokenWithVolumeAndTempo twv = parseToken(token);
		token = twv.token;
		float volume = baseVolume * twv.volumeMultiplier;
		float tempo = twv.tempoMultiplier;

		if (token.equals(".")) {
			return currentTime + (long) (noteTime / tempo);
		} else if (token.startsWith("[")) {
			return handleBlock(token, noteTime, volume, sound, builder, tempo, currentTime);
		} else if (token.contains("->")) {
			if (token.contains("—")) {
				return handleSustainedChord(token, sound, volume, builder, tempo, currentTime, noteTime);
			}
			return handleSustainedNote(token, sound, volume, builder, tempo, currentTime, noteTime);
		} else if (token.contains(">")) {
			return handleSmoothProgression(token, sound, volume, builder, tempo, noteTime, currentTime);
		} else if (token.contains("—")) {
			return handleChord(token, sound, volume, builder, currentTime, tempo, noteTime);
		} else {
			Integer pitch = noteSymbolToMidi(token);
			builder.addSingle(currentTime, sound, pitch, volume, SOUND_CATEGORY);
			return currentTime + (long) (noteTime / tempo);
		}
	}

	private void parseAndSchedule(List<String> tokens, int start, int end, long noteTime, float baseVolume, Sound sound,
			MusicBuilder builder, long currentTime) {
		for (int i = start; i < end; i++) {
			String rawToken = tokens.get(i);

			RepeatToken repeat = parseRepeat(rawToken);
			for (int r = 0; r < repeat.count; r++) {
				currentTime = scheduleToken(repeat.body, noteTime, baseVolume, sound, builder, currentTime);

				List<String> expanded = expandSectionToken(rawToken);
				if (expanded.size() > 1) {
					parseAndSchedule(expanded, 0, expanded.size(), noteTime, baseVolume, sound, builder, currentTime);
					continue;
				}
				rawToken = expanded.get(0);

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
					currentTime += (long) (noteTime / tempo);
				} else if (token.startsWith("[")) {
					currentTime = handleBlock(token, noteTime, volume, sound, builder, tempo, currentTime);
				} else if (token.contains("->")) {
					if (token.contains("—")) {
						currentTime = handleSustainedChord(token, sound, volume, builder, tempo, currentTime, noteTime);
					} else {
						currentTime = handleSustainedNote(token, sound, volume, builder, tempo, currentTime, noteTime);
					}
				} else if (token.contains(">")) {
					currentTime = handleSmoothProgression(token, sound, volume, builder, tempo, noteTime, currentTime);
				} else if (token.contains("—")) {
					currentTime = handleChord(token, sound, volume, builder, currentTime, tempo, noteTime);
				} else {
					Integer pitch = noteSymbolToMidi(token);
					builder.addSingle(currentTime, sound, pitch, volume, SOUND_CATEGORY);
					currentTime += (long) (noteTime / tempo);
				}
			}
		}
	}

	private long handleSustainedNote(String token, Sound sound, float volume, MusicBuilder builder, float tempo,
			long currentTime, long noteTime) {
		String[] parts = token.split("->");
		String fromNote = parts[0];
		String toTarget = parts[1];
		int duration;
		if (toTarget.startsWith("@")) {
			Long markerTime = markerMap.get(toTarget.substring(1));
			if (markerTime == null)
				throw new IllegalStateException("Marker @" + toTarget + " not found");
			duration = (int) (markerTime - currentTime);
		} else {
			duration = (int) (Integer.parseInt(toTarget) * noteTime / tempo);
		}
		if (fromNote.equals(".")) {
			return currentTime + duration;
		}
		Integer pitch = noteSymbolToMidi(fromNote);
		builder.addSustainedNote(currentTime, sound, pitch, volume, duration, SOUND_CATEGORY);
		return currentTime + duration;
	}

	/**
	 * Extracts the absolute time from a smooth progression token (e.g., "D-8>D+").
	 *
	 * @param progression
	 *            the progression token.
	 * @return the duration (in ticks) extracted.
	 */
	private static int extractProgressionTime(String progression) {
		Matcher m = Pattern.compile("\\d+").matcher(progression);
		return m.find() ? Integer.parseInt(m.group()) : 0;
	}

	private static final Map<Character, Integer> BASE_NOTE_INDEX = Map.of('C', 0, 'D', 2, 'E', 4, 'F', 5, 'G', 7, 'A',
			9, 'B', 11);
	private static final int A4_INDEX = 9 + (4 * 12); // A4 in half-step index

	private long handleSustainedChord(String token, Sound sound, float volume, MusicBuilder builder, float tempo,
			long currentTime, long noteTime) {
		String[] parts = token.split("->");
		String toTarget = parts[1];
		int duration;
		if (toTarget.startsWith("@")) {
			Long markerTime = markerMap.get(toTarget.substring(1));
			if (markerTime == null)
				throw new IllegalStateException("Marker @" + toTarget + " not found");
			duration = (int) (markerTime - currentTime);
		} else {
			duration = (int) (Integer.parseInt(toTarget) * noteTime / tempo);
		}
		String[] notes = parts[0].split("—");
		List<Integer> pitches = Arrays.stream(notes).map(MusicBuilder::noteSymbolToMidi).toList();
		builder.addSustainedChord(currentTime, sound, pitches, volume, duration, SOUND_CATEGORY);
		return currentTime + duration;
	}

	/**
	 * Adds a single sound event.
	 *
	 * @param timeOffset
	 *            the delay (in ticks) when the sound should play.
	 * @param sound
	 *            the sound to play.
	 * @param pitch
	 *            the pitch for the sound.
	 * @param volume
	 *            the volume for the sound.
	 * @param category
	 *            the sound category.
	 * @return this builder for chaining.
	 */
	public MusicBuilder addSingle(long timeOffset, Sound sound, Integer pitch, float volume, SoundCategory category) {
		track.addEvent(new MusicEvent(timeOffset, sound, pitch, volume, category, uid.getAndIncrement()));
		return this;
	}

	public MusicBuilder addSustainedNote(long startTime, Sound sound, Integer pitch, float volume, long durationTicks,
			SoundCategory category) {

		final int segmentSize = 8; // keep your segmentation

		// If too short, fallback to original single-shot behavior
		if (durationTicks < 3 * segmentSize) {
			return addSingle(startTime, sound, pitch, volume, category);
		}

		long inEnd = startTime + segmentSize;
		long outStart = startTime + durationTicks - segmentSize;

		// create a stable UUID for this sustained note so IN/MAIN/OUT can be correlated
		Integer noteUuid = uid.getAndIncrement();

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

	public MusicBuilder addSustainedChord(long startTime, Sound sound, List<Integer> pitches, float volume,
			long duration, SoundCategory category) {

		// Create a single UUID for the chord so OUT stops the whole chord at once
		Integer chordUuid = uid.getAndIncrement();

		// if too short for sustain, addSingle for each note
		final int segmentSize = 8;
		if (duration < 3 * segmentSize) {
			for (Integer p : pitches)
				addSingle(startTime, sound, p, volume, category);
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
	private void addSmoothProgression(long startTime, Sound sound, Integer startPitch, Integer endPitch, int duration,
			float volume, SoundCategory category) {
		int steps = 10;
		long stepDuration = duration / steps;

		for (int i = 0; i < steps; i++) {
			double t = i / (double) (steps - 1);
			Integer pitch = Math.toIntExact(Math.round(startPitch + t * (endPitch - startPitch)));
			addSingle(startTime + i * stepDuration, sound, pitch, volume, category);
		}
	}

	/**
	 * Adds a sequence of events with smooth (linear) pitch progression.
	 *
	 * @param startPitch
	 *            the starting pitch.
	 * @param endPitch
	 *            the ending pitch.
	 * @param duration
	 *            the duration (in ticks) over which to interpolate.
	 * @param sound
	 *            the sound to play.
	 * @param volume
	 *            the volume.
	 * @param category
	 *            the sound category.
	 * @param steps
	 *            the number of events to generate.
	 * @param startTime
	 *            the starting time offset.
	 * @return this builder for chaining.
	 */
	public MusicBuilder smoothProgress(Integer startPitch, Integer endPitch, long duration, Sound sound, float volume,
			SoundCategory category, int steps, long startTime) {
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
	 * @param startPitch
	 *            the starting pitch.
	 * @param endPitch
	 *            the ending pitch.
	 * @param duration
	 *            the duration (in ticks) over which to interpolate.
	 * @param sound
	 *            the sound to play.
	 * @param volume
	 *            the volume.
	 * @param category
	 *            the sound category.
	 * @param steps
	 *            the number of events.
	 * @param progression
	 *            a function that takes a linear t in [0, 1] and returns an adjusted
	 *            t.
	 * @param startTime
	 *            the starting time offset.
	 * @return this builder for chaining.
	 */
	public MusicBuilder progress(Integer startPitch, Integer endPitch, long duration, Sound sound, float volume,
			SoundCategory category, int steps, Function<Double, Double> progression, long startTime) {
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

	public MusicBuilder volumeCurve(long startTime, long duration, int steps, float from, float to, Sound sound,
			Integer pitch, SoundCategory category, Function<Double, Double> easing) {
		if (steps <= 1) {
			addSingle(startTime, sound, pitch, to, category);
			return this;
		}

		long stepDuration = duration / (steps - 1);
		for (int i = 0; i < steps; i++) {
			double t = i / (double) (steps - 1);
			double eased = easing.apply(t);
			float vol = (float) (from + eased * (to - from));
			addSingle(startTime + i * stepDuration, sound, pitch, vol, category);
		}
		return this;
	}

	/**
	 * Adds two simultaneous sound events (a duo) at the same time offset.
	 *
	 * @param timeOffset
	 *            the time offset.
	 * @param sound
	 *            the sound to play.
	 * @param pitch1
	 *            the pitch for the first note.
	 * @param pitch2
	 *            the pitch for the second note.
	 * @param volume
	 *            the volume for both.
	 * @param category
	 *            the sound category.
	 * @return this builder for chaining.
	 */
	public MusicBuilder addDuo(long timeOffset, Sound sound, Integer pitch1, Integer pitch2, float volume,
			SoundCategory category) {
		addSingle(timeOffset, sound, pitch1, volume, category);
		addSingle(timeOffset, sound, pitch2, volume, category);
		return this;
	}

	/**
	 * Adds three simultaneous sound events (a triad) at the same time offset.
	 *
	 * @param timeOffset
	 *            the time offset.
	 * @param sound
	 *            the sound to play.
	 * @param pitch1
	 *            the pitch for the first note.
	 * @param pitch2
	 *            the pitch for the second note.
	 * @param pitch3
	 *            the pitch for the third note.
	 * @param volume
	 *            the volume for all.
	 * @param category
	 *            the sound category.
	 * @return this builder for chaining.
	 */
	public MusicBuilder addTriple(long timeOffset, Sound sound, Integer pitch1, Integer pitch2, Integer pitch3,
			float volume, SoundCategory category) {
		addSingle(timeOffset, sound, pitch1, volume, category);
		addSingle(timeOffset, sound, pitch2, volume, category);
		addSingle(timeOffset, sound, pitch3, volume, category);
		return this;
	}

	/**
	 * Adds four simultaneous sound events (a quad) at the same time offset.
	 *
	 * @param timeOffset
	 *            the time offset.
	 * @param sound
	 *            the sound to play.
	 * @param pitch1
	 *            the pitch for the first note.
	 * @param pitch2
	 *            the pitch for the second note.
	 * @param pitch3
	 *            the pitch for the third note.
	 * @param pitch4
	 *            the pitch for the fourth note.
	 * @param volume
	 *            the volume for all.
	 * @param category
	 *            the sound category.
	 * @return this builder for chaining.
	 */
	public MusicBuilder addQuad(long timeOffset, Sound sound, Integer pitch1, Integer pitch2, Integer pitch3,
			Integer pitch4, float volume, SoundCategory category) {
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
	 * Holds a token string stripped of its volume modifier and the multiplier to
	 * apply.
	 */
	private static class TokenWithVolumeAndTempo {
		String token;
		float volumeMultiplier = 1.0f;
		float tempoMultiplier = 1.0f;
	}

	public enum NotePart {
		IN, MAIN, OUT
	}

	private static class RepeatToken {
		String body;
		int count = 1;
	}

	private static class ProgressionStep {
		String note;
		int duration; // duration to reach this note from previous one
	}
}
