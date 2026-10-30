"""Generate an original periodic Android loop. Python standard library only."""
from array import array
from pathlib import Path
import math
import sys
import wave

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/res/raw/gentle_loop.wav"
RATE = 22050
DURATION = 4


def generate() -> Path:
    samples = array("h")
    for index in range(RATE * DURATION):
        t = index / RATE
        # Integer frequencies and a 4s period make the loop seamless.
        # Android controls gain independently, so looping never restarts the fade.
        chord = sum(
            weight * math.sin(2 * math.pi * frequency * t)
            for frequency, weight in ((220.0, 0.48), (275.0, 0.30), (330.0, 0.16), (440.0, 0.06))
        )
        breath = 0.90 + 0.10 * math.sin(2 * math.pi * 0.25 * t) ** 2
        samples.append(round(32767 * .7 * breath * chord))
    if sys.byteorder != "little":
        samples.byteswap()
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    destination = OUTPUT
    with wave.open(str(destination), "wb") as audio:
        audio.setnchannels(1)
        audio.setsampwidth(2)
        audio.setframerate(RATE)
        audio.writeframes(samples.tobytes())
    return destination


if __name__ == "__main__":
    path = generate()
    print(f"Generated {path.name}: {DURATION}s, mono PCM16, {RATE}Hz")
