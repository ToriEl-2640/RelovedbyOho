import wave
import math
import struct
import subprocess
import os
import random

sample_rate = 44100
bpm = 74.0  # Ideal, unhurried, relaxing shopping tempo
beat_dur = 60.0 / bpm
bar_dur = beat_dur * 4.0
total_bars = 8  # 8 bars = ~25.95 seconds seamless loop
total_dur = total_bars * bar_dur
total_samples = int(total_dur * sample_rate)

left_channel = [0.0] * total_samples
right_channel = [0.0] * total_samples

def note_freq(midi_note):
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))

def add_audio(start_time, duration, gen_fn, vol=1.0, pan=0.0):
    start_idx = int(start_time * sample_rate)
    num_samples = int(duration * sample_rate)
    left_gain = (1.0 - pan) * 0.5 * vol
    right_gain = (1.0 + pan) * 0.5 * vol
    
    for i in range(num_samples):
        idx = (start_idx + i) % total_samples
        t = i / float(sample_rate)
        val = gen_fn(t, duration)
        left_channel[idx] += val * left_gain
        right_channel[idx] += val * right_gain

# 1. Warm Vintage Rhodes Electric Piano (Lush, velvety chords)
def synth_rhodes(midi_note, duration, velocity=0.7):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        # Soft touch attack (12ms smooth ramp), warm lingering decay
        attack = 1.0 - math.exp(-t / 0.012)
        decay = math.exp(-t / (dur * 0.85 + 0.3))
        env = attack * decay
        
        # Rhodes tine & bar physics: fundamental + bell overtone + warmth
        s1 = math.sin(2.0 * math.pi * freq * t)
        # Detuned harmonic for lush chorus shimmer
        s1_chorus = 0.5 * math.sin(2.0 * math.pi * (freq * 1.0012) * t + 0.3)
        s2 = 0.25 * math.sin(2.0 * math.pi * freq * 2.0 * t)
        s3 = 0.08 * math.sin(2.0 * math.pi * freq * 3.0 * t)
        # Sub warmth
        sub = 0.15 * math.sin(2.0 * math.pi * (freq * 0.5) * t)
        
        # Gentle tremolo (stereo auto-pan feel)
        tremolo = 1.0 + 0.08 * math.sin(2.0 * math.pi * 3.2 * t)
        
        return (s1 + s1_chorus + s2 + s3 + sub) * env * velocity * tremolo * 0.35
    return sample_gen

# 2. Warm Round Boutique Bass (Mellow, deep, unobtrusive)
def synth_warm_bass(midi_note, duration):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        # Very soft attack, warm low sustain
        env = (1.0 - math.exp(-t / 0.025)) * math.exp(-t / (dur * 0.9))
        f1 = math.sin(2.0 * math.pi * freq * t)
        # Deep sub-bass resonance
        f_sub = 0.4 * math.sin(2.0 * math.pi * (freq * 0.5) * t)
        # Gentle second harmonic for warmth through mobile phone speakers
        f2 = 0.25 * math.sin(2.0 * math.pi * freq * 2.0 * t)
        return (f1 + f_sub + f2) * env * 0.45
    return sample_gen

# 3. Soft Boutique Lo-Fi Drums (Super gentle brushed beat)
def synth_soft_kick(duration=0.25):
    def sample_gen(t, dur):
        # Smooth pitch drop without aggressive click
        freq = 55.0 * math.exp(-t / 0.04) + 38.0
        env = math.exp(-t / 0.12)
        val = math.sin(2.0 * math.pi * freq * t)
        return val * env * 0.32
    return sample_gen

def synth_brushed_rim(duration=0.09):
    random.seed(int(duration * 1000) % 999)
    def sample_gen(t, dur):
        env = math.exp(-t / 0.035)
        # Soft band-passed noise / rim tap
        tone = math.sin(2.0 * math.pi * 650.0 * t) * 0.3
        noise = (random.random() * 2.0 - 1.0) * 0.7
        return (tone + noise) * env * 0.18
    return sample_gen

def synth_soft_shaker(duration=0.06):
    def sample_gen(t, dur):
        env = (1.0 - math.exp(-t / 0.005)) * math.exp(-t / 0.022)
        noise = (random.random() * 2.0 - 1.0)
        return noise * env * 0.09
    return sample_gen

# 4. Dreamy Boutique Ambient Chime / Sparkle (Gentle, luxury shopping aesthetic)
def synth_ambient_chime(midi_note, duration=1.8):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        env = (1.0 - math.exp(-t / 0.006)) * math.exp(-t / 0.6)
        c1 = math.sin(2.0 * math.pi * freq * t)
        c2 = 0.3 * math.sin(2.0 * math.pi * freq * 2.002 * t)
        c3 = 0.15 * math.sin(2.0 * math.pi * freq * 3.0 * t)
        return (c1 + c2 + c3) * env * 0.14
    return sample_gen

# Chord Voicings for Sophisticated, Mellow Shopping Ambience:
# Bar 0,1: Fmaj9   (F2, A3, C4, E4, G4)
# Bar 2,3: Em7     (E2, G3, B3, D4, G4)
# Bar 4,5: Dm9     (D2, F3, A3, C4, E4)
# Bar 6,7: Cmaj9   (C2, E3, G3, B3, D4)

progression = [
    # bar 0: Fmaj9
    {"bass": 41, "chord": [57, 60, 64, 67], "chime": [72, 76]},
    # bar 1: Fmaj9 variation
    {"bass": 41, "chord": [57, 60, 64, 67], "chime": [79]},
    # bar 2: Em7
    {"bass": 40, "chord": [55, 59, 62, 67], "chime": [71, 74]},
    # bar 3: Em7 variation
    {"bass": 40, "chord": [55, 59, 62, 67], "chime": [76]},
    # bar 4: Dm9
    {"bass": 38, "chord": [53, 57, 60, 64], "chime": [69, 72]},
    # bar 5: Dm9 variation
    {"bass": 38, "chord": [53, 57, 60, 64], "chime": [74]},
    # bar 6: Cmaj9
    {"bass": 36, "chord": [52, 55, 59, 62], "chime": [67, 71]},
    # bar 7: Cmaj9 gentle turnaround
    {"bass": 36, "chord": [52, 55, 59, 62], "chime": [72, 74]},
]

print("Synthesizing Boutique Shopping Ambience...")

for bar_idx in range(total_bars):
    bar_time = bar_idx * bar_dur
    step_data = progression[bar_idx]
    
    # 1. Warm Bass on Downbeats with gentle syncopated walk
    bass_note = step_data["bass"]
    add_audio(bar_time, bar_dur * 0.48, synth_warm_bass(bass_note, bar_dur * 0.48), vol=0.85, pan=0.0)
    add_audio(bar_time + beat_dur * 2.5, bar_dur * 0.35, synth_warm_bass(bass_note + 7, bar_dur * 0.35), vol=0.65, pan=0.05)
    
    # 2. Lush Rhodes Chords (laid back, gentle strum timing)
    chord_notes = step_data["chord"]
    # Strum 1 at start of bar
    for note_i, note in enumerate(chord_notes):
        strum_offset = note_i * 0.022
        pan = -0.3 + (note_i / float(len(chord_notes) - 1)) * 0.6
        add_audio(bar_time + strum_offset, bar_dur * 0.85, synth_rhodes(note, bar_dur * 0.85, velocity=0.65), vol=0.75, pan=pan)
        
    # Second gentle syncopated chord on beat 2.5
    for note_i, note in enumerate(chord_notes):
        strum_offset = note_i * 0.018
        pan = -0.25 + (note_i / float(len(chord_notes) - 1)) * 0.5
        add_audio(bar_time + beat_dur * 2.5 + strum_offset, bar_dur * 0.4, synth_rhodes(note, bar_dur * 0.4, velocity=0.5), vol=0.6, pan=pan)
        
    # 3. Soft Ambient Chimes on select bars for boutique elegance
    chimes = step_data["chime"]
    for ci, c_note in enumerate(chimes):
        chime_t = bar_time + beat_dur * (1.0 + ci * 1.5)
        add_audio(chime_t, 1.6, synth_ambient_chime(c_note, 1.6), vol=0.7, pan=0.25 if ci % 2 == 0 else -0.25)

    # 4. Gentle Drum Groove (Soft lo-fi kick on beat 1 and beat 3, brushed rim on beat 2 and 4)
    for beat in range(4):
        beat_t = bar_time + beat * beat_dur
        
        # Soft kick on beat 0 and beat 2.5
        if beat == 0:
            add_audio(beat_t, 0.25, synth_soft_kick(), vol=0.7, pan=0.0)
        elif beat == 2:
            add_audio(beat_t + beat_dur * 0.5, 0.22, synth_soft_kick(), vol=0.55, pan=0.0)
            
        # Brushed rim on beat 1 and 3 (2 & 4 in musical counts)
        if beat == 1 or beat == 3:
            add_audio(beat_t, 0.1, synth_brushed_rim(), vol=0.5, pan=0.1)
            
        # Very quiet soft shaker on 8th notes
        add_audio(beat_t, 0.06, synth_soft_shaker(), vol=0.35, pan=-0.2)
        add_audio(beat_t + beat_dur * 0.5, 0.06, synth_soft_shaker(), vol=0.25, pan=0.2)

# Normalize smoothly to prevent any clipping, ensuring pristine background listening
max_amp = 0.0001
for i in range(total_samples):
    max_amp = max(max_amp, abs(left_channel[i]), abs(right_channel[i]))

target_peak = 0.75  # Gentle headroom so it never blasts or overwhelms
scale = target_peak / max_amp

wav_path = "scripts/shopping_ambience.wav"
with wave.open(wav_path, "w") as wav_file:
    wav_file.setnchannels(2)
    wav_file.setsampwidth(2)
    wav_file.setframerate(sample_rate)
    frames = bytearray()
    for i in range(total_samples):
        l_val = int(max(-32767, min(32767, left_channel[i] * scale * 32767)))
        r_val = int(max(-32767, min(32767, right_channel[i] * scale * 32767)))
        frames.extend(struct.pack("<hh", l_val, r_val))
    wav_file.writeframes(frames)

print(f"Generated WAV file ({os.path.getsize(wav_path)} bytes)")

# Convert to 128k MP3
mp3_shopping = "app/src/main/res/raw/shopping_ambience.mp3"
mp3_soft_salsa = "app/src/main/res/raw/soft_salsa.mp3"

subprocess.run(["ffmpeg", "-y", "-i", wav_path, "-codec:a", "libmp3lame", "-b:a", "128k", mp3_shopping], check=True)
# Also overwrite soft_salsa.mp3 with this chill shopping music so any legacy lookup gets the new perfect track
subprocess.run(["ffmpeg", "-y", "-i", wav_path, "-codec:a", "libmp3lame", "-b:a", "128k", mp3_soft_salsa], check=True)

# Clean up wav
os.remove(wav_path)
print("Successfully generated and converted shopping_ambience.mp3 and updated soft_salsa.mp3!")
