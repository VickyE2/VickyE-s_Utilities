import argparse
import json
import os
import time

"""from pyfluidsynth"""
import fluidsynth
import numpy
import numpy as np
from pydub import AudioSegment
from pydub.utils import which

sounds_json = {}
parser = argparse.ArgumentParser(description="Generate Minecraft soundpack from SoundFont.")
parser.add_argument(
    "--out", "-o",
    required=False,
    default="./output/soundgen",
    help="Base output directory for generated pack (will be converted to absolute path)."
)
args = parser.parse_args()


def remove_dc_offset(samples: np.ndarray) -> np.ndarray:
    """Remove DC offset per channel. Input shape: (n_frames, channels)."""
    if samples.size == 0:
        return samples
    means = samples.astype(np.float64).mean(axis=0)
    samples = samples.astype(np.float64) - means
    samples = np.clip(samples, -32768, 32767)
    return samples.astype(np.int16)


def make_loopable(audio: AudioSegment, loop_ms: int = 400, crossfade_ms: int = 30,
                  db_threshold: float = 1.5, max_phase_shift_ms: int = 10, zero_tol: int = 50):
    """
    Improved loop maker:
    - finds stable region,
    - phase-aligns head/tail by searching small circular shifts,
    - tries to find matching zero-crossings after alignment,
    - if none found, makes a symmetric crossfade using a mirrored head,
    - applies a tiny fade-in/out to hide micro-steps.
    Returns (AudioSegment loop_audio, bytes interleaved)
    """
    sr = audio.frame_rate
    channels = audio.channels
    loop_samples = int((loop_ms / 1000.0) * sr)
    crossfade_samples = int((crossfade_ms / 1000.0) * sr)
    window_step = max(256, crossfade_samples)

    raw = np.frombuffer(audio.raw_data, dtype=np.int16)
    if channels > 1:
        samples = raw.reshape((-1, channels))
    else:
        samples = raw.reshape((-1, 1))

    if samples.shape[0] < loop_samples + crossfade_samples:
        print("⚠️ audio too short for requested loop + crossfade; returning original")
        return audio, raw.tobytes()

    # remove dc
    samples = remove_dc_offset(samples)

    # find most stable region (same as before)
    def rms_db(segment):
        rms = np.sqrt(np.mean(segment.astype(np.float64) ** 2, axis=0))
        db = 20 * np.log10(np.maximum(rms.mean(), 1e-10))
        return db

    min_range = float("inf")
    best_start = 0
    for i in range(0, samples.shape[0] - loop_samples, window_step):
        seg = samples[i:i + loop_samples]
        sub = np.array_split(seg, 4)
        dbs = [rms_db(s) for s in sub if len(s) > 0]
        if not dbs:
            continue
        db_range = max(dbs) - min(dbs)
        if db_range < min_range:
            min_range = db_range
            best_start = i

    if min_range > db_threshold:
        print("⚠️ No ideal dB flat loop, using closest match. (min_range=", min_range, ")")

    seg = samples[best_start:best_start + loop_samples].copy()  # shape (L, channels)

    # Helper: find zero-crossing indices in 1D array (on left channel)
    def zero_crossings(arr1d):
        return np.where(np.diff(np.sign(arr1d)))[0]

    # Phase-align head/tail by searching small shifts that minimize L2 error.
    # We'll compare left channel only for speed but apply full-channel roll.
    L = seg.shape[0]
    head = seg[:crossfade_samples, 0] if crossfade_samples > 0 else seg[:1, 0]
    tail = seg[-crossfade_samples:, 0] if crossfade_samples > 0 else seg[-1:, 0]

    # max shift in samples (cap to small window)
    max_shift = min(int((max_phase_shift_ms / 1000.0) * sr), crossfade_samples // 2, 200)
    best_shift = 0
    best_err = None

    if crossfade_samples > 4 and max_shift >= 0:
        # search shifts from -max_shift..+max_shift
        for s in range(-max_shift, max_shift + 1):
            # roll head by s (positive s moves head forward)
            rolled_head = np.roll(head, s)
            err = np.sum((rolled_head.astype(np.float64) - tail.astype(np.float64)) ** 2)
            if best_err is None or err < best_err:
                best_err = err
                best_shift = s

        if best_shift != 0:
            # apply the same roll to the entire segment (circular)
            seg = np.roll(seg, -best_shift, axis=0)  # negative to align tail->head
            # re-calc head/tail after roll
            head = seg[:crossfade_samples, 0]
            tail = seg[-crossfade_samples:, 0]

    # Try to find matching zero crossings after alignment
    zc = zero_crossings(seg[:, 0])
    chosen = None
    for i in range(len(zc) - 1):
        s_idx = zc[i]
        for j in range(i + 1, len(zc)):
            e_idx = zc[j]
            if e_idx - s_idx >= loop_samples:
                # check amplitude similarity at endpoints (left channel)
                val_s = int(seg[s_idx, 0])
                val_e = int(seg[e_idx % seg.shape[0], 0])
                if abs(val_s - val_e) <= zero_tol:
                    chosen = (s_idx, s_idx + loop_samples)
                    break
        if chosen:
            break

    if chosen:
        start_idx, end_idx = chosen
        loop_region = seg[start_idx:end_idx]
        # ensure exact length
        loop_region = loop_region[:loop_samples]
    else:
        # Fallback: symmetric crossfade using mirrored head
        # Build mirrored_head = reversed copy of head
        if crossfade_samples <= 0:
            # trivial fallback
            loop_region = seg[:loop_samples]
        else:
            head_portion = seg[:crossfade_samples].copy()
            tail_portion = seg[-crossfade_samples:].copy()

            # mirror head (reverse in time)
            mirrored_head = head_portion[::-1]

            # blend tail <- mirrored_head (so tail tends toward mirrored head)
            w = (np.arange(crossfade_samples) / (crossfade_samples - 1)).reshape((crossfade_samples, 1))
            blended_tail = (tail_portion.astype(np.float64) * (1.0 - w) + mirrored_head.astype(np.float64) * w)
            blended_tail = np.clip(blended_tail, -32768, 32767).astype(np.int16)

            # assemble: seg body without tail + blended_tail
            body = seg[:-crossfade_samples]
            assembled = np.concatenate([body, blended_tail], axis=0)

            # now crossfade assembled tail into head to create final circular seam
            left = assembled[-crossfade_samples:]
            right = assembled[:crossfade_samples]
            w2 = (np.arange(crossfade_samples) / (crossfade_samples - 1)).reshape((crossfade_samples, 1))
            cross = (left.astype(np.float64) * (1.0 - w2) + right.astype(np.float64) * w2)
            cross = np.clip(cross, -32768, 32767).astype(np.int16)

            # final loop region: take first L samples of [assembled[:-CF], cross, assembled[CF:]]
            # simpler: create final array by replacing the tail with cross and taking first L frames
            assembled[-crossfade_samples:] = cross
            loop_region = assembled[:loop_samples]

    # Apply tiny fade in/out (per-channel) to further hide micro-steps (5-12 ms)
    tiny_fade_ms = max(6, int(crossfade_ms // 5))
    tiny = int((tiny_fade_ms / 1000.0) * sr)
    if tiny > 0 and loop_region.shape[0] > 2 * tiny:
        # per-channel fades
        fade_in = np.linspace(0.0, 1.0, tiny).reshape((tiny, 1))
        fade_out = np.linspace(1.0, 0.0, tiny).reshape((tiny, 1))
        lr = loop_region.astype(np.float64)
        lr[:tiny] *= fade_in
        lr[-tiny:] *= fade_out
        loop_region = np.clip(lr, -32768, 32767).astype(np.int16)

    # convert to bytes (interleaved)
    interleaved = loop_region.reshape(-1).tobytes()
    loop_audio = AudioSegment(
        data=interleaved,
        sample_width=2,
        frame_rate=sr,
        channels=channels
    )
    return loop_audio, interleaved


def extract_stable_zero_cross_loop(audio: AudioSegment, loop_ms: int = 400, window_ms: int = 20,
                                   db_threshold: float = 1.5) -> AudioSegment:
    sample_rate = audio.frame_rate
    samples = np.array(audio.get_array_of_samples()).reshape((-1, audio.channels))
    loop_sample_count = int((loop_ms / 1000.0) * sample_rate)
    window_size = int((window_ms / 1000.0) * sample_rate)

    left_channel = samples[:, 0]

    def rms_to_db(rms):
        return 20 * np.log10(np.maximum(rms, 1e-10))  # avoid log(0)

    # Slide a window and find the most volume-stable zone
    min_rms_delta = float("inf")
    best_start = 0

    if np.max(np.abs(left_channel)) < 500:
        print("⚠️ Signal too weak to loop cleanly.")
        return audio

    for i in range(0, len(left_channel) - loop_sample_count, window_size):
        segment = left_channel[i:i + loop_sample_count]
        if len(segment) < loop_sample_count:
            continue

        subwindow_size = loop_sample_count // 4
        subwindows = [segment[j:j + subwindow_size] for j in
                      range(0, loop_sample_count - subwindow_size, subwindow_size)]
        sub_dbs = []
        for w in subwindows:
            if len(w) == 0:
                continue
            rms = np.sqrt(np.mean(w ** 2))
            db = rms_to_db(rms)
            sub_dbs.append(db)

        if not sub_dbs:
            continue  # skip this candidate

        db_range = max(sub_dbs) - min(sub_dbs)
        if db_range < min_rms_delta:
            best_start = i
            min_rms_delta = db_range

    if min_rms_delta > db_threshold:
        print("⚠️ No ideal dB flat loop, but using closest match.")

    stable_region = samples[best_start:best_start + loop_sample_count]
    left = stable_region[:, 0]
    zc = np.where(np.diff(np.sign(left)))[0]

    for i in range(len(zc) - 1):
        start = zc[i]
        for j in range(i + 1, len(zc)):
            end = zc[j]
            if end - start >= loop_sample_count:
                loop = stable_region[start:end]
                if audio.channels == 2:
                    right = stable_region[:, 1]
                    if abs(right[start]) > 500 or abs(right[end]) > 500:
                        continue
                loop = loop[:loop_sample_count]
                data = loop.astype(np.int16).flatten().tobytes()
                return AudioSegment(
                    data=data,
                    sample_width=2,
                    frame_rate=sample_rate,
                    channels=audio.channels
                )

    print("⚠️ Could not find stable dB + zero-cross loop region.")
    return audio


def get_note_samples(fs, note, attack, sustain, release, channel=0, velocity=120, use_pedal=True):
    """
    Capture attack, sustain, and release buffers while using sustain pedal to lock sustain.
    - attack, sustain, release are durations in seconds.
    - If use_pedal=True, we hold the pedal during sustain and release it to trigger the envelope release.
    """
    # Ensure channel is quiet (optional)
    # fs.cc(channel, 123, 0)  # all notes off (controller 123)

    # 1) Note on
    fs.noteon(channel, note, velocity)

    # 2) Engage sustain pedal (hold the note even after noteoff)
    if use_pedal:
        fs.cc(channel, 64, 127)  # sustain ON

    # 3) Capture attack part
    attack_samples = fs.get_samples(int(SAMPLE_RATE * attack))

    # 4) Capture very long sustain slice (choose long duration if you want "infinite" sustain)
    sustain_samples = fs.get_samples(int(SAMPLE_RATE * sustain))

    # 5) Tell synth the note ended (noteoff) while pedal still down — sound remains
    fs.noteoff(channel, note)

    # 6) Release the pedal now to let the envelope go into its release phase
    if use_pedal:
        fs.cc(channel, 64, 0)  # sustain OFF -> triggers release for the voice(s) held by pedal

    # 7) Capture release tail
    release_samples = fs.get_samples(int(SAMPLE_RATE * release))

    # Convert to numpy arrays (pyFluidSynth returns lists)
    return (
        numpy.array(attack_samples),
        numpy.array(sustain_samples),
        numpy.array(release_samples)
    )


SAMPLE_RATE = 44100

AudioSegment.converter = which("ffmpeg")

# Directory setup
BASE_OUTPUT_ROOT = os.path.abspath(args.out)
print(f"Using output root: {BASE_OUTPUT_ROOT}")

# Keep your asset layout relative to BASE_OUTPUT_ROOT
base_output_dir = os.path.join(BASE_OUTPUT_ROOT, "assets", "vicky_music")
base_assets_dir = os.path.join(BASE_OUTPUT_ROOT)

# Ensure top-level output dirs exist before doing anything heavy
try:
    os.makedirs(base_output_dir, exist_ok=True)  # creates ./assets/vicky_music
    os.makedirs(base_assets_dir, exist_ok=True)  # ensures root exists too
except OSError as e:
    print(f"❌ Could not create output directories at {BASE_OUTPUT_ROOT}: {e}")
    raise SystemExit(1)

mcpack = """
{
  "pack": {
    "pack_format": 12,
    "description": "§6§l🎵 Vicky's Instrumental Symphony 🎼\nHand-crafted MIDI magic turned into real Minecraft soundwaves.\nIncludes piano, strings, brass, and more!"
  }
}
"""

os.makedirs(base_output_dir, exist_ok=True)

# Instrument map (example, extend as needed)
INSTRUMENTS = {
    "piano": (0, 0),
    "rhodes_piano": (0, 4),
    "chorused_piano": (0, 5),
    "acoustic_steel": (0, 25),
    "over_driven": (0, 29),
    "distortion": (0, 30),
    "violin": (0, 40),
    "viola": (0, 41),
    "cello": (0, 42),
    "strings": (0, 45),
    "harp": (0, 46),
    "sax": (0, 65),
    "flute": (0, 73),
    "pan_flute": (0, 75),
    # "shakuhachi": (0, 77),
    "guitar": (0, 24),
    "trumpet": (0, 56),
    "trombone": (0, 57),
    "brass": (0, 61),
    "muted_trumpet": (0, 59),
    "lead_chiff": (0, 83),
    "lead_bass": (0, 87),
}

# Note to MIDI map (simplified, assuming C4 = 60)
NOTE_MIDI_MAP = {
    'C--': 0, 'D--': 2, 'E--': 4, 'F--': 5, 'G--': 7, 'A--': 9, 'B--': 11,
    'C-': 24, 'D-': 26, 'E-': 28, 'F-': 29, 'G-': 31, 'A-': 33, 'B-': 35,
    'C': 36, 'D': 38, 'E': 40, 'F': 41, 'G': 43, 'A': 45, 'B': 47,
    'C+': 48, 'D+': 50, 'E+': 52, 'F+': 53, 'G+': 55, 'A+': 57, 'B+': 59,
    'C++': 60, 'D++': 62, 'E++': 64, 'F++': 65, 'G++': 67, 'A++': 69, 'B++': 71,

    'C--#': 1, 'D--#': 3, 'F--#': 6, 'G--#': 8, 'A--#': 10,
    'C-#': 25, 'D-#': 27, 'F-#': 30, 'G-#': 32, 'A-#': 34,
    'C#': 37, 'D#': 39, 'F#': 42, 'G#': 44, 'A#': 46,
    'C+#': 49, 'D+#': 51, 'F+#': 54, 'G+#': 56, 'A+#': 58,
    'C++#': 61, 'D++#': 63, 'F++#': 66, 'G++#': 68, 'A++#': 70,

}

# Load SoundFont
soundfont_path = "./data/GeneralUser-GS.sf2"
fs = fluidsynth.Synth()
fs.setting('synth.gain', 0.6)
fs.cc(0, 64, 0)  # sustain OFF
# fs.setting("synth.release-time", 0.01)
sfid = fs.sfload(soundfont_path)

# Duration settings
segment_duration_ms = 450  # ~350ms
total_duration_sec = (segment_duration_ms * 3) / 1000.0  # ~1.05s
loop_ms = segment_duration_ms  # e.g., 450
crossfade_ms = 100

# Generate sounds
for instrument, (bank, preset) in INSTRUMENTS.items():
    print(f"\n🎹 Generating instrument: {instrument}")
    instrument_dir = os.path.join(base_output_dir, "sounds", instrument)
    # os.makedirs(os.path.join(base_output_dir, f"raw/{instrument}"), exist_ok=True)
    os.makedirs(os.path.join(instrument_dir), exist_ok=True)

    fs.program_select(0, sfid, bank, preset)

    for key, midi_note in NOTE_MIDI_MAP.items():
        time_start = time.time()
        print(f"🎵 Generating note: {key} (MIDI {midi_note})")
        s = []

        new_key = (
            key.lower()
            .replace('#', '_sharp')
            .replace('-', '_minus')
            .replace('+', '_plus')
        )
        raw_path = os.path.join(base_output_dir, f"raw/{instrument}", f"{new_key}_full.wav")

        attack_sec = total_duration_sec / 3
        sustain_sec = 1.0
        release_sec = total_duration_sec / 3
        attack, sustain, release = get_note_samples(fs, midi_note, attack_sec, sustain_sec, release_sec)
        attack2, sustain2, release2 = get_note_samples(fs, midi_note, attack_sec, attack_sec, attack_sec)

        # Convert to PyDub segment
        attack_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(attack),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        ).fade_in(10)

        sustain_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(sustain),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        )

        loop_audio, interleaved = make_loopable(sustain_audio, loop_ms=loop_ms, crossfade_ms=crossfade_ms)
        # sustain_audio = trim_to_loopable(sustain_audio, 500)
        # sustain_audio = sustain_audio.append(sustain_audio[:30], crossfade=30)

        release_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(release),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        ).fade_out(10)

        raw_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(numpy.concatenate([attack2, sustain2, release2])),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        ).fade_out(10)

        attack_audio.export(os.path.join(instrument_dir, f"{new_key}_in.ogg"), format="ogg")
        loop_audio.export(os.path.join(instrument_dir, f"{new_key}_main.ogg"), format="ogg")
        release_audio.export(os.path.join(instrument_dir, f"{new_key}_out.ogg"), format="ogg")
        raw_audio.export(os.path.join(instrument_dir, f"{new_key}.ogg"), format="ogg")

        # raw_audio.export(raw_path, format="wav")

        sounds_json[f"vicky_note_{instrument}_{new_key}_1"] = {
            "sounds": [f"vicky_music:{instrument}/{new_key}_in"]
        }
        sounds_json[f"vicky_note_{instrument}_{new_key}_2"] = {
            "sounds": [f"vicky_music:{instrument}/{new_key}_main"]
        }
        sounds_json[f"vicky_note_{instrument}_{new_key}_3"] = {
            "sounds": [f"vicky_music:{instrument}/{new_key}_out"]
        }
        sounds_json[f"vicky_note_{instrument}_{new_key}"] = {
            "sounds": [f"vicky_music:{instrument}/{new_key}"]
        }

        print(f"XD Took ~ {time.time() - time_start}s")

with open(os.path.join(base_output_dir, "sounds.json"), "w", encoding="utf-8") as j:
    json.dump(sounds_json, j, indent=2)

with open(os.path.join(base_assets_dir, "pack.mcmeta"), "w", encoding="utf-8") as j:
    j.write(mcpack)

fs.delete()
