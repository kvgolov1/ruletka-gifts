"""Generate an original short celebration tune (no third-party music)."""
import math
import struct
import wave
from pathlib import Path

RATE = 44100
DURATION = 9
NOTES = [72, 76, 79, 84, 79, 76, 77, 81, 84, 89, 84, 81, 79, 83, 86, 91]
output = Path(__file__).resolve().parent.parent / "build" / "audio" / "fanfare.wav"
output.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(output), "wb") as track:
    track.setnchannels(1)
    track.setsampwidth(2)
    track.setframerate(RATE)
    samples = bytearray()
    for i in range(RATE * DURATION):
        t = i / RATE
        beat = int(t / .375)
        age = t % .375
        frequency = 440 * 2 ** ((NOTES[beat % len(NOTES)] - 69) / 12)
        envelope = min(age / .015, 1) * math.exp(-age * 7)
        melody = envelope * (math.sin(2 * math.pi * frequency * t)
                              + .25 * math.sin(4 * math.pi * frequency * t))
        chord = (60, 64, 67) if t < 3 or t >= 6 else (65, 69, 72)
        harmony = sum(math.sin(2 * math.pi * 440 * 2 ** ((n - 69) / 12) * t)
                      for n in chord) / 3
        fade = min(1, t / .04, (DURATION - t) / 1.2)
        value = (melody * .22 + harmony * .06) * fade
        samples.extend(struct.pack("<h", int(value * 32767)))
    track.writeframes(samples)
