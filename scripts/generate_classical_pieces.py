import wave
import math
import struct
import subprocess
import os
import random

sample_rate = 44100

def note_freq(midi_note):
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))

# ---------------------------------------------------------
# Sound Generator 1: Acoustic Grand Piano (Warm, rich, wooden)
# ---------------------------------------------------------
def make_piano_sample(freq, dur, velocity=0.7, brightness=1.0):
    def gen(t, total_dur):
        if t >= dur:
            return 0.0
        # Felt hammer attack
        attack = 1.0 - math.exp(-t / 0.008)
        # Decay: dual stage (prompt decay + long singing tail)
        decay = 0.65 * math.exp(-t / (dur * 0.45 + 0.15)) + 0.35 * math.exp(-t / (dur * 1.2 + 0.6))
        env = attack * decay
        
        # Harmonic spectrum of grand piano string
        # Fundamental
        h1 = math.sin(2.0 * math.pi * freq * t)
        # 2nd harmonic (octave warmth)
        h2 = 0.55 * brightness * math.sin(2.0 * math.pi * freq * 2.0 * t)
        # 3rd harmonic (fifth color)
        h3 = 0.28 * brightness * math.sin(2.0 * math.pi * freq * 3.0 * t)
        # 4th harmonic
        h4 = 0.14 * (brightness ** 1.5) * math.sin(2.0 * math.pi * freq * 4.0 * t)
        # 5th harmonic
        h5 = 0.08 * (brightness ** 2.0) * math.sin(2.0 * math.pi * freq * 5.0 * t)
        # Slightly detuned string for unison chorusing (3 strings per key on grand piano)
        unison = 0.4 * math.sin(2.0 * math.pi * (freq * 1.0008) * t + 0.2)
        
        # Soundboard wooden resonance
        soundboard = 0.1 * math.sin(2.0 * math.pi * (freq * 0.5) * t) * math.exp(-t / 0.8)
        
        sig = (h1 + unison + h2 + h3 + h4 + h5 + soundboard) * env * velocity * 0.45
        return sig
    return gen

# ---------------------------------------------------------
# Sound Generator 2: Singing Chamber Strings (Warm, bowed, vibrato)
# ---------------------------------------------------------
def make_string_sample(freq, dur, velocity=0.6, vibrato_rate=4.8, vibrato_depth=0.015):
    def gen(t, total_dur):
        if t >= dur:
            return 0.0
        # Bowed attack (smoother ramp)
        attack = min(1.0, t / 0.12)
        # Release tail
        release_start = max(0.0, dur - 0.18)
        release = 1.0 if t < release_start else max(0.0, 1.0 - (t - release_start) / 0.18)
        env = attack * release
        
        # Expressive human vibrato (takes ~0.25s to establish)
        vib_intensity = min(1.0, t / 0.35)
        vib = 1.0 + vibrato_depth * vib_intensity * math.sin(2.0 * math.pi * vibrato_rate * t)
        inst_freq = freq * vib
        
        # String harmonics
        s1 = math.sin(2.0 * math.pi * inst_freq * t)
        s2 = 0.45 * math.sin(2.0 * math.pi * inst_freq * 2.0 * t)
        s3 = 0.25 * math.sin(2.0 * math.pi * inst_freq * 3.0 * t)
        s4 = 0.12 * math.sin(2.0 * math.pi * inst_freq * 4.0 * t)
        sub = 0.15 * math.sin(2.0 * math.pi * inst_freq * 0.5 * t)
        
        sig = (s1 + s2 + s3 + s4 + sub) * env * velocity * 0.35
        return sig
    return gen

# Helper to mix events into stereo buffers
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
        # Normalize and soft-limit
        max_val = max(max(abs(x) for x in self.left), max(abs(x) for x in self.right), 0.001)
        scale = 0.90 / max_val
        print(f"Normalizing with scale {scale:.3f} (peak was {max_val:.3f})")
        
        with wave.open(wav_path, 'wb') as wf:
            wf.setnchannels(2)
            wf.setsampwidth(2)
            wf.setframerate(sample_rate)
            
            frames = bytearray()
            for i in range(self.num_samples):
                l = self.left[i] * scale
                r = self.right[i] * scale
                
                # Soft tanh limiting
                l = math.tanh(l)
                r = math.tanh(r)
                
                il = max(-32767, min(32767, int(l * 32767.0)))
                ir = max(-32767, min(32767, int(r * 32767.0)))
                frames.extend(struct.pack('<hh', il, ir))
            
            wf.writeframes(frames)
        
        # Convert to high-quality MP3
        cmd = ["ffmpeg", "-y", "-i", wav_path, "-codec:a", "libmp3lame", "-b:a", "192k", mp3_path]
        subprocess.run(cmd, check=True)
        print(f"Exported: {mp3_path} ({os.path.getsize(mp3_path)} bytes)")


# =========================================================
# Piece 1: Erik Satie - Gymnopédie No. 1
# =========================================================
def render_satie():
    # 3/4 time, unhurried, calm Lent tempo: 64 BPM
    bpm = 64.0
    beat = 60.0 / bpm
    bar = beat * 3.0
    total_bars = 16
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)
    
    # Harmony progression:
    # Bars 0-3: Gmaj7 (G bass -> B-D-F#) -> Dmaj7 (D bass -> F#-A-C#)
    bass_chords = [
        # (bass_midi, [chord_midis])
        (43, [59, 62, 66]),  # G2, [B3, D4, F#4] = Gmaj7
        (38, [54, 57, 61]),  # D2, [F#3, A3, C#4] = Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (40, [55, 59, 62]),  # E2, [G3, B3, D4] = Emin7
        (45, [57, 60, 64]),  # A2, [A3, C4, E4] = Amin7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
        (38, [54, 57, 61]),  # D2, Dmaj7
        (43, [59, 62, 66]),  # G2, Gmaj7
    ]
    
    for bar_idx, (b_note, c_notes) in enumerate(bass_chords):
        t_bar = bar_idx * bar
        # Beat 1: Bass note
        mixer.add(t_bar, beat * 2.8, make_piano_sample(note_freq(b_note), beat * 2.6, velocity=0.62, brightness=0.85), vol=0.85, pan=-0.2)
        # Beats 2 & 3: Soft mid-range chords
        for c_n in c_notes:
            mixer.add(t_bar + beat * 1.0, beat * 1.9, make_piano_sample(note_freq(c_n), beat * 1.8, velocity=0.48, brightness=0.75), vol=0.55, pan=-0.05)
            mixer.add(t_bar + beat * 2.0, beat * 1.4, make_piano_sample(note_freq(c_n), beat * 1.3, velocity=0.45, brightness=0.75), vol=0.52, pan=0.05)

    # Iconic Gymnopédie No. 1 singing melody (starts on bar 4)
    # (start_bar, start_beat, duration_beats, midi_note, velocity)
    melody = [
        # Phrase 1
        (4, 0, 3.0, 66, 0.72), # F#4
        (5, 0, 1.5, 69, 0.70), # A4
        (5, 1.5, 1.5, 67, 0.68), # G4
        (6, 0, 3.0, 66, 0.74), # F#4
        (7, 0, 3.0, 61, 0.68), # C#4
        (8, 0, 3.0, 59, 0.66), # B3
        (9, 0, 1.5, 61, 0.68), # C#4
        (9, 1.5, 1.5, 62, 0.70), # D4
        (10, 0, 3.0, 64, 0.72), # E4
        (11, 0, 3.0, 59, 0.68), # B3
        (12, 0, 1.5, 61, 0.70), # C#4
        (12, 1.5, 1.5, 62, 0.72), # D4
        (13, 0, 3.0, 66, 0.76), # F#4
        (14, 0, 1.5, 64, 0.70), # E4
        (14, 1.5, 1.5, 62, 0.68), # D4
        (15, 0, 3.0, 66, 0.74), # F#4
    ]
    
    for (m_bar, m_b, dur_b, midi_n, vel) in melody:
        t_note = m_bar * bar + m_b * beat
        dur_sec = dur_b * beat
        # Piano melody
        mixer.add(t_note, dur_sec + 0.8, make_piano_sample(note_freq(midi_n), dur_sec + 0.6, velocity=vel, brightness=1.1), vol=0.92, pan=0.15)
        # Gentle celestial ambient string bed supporting melody
        mixer.add(t_note, dur_sec + 0.4, make_string_sample(note_freq(midi_n), dur_sec + 0.3, velocity=vel * 0.4), vol=0.35, pan=0.25)
    
    wav_out = "/tmp/satie_gymnopedie.wav"
    mp3_out = "app/src/main/res/raw/classical_gymnopedie.mp3"
    mixer.export(wav_out, mp3_out)


# =========================================================
# Piece 2: J.S. Bach - Air on the G String (BWV 1068)
# =========================================================
def render_bach():
    # 4/4 meter, Adagio tempo: 52 BPM
    bpm = 52.0
    beat = 60.0 / bpm
    bar = beat * 4.0
    total_bars = 8
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)
    
    # Bach's iconic walking cello bass line (steady 8th notes)
    # Key: D major
    bass_line = [
        # Bar 0
        (0.0, 50), (1.0, 49), (2.0, 47), (3.0, 45), # D3, C#3, B2, A2
        # Bar 1
        (4.0, 43), (5.0, 42), (6.0, 40), (7.0, 45), # G2, F#2, E2, A2
        # Bar 2
        (8.0, 50), (9.0, 49), (10.0, 47), (11.0, 45),
        # Bar 3
        (12.0, 43), (13.0, 42), (14.0, 40), (15.0, 45),
        # Bar 4
        (16.0, 47), (17.0, 45), (18.0, 43), (19.0, 42), # B2, A2, G2, F#2
        # Bar 5
        (20.0, 40), (21.0, 42), (22.0, 43), (23.0, 45), # E2, F#2, G2, A2
        # Bar 6
        (24.0, 47), (25.0, 45), (26.0, 43), (27.0, 42),
        # Bar 7
        (28.0, 40), (29.0, 45), (30.0, 50), (31.0, 50), # E2, A2, D3, D3
    ]
    
    for (beat_idx, midi_note) in bass_line:
        t_note = beat_idx * beat
        dur_sec = beat * 0.95
        # Pizzicato/warm cello bass
        mixer.add(t_note, dur_sec * 1.5, make_piano_sample(note_freq(midi_note - 12), dur_sec * 1.4, velocity=0.65, brightness=0.6), vol=0.75, pan=-0.25)
        mixer.add(t_note, dur_sec, make_string_sample(note_freq(midi_note - 12), dur_sec, velocity=0.55), vol=0.60, pan=-0.3)

    # Inner string harmony pads (viola & 2nd violin)
    inner_harmonies = [
        # (bar, [(midi, pan)])
        (0, [(57, -0.1), (62, 0.1)]), # A3, D4
        (1, [(55, -0.1), (61, 0.1)]), # G3, C#4
        (2, [(57, -0.1), (62, 0.1)]), # A3, D4
        (3, [(55, -0.1), (61, 0.1)]), # G3, C#4
        (4, [(59, -0.1), (62, 0.1)]), # B3, D4
        (5, [(57, -0.1), (60, 0.1)]), # A3, C4
        (6, [(59, -0.1), (62, 0.1)]), # B3, D4
        (7, [(57, -0.1), (62, 0.1)]), # A3, D4
    ]
    for b_idx, notes in inner_harmonies:
        t_bar = b_idx * bar
        for (m_note, p) in notes:
            mixer.add(t_bar, bar * 0.98, make_string_sample(note_freq(m_note), bar * 0.98, velocity=0.45), vol=0.45, pan=p)

    # Masterpiece sustained solo violin melody (singing, rich vibrato)
    violin_melody = [
        # Bar 0: Long majestic high note
        (0.0, 3.8, 74, 0.78), # D5 sustained
        # Bar 1: Stepwise descent with ornament
        (4.0, 1.0, 74, 0.74), # D5
        (5.0, 0.5, 73, 0.72), # C#5
        (5.5, 0.5, 71, 0.70), # B4
        (6.0, 1.0, 73, 0.75), # C#5
        (7.0, 1.0, 74, 0.76), # D5
        # Bar 2: Soaring to high F#
        (8.0, 3.5, 78, 0.82), # F#5 sustained
        # Bar 3: Elegant baroque resolution
        (12.0, 1.0, 76, 0.75), # E5
        (13.0, 0.5, 74, 0.72), # D5
        (13.5, 0.5, 73, 0.70), # C#5
        (14.0, 1.0, 74, 0.74), # D5
        (15.0, 1.0, 71, 0.72), # B4
        # Bar 4: Soaring G5
        (16.0, 3.0, 79, 0.84), # G5
        (19.0, 1.0, 78, 0.78), # F#5
        # Bar 5:
        (20.0, 1.5, 76, 0.76), # E5
        (21.5, 0.5, 74, 0.72), # D5
        (22.0, 2.0, 73, 0.75), # C#5
        # Bar 6:
        (24.0, 2.0, 74, 0.78), # D5
        (26.0, 2.0, 76, 0.80), # E5
        # Bar 7: Warm final cadence
        (28.0, 1.5, 74, 0.76), # D5
        (29.5, 0.5, 73, 0.72), # C#5
        (30.0, 2.0, 74, 0.80), # D5
    ]
    
    for (t_b, dur_b, m_note, vel) in violin_melody:
        t_note = t_b * beat
        dur_sec = dur_b * beat
        mixer.add(t_note, dur_sec, make_string_sample(note_freq(m_note), dur_sec, velocity=vel, vibrato_rate=5.2, vibrato_depth=0.018), vol=0.88, pan=0.15)
        # Soft acoustic piano doubling
        mixer.add(t_note, dur_sec, make_piano_sample(note_freq(m_note), dur_sec, velocity=vel * 0.45, brightness=1.0), vol=0.35, pan=0.0)

    wav_out = "/tmp/bach_air.wav"
    mp3_out = "app/src/main/res/raw/classical_bach_air.mp3"
    mixer.export(wav_out, mp3_out)


# =========================================================
# Piece 3: Frédéric Chopin - Nocturne in E-flat Major, Op. 9 No. 2
# =========================================================
def render_chopin():
    # 12/8 time feel (4 gentle beats of triplets), Andante tempo: 60 BPM
    bpm = 60.0
    beat = 60.0 / bpm
    bar = beat * 4.0
    total_bars = 8
    total_dur = total_bars * bar
    mixer = AudioMixer(total_dur)
    
    # Left hand: Classic Chopin Nocturne bass note + 2 soft chords
    # Key: Eb major (Eb3 = 51, Bb3 = 58, G4 = 67, etc.)
    lh_bars = [
        # Bar 0: Eb major
        (39, [51, 55, 58]), # Eb2 bass, [Eb3, G3, Bb3] chords
        # Bar 1: C minor 7
        (36, [48, 51, 55, 58]), # C2 bass, [C3, Eb3, G3, Bb3]
        # Bar 2: F minor 7
        (41, [53, 56, 60, 63]), # F2 bass, [F3, Ab3, C4, Eb4]
        # Bar 3: Bb 7
        (34, [46, 50, 53, 56]), # Bb1 bass, [Bb2, D3, F3, Ab3]
        # Bar 4: Eb major
        (39, [51, 55, 58]), # Eb2
        # Bar 5: G minor / Bb
        (34, [46, 50, 53, 58]), # Bb1
        # Bar 6: Ab major
        (32, [44, 48, 51, 56]), # Ab1
        # Bar 7: Eb major cadence
        (39, [51, 55, 58]), # Eb2
    ]
    
    for bar_idx, (b_note, c_notes) in enumerate(lh_bars):
        t_bar = bar_idx * bar
        # Beat 1: Bass note
        mixer.add(t_bar, beat * 1.8, make_piano_sample(note_freq(b_note), beat * 1.7, velocity=0.60, brightness=0.75), vol=0.80, pan=-0.2)
        # Chords on sub-beats (gentle accompaniment)
        for offset in [beat * 0.33, beat * 0.66, beat * 1.33, beat * 1.66, beat * 2.33, beat * 2.66, beat * 3.33, beat * 3.66]:
            for cn in c_notes:
                mixer.add(t_bar + offset, beat * 0.6, make_piano_sample(note_freq(cn), beat * 0.55, velocity=0.42, brightness=0.68), vol=0.48, pan=-0.05)

    # Right hand: Chopin's beloved lyrical cantabile melody
    # (t_beat, dur_beats, midi_note, velocity)
    rh_melody = [
        # Iconic opening motif: Bb4 -> G5
        (0.0, 1.5, 70, 0.75), # Bb4
        (1.5, 1.5, 79, 0.82), # G5
        (3.0, 0.5, 77, 0.72), # F5
        (3.5, 0.5, 75, 0.70), # Eb5
        # Bar 1
        (4.0, 1.5, 77, 0.76), # F5
        (5.5, 1.0, 79, 0.78), # G5
        (6.5, 0.5, 70, 0.70), # Bb4
        (7.0, 1.0, 72, 0.74), # C5
        # Bar 2
        (8.0, 2.0, 75, 0.80), # Eb5
        (10.0, 1.0, 74, 0.72), # D5
        (11.0, 1.0, 72, 0.70), # C5
        # Bar 3
        (12.0, 2.5, 70, 0.78), # Bb4
        (14.5, 0.5, 72, 0.72), # C5
        (15.0, 1.0, 74, 0.75), # D5
        # Bar 4
        (16.0, 1.5, 75, 0.82), # Eb5
        (17.5, 1.5, 79, 0.84), # G5
        (19.0, 0.5, 77, 0.74), # F5
        (19.5, 0.5, 75, 0.72), # Eb5
        # Bar 5
        (20.0, 1.5, 77, 0.78), # F5
        (21.5, 1.0, 79, 0.80), # G5
        (22.5, 0.5, 82, 0.85), # Bb5 soaring
        (23.0, 1.0, 80, 0.76), # Ab5
        # Bar 6
        (24.0, 1.5, 79, 0.80), # G5
        (25.5, 1.0, 77, 0.75), # F5
        (26.5, 0.5, 75, 0.72), # Eb5
        (27.0, 1.0, 74, 0.74), # D5
        # Bar 7
        (28.0, 3.5, 75, 0.82), # Eb5 final sweet resolution
    ]
    
    for (t_b, dur_b, m_note, vel) in rh_melody:
        t_note = t_b * beat
        dur_sec = dur_b * beat
        mixer.add(t_note, dur_sec + 0.6, make_piano_sample(note_freq(m_note), dur_sec + 0.5, velocity=vel, brightness=1.12), vol=0.92, pan=0.18)
        # Gentle atmospheric warmth
        mixer.add(t_note, dur_sec + 0.3, make_string_sample(note_freq(m_note - 12), dur_sec + 0.2, velocity=vel * 0.35), vol=0.30, pan=0.1)

    wav_out = "/tmp/chopin_nocturne.wav"
    mp3_out = "app/src/main/res/raw/classical_chopin_nocturne.mp3"
    mixer.export(wav_out, mp3_out)

if __name__ == "__main__":
    print("Rendering Satie Gymnopédie No. 1...")
    render_satie()
    print("Rendering Bach Air on the G String...")
    render_bach()
    print("Rendering Chopin Nocturne Op. 9 No. 2...")
    render_chopin()
    print("All classical pieces generated successfully!")
