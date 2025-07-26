import json
import os
import time

import fluidsynth
import numpy
import numpy as np
from pydub import AudioSegment
from pydub.utils import which

sounds_json = {}

def extract_stable_zero_cross_loop(audio: AudioSegment, loop_ms: int = 400, window_ms: int = 20, db_threshold: float = 1.5) -> AudioSegment:
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
        subwindows = [segment[j:j + subwindow_size] for j in range(0, loop_sample_count - subwindow_size, subwindow_size)]
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


def get_note_samples(fs, note, attack, sustain, release):
    fs.noteon(0, note, 120)
    attack_samples = fs.get_samples(int(SAMPLE_RATE * attack))
    sustain_samples = fs.get_samples(int(SAMPLE_RATE * sustain))
    fs.noteoff(0, note)
    release_samples = fs.get_samples(int(SAMPLE_RATE * release))

    return (
        numpy.array(attack_samples),
        numpy.array(sustain_samples),
        numpy.array(release_samples)
    )

SAMPLE_RATE = 44100

AudioSegment.converter = which("ffmpeg")

# Directory setup
base_output_dir = "./output/soundgen/assets/vicky_music"

base_assets_dir = "./output/soundgen"
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
soundfont_path = "./data/Monalisa GM v2.105.1/Monalisa GM v2_105_1.sf2"
fs = fluidsynth.Synth()
fs.setting('synth.gain', 0.6)
# fs.setting("synth.release-time", 0.01)
sfid = fs.sfload(soundfont_path)

# Duration settings
segment_duration_ms = 450  # ~350ms
total_duration_sec = (segment_duration_ms * 3) / 1000.0  # ~1.05s

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


        attack_sec = total_duration_sec  / 3
        sustain_sec = total_duration_sec / 3
        release_sec = total_duration_sec / 3
        attack, sustain, release = get_note_samples(fs, midi_note, attack_sec, sustain_sec, release_sec)

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

        release_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(release),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        ).fade_out(10)

        raw_audio = AudioSegment(
            data=fluidsynth.raw_audio_string(numpy.concatenate([attack, sustain, release])),
            sample_width=2,
            frame_rate=SAMPLE_RATE,
            channels=2
        ).fade_out(10)

        attack_audio.export(os.path.join(instrument_dir, f"{new_key}_in.ogg"), format="ogg")
        sustain_audio.export(os.path.join(instrument_dir, f"{new_key}_main.ogg"), format="ogg")
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