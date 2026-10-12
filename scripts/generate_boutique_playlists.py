import wave
import math
import struct
import subprocess
import os
import random

sample_rate = 44100

def note_freq(midi_note):
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))

class AudioMixer:
    def __init__(self, duration_sec):
        self.dur = duration_sec
        self.num_samples = int(duration_sec * sample_rate)
        self.left = [0.0] * self.num_samples
        self.right = [0.0] * self.num_samples

    def add(self, start_time, duration, gen_fn, vol=1.0, pan=0.0):
        start_idx = int(start_time * sample_rate)
        n_samples = int(duration * sample_rate)
        left_gain = (1.0 - pan) * 0.5 * vol
        right_gain = (1.0 + pan) * 0.5 * vol
        
        for i in range(n_samples):
            idx = (start_idx + i) % self.num_samples
            t = i / float(sample_rate)
            val = gen_fn(t, duration)
            self.left[idx] += val * left_gain
            self.right[idx] += val * right_gain

    def export(self, wav_path, mp3_path):
        max_val = max(max(abs(x) for x in self.left), max(abs(x) for x in self.right), 0.001)
        scale = 0.88 / max_val
        print(f"Normalizing {mp3_path} with scale {scale:.3f} (peak was {max_val:.3f})")
        
        with wave.open(wav_path, 'wb') as wf:
            wf.setnchannels(2)
            wf.setsampwidth(2)
            wf.setframerate(sample_rate)
            
            frames = bytearray()
            for i in range(self.num_samples):
                l = math.tanh(self.left[i] * scale)
                r = math.tanh(self.right[i] * scale)
                il = max(-32767, min(32767, int(l * 32767.0)))
                ir = max(-32767, min(32767, int(r * 32767.0)))
                frames.extend(struct.pack('<hh', il, ir))
            wf.writeframes(frames)
        
        cmd = ["ffmpeg", "-y", "-i", wav_path, "-codec:a", "libmp3lame", "-b:a", "192k", mp3_path]
        subprocess.run(cmd, check=True)
        print(f"Successfully generated: {mp3_path} ({os.path.getsize(mp3_path)} bytes)")


# =========================================================
# INSTRUMENTS
# =========================================================

# 1. Warm Jazz Piano / Rhodes
def synth_jazz_piano(freq, dur, velocity=0.7, brightness=0.9):
    def gen(t, total_dur):
        if t >= dur: return 0.0
        attack = 1.0 - math.exp(-t / 0.008)
        decay = 0.6 * math.exp(-t / (dur * 0.5 + 0.1)) + 0.4 * math.exp(-t / (dur * 1.1 + 0.3))
        env = attack * decay
        h1 = math.sin(2.0 * math.pi * freq * t)
        h2 = 0.45 * brightness * math.sin(2.0 * math.pi * freq * 2.0 * t)
        h3 = 0.22 * brightness * math.sin(2.0 * math.pi * freq * 3.0 * t)
        h4 = 0.10 * (brightness**1.5) * math.sin(2.0 * math.pi * freq * 4.0 * t)
        unison = 0.35 * math.sin(2.0 * math.pi * (freq * 1.001) * t + 0.2)
        return (h1 + unison + h2 + h3 + h4) * env * velocity * 0.5
    return gen

# 2. Upright Acoustic Jazz Bass
def synth_upright_bass(freq, dur, velocity=0.8):
    def gen(t, total_dur):
        if t >= dur: return 0.0
        attack = 1.0 - math.exp(-t / 0.015)
        decay = math.exp(-t / (dur * 0.8 + 0.15))
        env = attack * decay
        b1 = math.sin(2.0 * math.pi * freq * t)
        b2 = 0.5 * math.sin(2.0 * math.pi * freq * 2.0 * t)
        b3 = 0.15 * math.sin(2.0 * math.pi * freq * 3.0 * t)
        sub = 0.3 * math.sin(2.0 * math.pi * (freq * 0.5) * t)
        return (b1 + b2 + b3 + sub) * env * velocity * 0.6
    return gen

# 3. Clean Electric Guitar (Highlife / Pop)
def synth_guitar(freq, dur, velocity=0.7, tone=1.0):
    def gen(t, total_dur):
        if t >= dur: return 0.0
        attack = 1.0 - math.exp(-t / 0.005)
        decay = math.exp(-t / (dur * 0.65 + 0.08))
        env = attack * decay
        # Pluck transient
        pluck = 0.4 * math.sin(2.0 * math.pi * freq * 4.0 * t) * math.exp(-t / 0.02)
        g1 = math.sin(2.0 * math.pi * freq * t)
        g2 = 0.6 * tone * math.sin(2.0 * math.pi * freq * 2.0 * t)
        g3 = 0.3 * tone * math.sin(2.0 * math.pi * freq * 3.0 * t)
        g4 = 0.12 * tone * math.sin(2.0 * math.pi * freq * 4.0 * t)
        return (g1 + g2 + g3 + g4 + pluck) * env * velocity * 0.5
    return gen

# 4. Soft Brushed Snare / Drum Brush
def synth_brush_snare(dur, velocity=0.5):
    random_noise = [random.uniform(-1.0, 1.0) for _ in range(int(dur * sample_rate))]
    def gen(t, total_dur):
        idx = int(t * sample_rate)
        if idx >= len(random_noise): return 0.0
        env = math.exp(-t / 0.08)
        # bandpass / tone
        body = math.sin(2.0 * math.pi * 180.0 * t) * math.exp(-t / 0.04)
        noise = random_noise[idx] * env
        return (noise * 0.7 + body * 0.3) * velocity * 0.35
    return gen

# 5. Soft Kick Drum
def synth_kick(velocity=0.7):
    def gen(t, total_dur):
        if t >= 0.25: return 0.0
        env = math.exp(-t / 0.07)
        freq = 110.0 * math.exp(-t / 0.03) + 45.0
        return math.sin(2.0 * math.pi * freq * t) * env * velocity * 0.7
    return gen

# 6. Highlife Percussion: Shekere / Shaker
def synth_shekere(dur, velocity=0.4):
    noise = [random.uniform(-1.0, 1.0) for _ in range(int(dur * sample_rate))]
    def gen(t, total_dur):
        idx = int(t * sample_rate)
        if idx >= len(noise): return 0.0
        env = math.sin(min(math.pi, t / dur * math.pi)) * math.exp(-t / 0.05)
        return noise[idx] * env * velocity * 0.3
    return gen

# 7. Mellow Horn / Saxophone
def synth_mellow_horn(freq, dur, velocity=0.6):
    def gen(t, total_dur):
        if t >= dur: return 0.0
        attack = min(1.0, t / 0.08)
        rel = max(0.0, 1.0 - (t - max(0.0, dur - 0.12)) / 0.12)
        env = attack * rel
        vib = 1.0 + 0.012 * math.sin(2.0 * math.pi * 5.0 * t)
        f = freq * vib
        h1 = math.sin(2.0 * math.pi * f * t)
        h2 = 0.55 * math.sin(2.0 * math.pi * f * 2.0 * t)
        h3 = 0.35 * math.sin(2.0 * math.pi * f * 3.0 * t)
        h4 = 0.18 * math.sin(2.0 * math.pi * f * 4.0 * t)
        warmth = 0.2 * math.sin(2.0 * math.pi * (f * 0.5) * t)
        return (h1 + h2 + h3 + h4 + warmth) * env * velocity * 0.4
    return gen


# =========================================================
# TRACK 1: Boutique Café Jazz Instrumental
# Smooth coffeehouse jazz: walking upright bass, swung brushed drums,
# lush Major 7th & Minor 9th chords, gentle muted jazz melody
# =========================================================
def render_cafe_jazz():
    bpm = 84.0
    beat = 60.0 / bpm
    bar = beat * 4.0
    total_bars = 8
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)

    # Chord progression:
    # Bar 0: Fmaj7 (F2, [A3, C4, E4, G4])
    # Bar 1: Dm9   (D2, [F3, A3, C4, E4])
    # Bar 2: Gm9   (G2, [Bb3, D4, F4, A4])
    # Bar 3: C13   (C2, [Bb3, E4, A4, D5])
    # Bar 4: Am7   (A2, [C4, E4, G4, B4])
    # Bar 5: D7b9  (D2, [F#3, C4, Eb4, A4])
    # Bar 6: Gm7   (G2, [Bb3, D4, F4, Bb4])
    # Bar 7: C7    (C2, [Bb3, E4, G4, C5])
    chords = [
        (41, [57, 60, 64, 67]), # F2, Fmaj9
        (38, [53, 57, 60, 64]), # D2, Dm9
        (43, [58, 62, 65, 69]), # G2, Gm9
        (36, [58, 64, 69, 74]), # C2, C13
        (45, [60, 64, 67, 71]), # A2, Am7
        (38, [54, 60, 63, 69]), # D2, D7b9
        (43, [58, 62, 65, 70]), # G2, Gm7
        (36, [58, 64, 67, 72]), # C2, C7
    ]

    for bar_idx, (b_note, c_notes) in enumerate(chords):
        t_bar = bar_idx * bar
        
        # 1. Swung Walking Bass on every beat
        # Jazz walking line: Root, approach, 5th, chromatic passing
        bass_notes = [b_note, b_note + 4, b_note + 7, b_note + 2]
        for beat_i, b_pitch in enumerate(bass_notes):
            t_beat = t_bar + beat_i * beat
            mixer.add(t_beat, beat * 0.9, synth_upright_bass(note_freq(b_pitch), beat * 0.85, velocity=0.75), vol=0.8, pan=-0.2)

        # 2. Swung brushed drums & ride
        for beat_i in range(4):
            t_beat = t_bar + beat_i * beat
            # Soft kick on beats 1 and 3
            if beat_i in (0, 2):
                mixer.add(t_beat, 0.2, synth_kick(velocity=0.5), vol=0.6, pan=0.0)
            # Brushed snare on 2 and 4
            if beat_i in (1, 3):
                mixer.add(t_beat, 0.15, synth_brush_snare(0.12, velocity=0.6), vol=0.6, pan=0.1)
            # Swung hi-hat/ride brush (chick on swing-eighth)
            swing_off = beat * 0.66
            mixer.add(t_beat + swing_off, 0.08, synth_shekere(0.06, velocity=0.4), vol=0.4, pan=0.25)

        # 3. Café Piano Comping (classic Charleston rhythm on beat 1 & up-beat of 2)
        for c_pitch in c_notes:
            mixer.add(t_bar, beat * 1.5, synth_jazz_piano(note_freq(c_pitch), beat * 1.4, velocity=0.55), vol=0.65, pan=0.15)
            mixer.add(t_bar + beat * 1.66, beat * 1.2, synth_jazz_piano(note_freq(c_pitch), beat * 1.1, velocity=0.5), vol=0.6, pan=0.15)
            mixer.add(t_bar + beat * 3.0, beat * 0.8, synth_jazz_piano(note_freq(c_pitch), beat * 0.75, velocity=0.48), vol=0.55, pan=0.15)

    # 4. Melodic jazz improvisation (mellow, warm, unhurried café melody)
    jazz_melody = [
        (0.0, 1.5, 69), (1.66, 1.0, 72), (2.66, 1.0, 71), (3.66, 0.5, 69), # Fmaj bar
        (4.0, 2.0, 67), (6.0, 1.5, 65), (7.66, 0.5, 64),                    # Dm bar
        (8.0, 1.5, 62), (9.66, 1.0, 65), (10.66, 1.0, 69), (11.66, 0.5, 72),# Gm bar
        (12.0, 2.5, 71), (14.66, 1.0, 69),                                   # C bar
        (16.0, 1.5, 72), (17.66, 1.0, 76), (18.66, 1.0, 74),                 # Am bar
        (20.0, 1.5, 71), (21.66, 1.0, 69), (22.66, 1.0, 67),                 # D7 bar
        (24.0, 2.0, 65), (26.0, 1.5, 64), (27.66, 0.5, 62),                 # Gm bar
        (28.0, 3.0, 65),                                                     # Resolution F
    ]
    for (t_b, dur_b, m_pitch) in jazz_melody:
        t_note = t_b * beat
        dur_sec = dur_b * beat
        mixer.add(t_note, dur_sec, synth_mellow_horn(note_freq(m_pitch), dur_sec, velocity=0.65), vol=0.75, pan=-0.1)

    wav_out = "/tmp/cafe_jazz.wav"
    mp3_out = "app/src/main/res/raw/boutique_cafe_jazz.mp3"
    mixer.export(wav_out, mp3_out)


# =========================================================
# TRACK 2: Feel-Good Pop
# Bright, upbeat, infectious groove: snappy 4-on-the-floor beat,
# funky bassline, bright pop guitar riffs, sparkling chord synths
# =========================================================
def render_feelgood_pop():
    bpm = 108.0
    beat = 60.0 / bpm
    bar = beat * 4.0
    total_bars = 8
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)

    # Pop Progression: I - V - vi - IV (Key: G Major)
    # G -> D -> Em -> C (repeat 2x)
    pop_bars = [
        (43, [55, 59, 62]), # G (G2, [G3, B3, D4])
        (38, [54, 57, 62]), # D (D2, [F#3, A3, D4])
        (40, [55, 59, 64]), # Em (E2, [G3, B3, E4])
        (36, [52, 55, 60]), # C (C2, [E3, G3, C4])
        (43, [55, 59, 62]), # G
        (38, [54, 57, 62]), # D
        (40, [55, 59, 64]), # Em
        (36, [52, 55, 60]), # C
    ]

    for bar_idx, (b_note, c_notes) in enumerate(pop_bars):
        t_bar = bar_idx * bar

        # 1. 4-on-the-floor Kick & Snare Beat (Bright & energetic)
        for beat_i in range(4):
            t_beat = t_bar + beat_i * beat
            mixer.add(t_beat, 0.2, synth_kick(velocity=0.8), vol=0.75, pan=0.0)
            if beat_i in (1, 3):
                mixer.add(t_beat, 0.18, synth_brush_snare(0.15, velocity=0.85), vol=0.8, pan=0.0)
            # 8th-note Hi-hat shimmer
            mixer.add(t_beat, 0.06, synth_shekere(0.05, velocity=0.45), vol=0.4, pan=0.2)
            mixer.add(t_beat + beat * 0.5, 0.06, synth_shekere(0.05, velocity=0.55), vol=0.45, pan=-0.2)

        # 2. Funky Pop Bassline (bouncy 8th notes)
        bass_pattern = [
            (0.0, b_note, 0.8), (0.75, b_note, 0.6), (1.5, b_note + 12, 0.7),
            (2.0, b_note, 0.8), (2.5, b_note + 7, 0.7), (3.25, b_note, 0.65)
        ]
        for (off_b, pitch, vel) in bass_pattern:
            mixer.add(t_bar + off_b * beat, beat * 0.45, synth_upright_bass(note_freq(pitch), beat * 0.4, velocity=vel), vol=0.85, pan=-0.1)

        # 3. Bright Chk-chk Pop Guitar Strumming (offbeats)
        for beat_i in range(4):
            t_off = t_bar + (beat_i + 0.5) * beat
            for cn in c_notes:
                mixer.add(t_off, beat * 0.4, synth_guitar(note_freq(cn + 12), beat * 0.35, velocity=0.7, tone=1.3), vol=0.6, pan=0.25)

    # 4. Catchy, whistling/singing Feel-Good Pop Hook
    # G major catchy hook: B4 - D5 - E5 - D5 - B4 - A4 - G4
    pop_hook = [
        # Bar 0 (G)
        (0.0, 0.5, 71), (0.5, 0.5, 74), (1.0, 1.0, 76), (2.0, 0.5, 74), (2.5, 0.5, 71), (3.0, 1.0, 69),
        # Bar 1 (D)
        (4.0, 1.5, 67), (5.5, 0.5, 69), (6.0, 1.0, 71), (7.0, 1.0, 69),
        # Bar 2 (Em)
        (8.0, 0.5, 71), (8.5, 0.5, 74), (9.0, 1.0, 76), (10.0, 0.5, 79), (10.5, 0.5, 76), (11.0, 1.0, 74),
        # Bar 3 (C)
        (12.0, 2.0, 71), (14.0, 1.0, 69), (15.0, 1.0, 67),
        # Bar 4 (G - repeat variation)
        (16.0, 0.5, 71), (16.5, 0.5, 74), (17.0, 1.0, 76), (18.0, 0.5, 74), (18.5, 0.5, 71), (19.0, 1.0, 69),
        # Bar 5 (D)
        (20.0, 1.5, 67), (21.5, 0.5, 69), (22.0, 1.0, 71), (23.0, 1.0, 69),
        # Bar 6 (Em)
        (24.0, 0.5, 71), (24.5, 0.5, 74), (25.0, 1.0, 76), (26.0, 0.5, 79), (26.5, 0.5, 81), (27.0, 1.0, 79),
        # Bar 7 (C)
        (28.0, 3.0, 79),
    ]
    for (t_b, dur_b, pitch) in pop_hook:
        t_note = t_b * beat
        dur_sec = dur_b * beat
        mixer.add(t_note, dur_sec, synth_jazz_piano(note_freq(pitch), dur_sec, velocity=0.85, brightness=1.3), vol=0.85, pan=0.1)
        mixer.add(t_note, dur_sec, synth_guitar(note_freq(pitch), dur_sec, velocity=0.75, tone=1.4), vol=0.6, pan=-0.15)

    wav_out = "/tmp/feelgood_pop.wav"
    mp3_out = "app/src/main/res/raw/feelgood_pop.mp3"
    mixer.export(wav_out, mp3_out)


# =========================================================
# TRACK 3: Highlife and Mellow Nigerian Classics
# Warm, nostalgic, authentic Nigerian palm-wine & highlife:
# Interlocking dual melodic electric guitars (classic Igbo/Yoruba highlife style),
# warm melodic bass, shekere and conga groove, mellow sweet brass
# =========================================================
def render_nigerian_highlife():
    bpm = 92.0
    beat = 60.0 / bpm
    bar = beat * 4.0
    total_bars = 8
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)

    # Classic Nigerian Highlife progression:
    # Key: C Major / F Major (Classic Rex Lawson / Chief Osita Osadebe / Victor Uwaifo warmth)
    # C -> F -> G -> C (repeat)
    highlife_bars = [
        (36, [60, 64, 67]), # C (C2, [C4, E4, G4])
        (41, [60, 65, 69]), # F (F2, [C4, F4, A4])
        (43, [59, 62, 67]), # G (G2, [B3, D4, G4])
        (36, [60, 64, 67]), # C
        (36, [60, 64, 67]), # C
        (41, [60, 65, 69]), # F
        (43, [59, 62, 67]), # G
        (36, [60, 64, 67]), # C
    ]

    for bar_idx, (b_note, c_notes) in enumerate(highlife_bars):
        t_bar = bar_idx * bar

        # 1. Highlife Percussion: Shekere & Talking Drum / Conga rhythm
        # Syncopated African bell / shekere clave (1, 1-and-a-half, 2-and, 3-and, 4)
        shekere_pattern = [0.0, 0.75, 1.5, 2.25, 3.0, 3.5]
        for sp in shekere_pattern:
            mixer.add(t_bar + sp * beat, 0.08, synth_shekere(0.06, velocity=0.55), vol=0.5, pan=0.3)

        # Warm ground kick / conga low thud on 1 and 3
        mixer.add(t_bar, 0.2, synth_kick(velocity=0.6), vol=0.65, pan=-0.1)
        mixer.add(t_bar + 2.0 * beat, 0.2, synth_kick(velocity=0.55), vol=0.6, pan=-0.1)
        # Conga high slap on 2 and 4
        mixer.add(t_bar + 1.0 * beat, 0.12, synth_brush_snare(0.09, velocity=0.5), vol=0.45, pan=0.2)
        mixer.add(t_bar + 3.0 * beat, 0.12, synth_brush_snare(0.09, velocity=0.5), vol=0.45, pan=0.2)

        # 2. Warm Mellow Highlife Bassline (very melodic, dancing, round)
        # Root -> 3rd -> 5th -> Octave syncopated
        hl_bass = [
            (0.0, b_note), (0.75, b_note + 4), (1.5, b_note + 7), (2.25, b_note + 12),
            (3.0, b_note + 7), (3.5, b_note + 4)
        ]
        for (off_b, pitch) in hl_bass:
            mixer.add(t_bar + off_b * beat, beat * 0.5, synth_upright_bass(note_freq(pitch), beat * 0.45, velocity=0.75), vol=0.85, pan=-0.2)

        # 3. Interlocking Rhythm Guitar 1 (classic highlife 6ths & 3rds picking)
        rh_picks = [
            (0.0, c_notes[0]), (0.5, c_notes[1]), (1.0, c_notes[2]), (1.5, c_notes[1]),
            (2.0, c_notes[0]), (2.5, c_notes[1]), (3.0, c_notes[2]), (3.5, c_notes[1])
        ]
        for (off_b, pitch) in rh_picks:
            mixer.add(t_bar + off_b * beat, beat * 0.4, synth_guitar(note_freq(pitch + 12), beat * 0.35, velocity=0.6, tone=1.1), vol=0.55, pan=-0.25)

    # 4. Lead Highlife Melodic Guitar (Soulful, lyrical Osadebe-style lead guitar)
    hl_lead = [
        # Bar 0 (C)
        (0.0, 0.75, 72), (0.75, 0.75, 76), (1.5, 1.0, 79), (2.5, 0.5, 76), (3.0, 1.0, 72),
        # Bar 1 (F)
        (4.0, 0.75, 77), (4.75, 0.75, 81), (5.5, 1.5, 79), (7.0, 1.0, 76),
        # Bar 2 (G)
        (8.0, 0.75, 74), (8.75, 0.75, 79), (9.5, 1.0, 77), (10.5, 0.5, 76), (11.0, 1.0, 74),
        # Bar 3 (C)
        (12.0, 2.0, 72), (14.0, 1.0, 76), (15.0, 1.0, 74),
        # Bar 4 (C)
        (16.0, 0.75, 72), (16.75, 0.75, 76), (17.5, 1.0, 79), (18.5, 0.5, 81), (19.0, 1.0, 79),
        # Bar 5 (F)
        (20.0, 0.75, 81), (20.75, 0.75, 84), (21.5, 1.5, 81), (23.0, 1.0, 79),
        # Bar 6 (G)
        (24.0, 0.75, 79), (24.75, 0.75, 77), (25.5, 1.0, 76), (26.5, 0.5, 74), (27.0, 1.0, 72),
        # Bar 7 (C)
        (28.0, 3.5, 72),
    ]
    for (t_b, dur_b, pitch) in hl_lead:
        t_note = t_b * beat
        dur_sec = dur_b * beat
        # Highlife lead clean electric guitar
        mixer.add(t_note, dur_sec, synth_guitar(note_freq(pitch), dur_sec, velocity=0.85, tone=1.35), vol=0.85, pan=0.18)
        # Soft brass harmony doubling
        mixer.add(t_note, dur_sec, synth_mellow_horn(note_freq(pitch - 12), dur_sec, velocity=0.45), vol=0.4, pan=0.0)

    wav_out = "/tmp/nigerian_highlife.wav"
    mp3_out = "app/src/main/res/raw/nigerian_highlife.mp3"
    mixer.export(wav_out, mp3_out)

if __name__ == "__main__":
    print("Rendering 1: Boutique Café Jazz Instrumental...")
    render_cafe_jazz()
    print("Rendering 2: Feel-Good Pop...")
    render_feelgood_pop()
    print("Rendering 3: Highlife and Mellow Nigerian Classics...")
    render_nigerian_highlife()
    print("All requested boutique playlists generated successfully!")
