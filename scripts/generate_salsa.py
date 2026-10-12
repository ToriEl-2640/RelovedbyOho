import wave
import math
import struct
import subprocess
import os

sample_rate = 44100
bpm = 86.0
beat_dur = 60.0 / bpm
bar_dur = beat_dur * 4.0
total_bars = 8  # 8 bars = ~22.3 seconds seamless loop
total_dur = total_bars * bar_dur
total_samples = int(total_dur * sample_rate)

left_channel = [0.0] * total_samples
right_channel = [0.0] * total_samples

def note_freq(midi_note):
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))

def add_audio(buf, start_time, duration, gen_fn, vol=1.0, pan=0.0):
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

# 1. Warm Acoustic / Upright Bass Synth (Tumbao)
def synth_bass(midi_note, duration):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        # Soft envelope: quick 10ms attack, exponential decay
        env = (1.0 - math.exp(-t / 0.015)) * math.exp(-t / (dur * 0.8))
        # Warm fundamental + gentle 2nd and 3rd harmonic
        s1 = math.sin(2.0 * math.pi * freq * t)
        s2 = 0.45 * math.sin(2.0 * math.pi * freq * 2.0 * t)
        s3 = 0.15 * math.sin(2.0 * math.pi * freq * 3.0 * t)
        sub = 0.3 * math.sin(2.0 * math.pi * (freq * 0.5) * t)
        return (s1 + s2 + s3 + sub) * env * 0.7
    return sample_gen

# 2. Gentle Rhodes / Acoustic Piano Montuno
def synth_piano(midi_note, duration, velocity=0.8):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        env = (1.0 - math.exp(-t / 0.008)) * math.exp(-t / (dur * 0.75 + 0.1))
        # Bell-like mellow EP/Piano timbre (harmonic ratios with detune)
        h1 = math.sin(2.0 * math.pi * freq * t)
        h2 = 0.35 * math.sin(2.0 * math.pi * freq * 2.0 * t + 0.1)
        h3 = 0.15 * math.sin(2.0 * math.pi * freq * 3.0 * t)
        h4 = 0.08 * math.sin(2.0 * math.pi * freq * 4.0 * t)
        # Gentle tremolo / chorus
        chorus = 1.0 + 0.05 * math.sin(2.0 * math.pi * 4.5 * t)
        return (h1 + h2 + h3 + h4) * env * velocity * chorus * 0.5
    return sample_gen

# 3. Soft Latin Percussion: Clave, Conga, Maracas
import random
random.seed(42)

def synth_clave(duration=0.08):
    freq = 2400.0
    def sample_gen(t, dur):
        env = math.exp(-t / 0.018)
        val = math.sin(2.0 * math.pi * freq * t) + 0.3 * math.sin(2.0 * math.pi * freq * 1.6 * t)
        return val * env * 0.35
    return sample_gen

def synth_conga_open(midi_note=60, duration=0.25):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        # Pitch drops slightly on strike
        cur_freq = freq * (1.0 + 0.3 * math.exp(-t / 0.02))
        env = math.exp(-t / 0.12)
        tone = math.sin(2.0 * math.pi * cur_freq * t)
        noise = (random.random() * 2.0 - 1.0) * math.exp(-t / 0.008) * 0.4
        return (tone + noise) * env * 0.6
    return sample_gen

def synth_conga_slap(duration=0.12):
    def sample_gen(t, dur):
        env = math.exp(-t / 0.025)
        click = math.sin(2.0 * math.pi * 1200.0 * t) * math.exp(-t / 0.006)
        noise = (random.random() * 2.0 - 1.0) * env
        return (click * 0.5 + noise * 0.5) * 0.45
    return sample_gen

def synth_maraca(duration=0.06):
    def sample_gen(t, dur):
        env = (1.0 - math.exp(-t / 0.006)) * math.exp(-t / 0.025)
        # filtered high noise
        noise = (random.random() * 2.0 - 1.0) * env
        return noise * 0.25
    return sample_gen

# 4. Soft Vibraphone / Flute melody
def synth_vibraphone(midi_note, duration):
    freq = note_freq(midi_note)
    def sample_gen(t, dur):
        env = (1.0 - math.exp(-t / 0.01)) * math.exp(-t / (dur * 0.85))
        # warm vibraphone with mellow vibrato
        vibrato = 1.0 + 0.015 * math.sin(2.0 * math.pi * 5.0 * t)
        tone = math.sin(2.0 * math.pi * freq * vibrato * t)
        tone2 = 0.2 * math.sin(2.0 * math.pi * freq * 2.0 * vibrato * t)
        tone3 = 0.08 * math.sin(2.0 * math.pi * freq * 4.0 * vibrato * t)
        return (tone + tone2 + tone3) * env * 0.4
    return sample_gen

# Chord progression for 8 bars (2 cycles of 4 bars):
# Bar 1: Dm9 (D F A C E)
# Bar 2: G13 / G9 (G B D F A E)
# Bar 3: Cmaj9 (C E G B D)
# Bar 4: Am9 / A7alt (A C E G B)
# Relaxing Latin Bossa/Salsa lounge harmony

chords = [
    # Bar 0: Dm9
    {"root": 38, "fifth": 45, "notes": [50, 53, 57, 60, 64]},
    # Bar 1: G13
    {"root": 43, "fifth": 50, "notes": [47, 53, 57, 59, 64]},
    # Bar 2: Cmaj9
    {"root": 36, "fifth": 43, "notes": [48, 52, 55, 59, 62]},
    # Bar 3: Am9
    {"root": 45, "fifth": 52, "notes": [45, 52, 57, 60, 64]},
    # Bar 4: Dm9
    {"root": 38, "fifth": 45, "notes": [50, 53, 57, 60, 64]},
    # Bar 5: G9
    {"root": 43, "fifth": 50, "notes": [47, 53, 57, 59, 62]},
    # Bar 6: Cmaj9
    {"root": 36, "fifth": 43, "notes": [48, 52, 55, 59, 64]},
    # Bar 7: A7b13 / Am9
    {"root": 45, "fifth": 52, "notes": [45, 53, 57, 61, 64]}
]

# Generate Bass Tumbao for each bar
# Salsa tumbao: beat 2-and, beat 4 (tied into next bar)
for bar in range(total_bars):
    b_time = bar * bar_dur
    c = chords[bar]
    root = c["root"]
    fifth = c["fifth"]
    
    # Beat 1 (soft touch on downbeat)
    add_audio(left_channel, b_time + 0.0, beat_dur * 0.8, synth_bass(root, beat_dur * 0.8), vol=0.6, pan=-0.1)
    # Beat 2-and (syncopated tumbao push)
    add_audio(left_channel, b_time + beat_dur * 1.5, beat_dur * 0.9, synth_bass(fifth, beat_dur * 0.9), vol=0.85, pan=-0.1)
    # Beat 4 (anticipating next chord)
    add_audio(left_channel, b_time + beat_dur * 3.0, beat_dur * 1.4, synth_bass(root + 12, beat_dur * 1.4), vol=0.9, pan=-0.1)

# Generate Piano Montuno (syncopated salsa piano arpeggios, gentle lounge velocity)
# Salsa montuno pattern per bar:
# [0.0, 0.75, 1.5, 2.25, 3.0, 3.5] beats
montuno_offsets = [0.0, 0.5, 1.25, 1.75, 2.25, 3.0, 3.5]
for bar in range(total_bars):
    b_time = bar * bar_dur
    c = chords[bar]
    notes = c["notes"]
    for i, offset in enumerate(montuno_offsets):
        t_note = b_time + offset * beat_dur
        # Play chord dyads or triads
        n1 = notes[i % len(notes)]
        n2 = notes[(i + 2) % len(notes)]
        n3 = notes[(i + 3) % len(notes)]
        dur = beat_dur * 0.65
        vel = 0.55 if (i % 2 == 0) else 0.42
        pan = -0.3 + 0.1 * (i % 3)
        add_audio(left_channel, t_note, dur, synth_piano(n1, dur, vel), vol=0.65, pan=pan)
        add_audio(left_channel, t_note, dur, synth_piano(n2, dur, vel * 0.85), vol=0.6, pan=pan + 0.1)
        add_audio(left_channel, t_note, dur, synth_piano(n3 + 12, dur, vel * 0.6), vol=0.45, pan=pan + 0.2)

# Generate Clave (Son Clave 3-2: Bar A has 3 strokes [0, 1.5, 3], Bar B has 2 strokes [1.0, 2.0])
for bar in range(total_bars):
    b_time = bar * bar_dur
    if bar % 2 == 0:
        # 3 side
        clave_beats = [0.0, 1.5, 3.0]
    else:
        # 2 side
        clave_beats = [1.0, 2.0]
    for cb in clave_beats:
        t_c = b_time + cb * beat_dur
        add_audio(left_channel, t_c, 0.08, synth_clave(0.08), vol=0.35, pan=0.25)

# Generate Percussion: Maracas (continuous 16th notes with accents) & Congas
for bar in range(total_bars):
    b_time = bar * bar_dur
    # 16th notes maracas (16 per bar)
    for step in range(16):
        t_m = b_time + step * (beat_dur / 4.0)
        accent = 1.3 if (step % 4 == 0 or step % 4 == 2) else 0.75
        add_audio(left_channel, t_m, 0.06, synth_maraca(0.06), vol=0.22 * accent, pan=0.35)
    
    # Conga Tumbao pattern:
    # 1: heel/toe, 2: slap, 3: heel/toe, 4: open, 4-and: open
    add_audio(left_channel, b_time + beat_dur * 1.0, 0.12, synth_conga_slap(0.12), vol=0.45, pan=-0.25)
    add_audio(left_channel, b_time + beat_dur * 3.0, 0.25, synth_conga_open(58, 0.25), vol=0.5, pan=-0.2)
    add_audio(left_channel, b_time + beat_dur * 3.5, 0.25, synth_conga_open(60, 0.25), vol=0.55, pan=-0.15)

# Soothing Vibraphone / Flute Melody (mellow, lyrical Latin lounge phrases)
# Melody phrase 1 (bars 0-3): E -> G -> A -> G -> E -> D -> C
melody_events = [
    # Bar 0 (Dm9)
    (0 * bar_dur + 0.5 * beat_dur, 64, 1.2), # E5
    (0 * bar_dur + 2.0 * beat_dur, 65, 0.8), # F5
    (0 * bar_dur + 3.0 * beat_dur, 67, 1.5), # G5
    # Bar 1 (G13)
    (1 * bar_dur + 1.0 * beat_dur, 69, 1.2), # A5
    (1 * bar_dur + 2.5 * beat_dur, 67, 1.0), # G5
    (1 * bar_dur + 3.5 * beat_dur, 64, 1.4), # E5
    # Bar 2 (Cmaj9)
    (2 * bar_dur + 1.5 * beat_dur, 62, 1.2), # D5
    (2 * bar_dur + 3.0 * beat_dur, 60, 1.8), # C5
    # Bar 3 (Am9)
    (3 * bar_dur + 1.0 * beat_dur, 64, 1.0), # E5
    (3 * bar_dur + 2.0 * beat_dur, 67, 1.0), # G5
    (3 * bar_dur + 3.0 * beat_dur, 69, 1.8), # A5
    # Bar 4 (Dm9)
    (4 * bar_dur + 0.5 * beat_dur, 72, 1.5), # C6
    (4 * bar_dur + 2.0 * beat_dur, 71, 0.9), # B5
    (4 * bar_dur + 3.0 * beat_dur, 69, 1.4), # A5
    # Bar 5 (G9)
    (5 * bar_dur + 1.0 * beat_dur, 67, 1.2), # G5
    (5 * bar_dur + 2.5 * beat_dur, 65, 1.0), # F5
    (5 * bar_dur + 3.5 * beat_dur, 64, 1.4), # E5
    # Bar 6 (Cmaj9)
    (6 * bar_dur + 1.5 * beat_dur, 62, 1.4), # D5
    (6 * bar_dur + 3.0 * beat_dur, 64, 1.8), # E5
    # Bar 7 (A7)
    (7 * bar_dur + 1.0 * beat_dur, 60, 1.0), # C5
    (7 * bar_dur + 2.5 * beat_dur, 57, 1.8), # A4
]

for t_start, m_note, dur_b in melody_events:
    add_audio(left_channel, t_start, dur_b * beat_dur, synth_vibraphone(m_note, dur_b * beat_dur), vol=0.55, pan=0.15)

# Normalization & soft clipping
max_peak = 0.0
for i in range(total_samples):
    max_peak = max(max_peak, abs(left_channel[i]), abs(right_channel[i]))

gain = 0.85 / max(max_peak, 0.001)

wav_path = "scripts/soft_salsa.wav"
with wave.open(wav_path, "wb") as wf:
    wf.setnchannels(2)
    wf.setsampwidth(2)
    wf.setframerate(sample_rate)
    frames = bytearray()
    for i in range(total_samples):
        # Soft tanh saturation for analog warmth
        l = math.tanh(left_channel[i] * gain)
        r = math.tanh(right_channel[i] * gain)
        int_l = max(-32767, min(32767, int(l * 32767.0)))
        int_r = max(-32767, min(32767, int(r * 32767.0)))
        frames.extend(struct.pack("<hh", int_l, int_r))
    wf.writeframes(frames)

print(f"Generated WAV {wav_path}, duration {total_dur:.2f}s")
# Convert to lightweight high-quality MP3 for Android raw resource
mp3_path = "app/src/main/res/raw/soft_salsa.mp3"
cmd = ["ffmpeg", "-y", "-i", wav_path, "-b:a", "128k", mp3_path]
res = subprocess.run(cmd, capture_output=True, text=True)
print("FFmpeg result:", res.returncode)
print(f"Created {mp3_path}, size: {os.path.getsize(mp3_path)} bytes")
