"""Synthesises the "owner comes home" sound effects (no samples, no licences needed).

Usage: python tools/audio/homecoming.py <out_dir>

Writes 22.05 kHz mono 16-bit WAVs, small enough for Android's SoundPool:
  home_tick, home_car, home_car_door, home_steps, home_keys, home_lock, home_door,
  home_switch, home_scatter
"""
import math
import os
import sys
import wave

import numpy as np

RATE = 22050
rng = np.random.default_rng(42)


def t_axis(seconds):
    return np.arange(int(seconds * RATE)) / RATE


def lowpass(x, cutoff):
    """One-pole low-pass; cutoff may be an array (per-sample)."""
    cutoff = np.broadcast_to(np.asarray(cutoff, dtype=np.float64), x.shape)
    a = 1.0 - np.exp(-2.0 * math.pi * cutoff / RATE)
    y = np.empty_like(x)
    acc = 0.0
    for i in range(len(x)):
        acc += a[i] * (x[i] - acc)
        y[i] = acc
    return y


def highpass(x, cutoff):
    return x - lowpass(x, cutoff)


def bandpass(x, lo, hi):
    return lowpass(highpass(x, lo), hi)


def decay(seconds, tau):
    return np.exp(-t_axis(seconds) / tau)


def place(buf, sound, at, gain=1.0):
    start = int(at * RATE)
    end = min(len(buf), start + len(sound))
    if start < len(buf):
        buf[start:end] += sound[: end - start] * gain


def resonator(freq, seconds, tau, partials=(1.0,), amps=None):
    t = t_axis(seconds)
    amps = amps or [1.0 / (i + 1) for i in range(len(partials))]
    out = np.zeros_like(t)
    for p, a in zip(partials, amps):
        out += a * np.sin(2 * math.pi * freq * p * t + rng.uniform(0, 6.28))
    return out * np.exp(-t / tau)


def noise(seconds):
    return rng.standard_normal(int(seconds * RATE))


def write(path, x, peak=0.85):
    x = np.asarray(x, dtype=np.float64)
    m = np.max(np.abs(x)) or 1.0
    x = np.clip(x / m * peak, -1, 1)
    # Short fades so nothing clicks at the edges.
    fade = min(len(x) // 4, int(0.01 * RATE))
    x[:fade] *= np.linspace(0, 1, fade)
    x[-fade:] *= np.linspace(1, 0, fade)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes((x * 32767).astype("<i2").tobytes())


def tick():
    """Projector-fan "tock" for each countdown second."""
    body = resonator(820, 0.12, 0.025, partials=(1, 2.3, 3.9), amps=[1, 0.4, 0.2])
    click = bandpass(noise(0.12), 1500, 6000) * decay(0.12, 0.004) * 0.6
    return body + click


def car():
    """A car pulling up outside: far-off engine growing closer, braking, idling, cut."""
    seconds = 6.0
    t = t_axis(seconds)
    # Engine speed: cruising, easing off, idle; then the key turns at 5.4 s.
    rpm = np.interp(t, [0, 2.5, 3.8, 4.6, 5.4, 6.0], [42, 46, 34, 24, 23, 0])
    phase = 2 * math.pi * np.cumsum(rpm) / RATE
    engine = (np.sin(phase) + 0.6 * np.sin(2 * phase + 0.3) + 0.35 * np.sin(3 * phase + 1.1)
              + 0.2 * np.sin(4 * phase))
    # Combustion roughness.
    engine *= 1 + 0.25 * lowpass(noise(seconds), 30)
    rumble = lowpass(noise(seconds), 140) * 3.0
    tyres = bandpass(noise(seconds), 120, 900) * np.interp(t, [0, 3.0, 4.6, 4.8], [0.25, 0.5, 0.15, 0])
    # Approach: distance falls, so loudness and brightness rise.
    near = np.interp(t, [0, 3.5, 4.5, 6.0], [0.08, 0.7, 1.0, 1.0])
    mix = (engine * 0.8 + rumble * 0.4) * near + tyres * near
    mix = lowpass(mix, 250 + 2200 * near)
    # Brake squeal, faint.
    squeal = np.sin(2 * math.pi * 2300 * t) * np.interp(t, [0, 4.0, 4.2, 4.6, 4.7], [0, 0, 0.05, 0.03, 0])
    off = np.interp(t, [0, 5.35, 5.6], [1, 1, 0])
    return mix * off + squeal


def car_door():
    """Heavy door thunk, then the latch."""
    out = np.zeros(int(0.7 * RATE))
    thump = lowpass(noise(0.4), 220) * decay(0.4, 0.06) * 2.5 + resonator(70, 0.4, 0.08)
    latch = bandpass(noise(0.05), 1500, 5000) * decay(0.05, 0.006)
    place(out, thump, 0.0)
    place(out, latch, 0.03, 0.5)
    return out


def steps():
    """Six steps up the concrete path, getting closer."""
    out = np.zeros(int(3.0 * RATE))
    for i in range(6):
        at = 0.1 + i * 0.47 + rng.uniform(-0.03, 0.03)
        gain = 0.35 + 0.65 * i / 5
        heel = bandpass(noise(0.09), 150, 1800) * decay(0.09, 0.018) + resonator(85, 0.09, 0.02) * 0.8
        toe = bandpass(noise(0.07), 400, 3500) * decay(0.07, 0.012) * 0.5
        grit = bandpass(noise(0.15), 2000, 7000) * decay(0.15, 0.04) * 0.12
        place(out, heel, at, gain)
        place(out, toe, at + 0.07, gain)
        place(out, grit, at, gain)
    return out


def keys():
    """A ring of keys jingling as they are found."""
    out = np.zeros(int(1.3 * RATE))
    for _ in range(14):
        at = abs(rng.normal(0.35, 0.22))
        f = rng.uniform(2100, 3400)
        ping = resonator(f, 0.25, rng.uniform(0.03, 0.09), partials=(1, 1.53, 2.37, 3.11),
                         amps=[1, 0.6, 0.4, 0.25])
        place(out, ping, at, rng.uniform(0.2, 0.6))
    return out


def lock():
    """Key scraping in, then the deadbolt turning over."""
    out = np.zeros(int(0.8 * RATE))
    scrape = bandpass(noise(0.16), 2500, 8000) * np.interp(t_axis(0.16), [0, 0.02, 0.16], [0, 1, 0.2]) * 0.3
    place(out, scrape, 0.0)
    click = resonator(1700, 0.06, 0.008, partials=(1, 2.7)) + bandpass(noise(0.06), 2000, 7000) * decay(0.06, 0.004)
    place(out, click, 0.2, 0.7)
    clunk = resonator(330, 0.2, 0.03, partials=(1, 2.1, 3.3)) + lowpass(noise(0.2), 600) * decay(0.2, 0.02) * 2
    place(out, clunk, 0.42)
    return out


def door():
    """Latch, then a slow hinge creak as the door swings in."""
    seconds = 1.6
    out = np.zeros(int(seconds * RATE))
    latch = resonator(1400, 0.08, 0.01, partials=(1, 2.3)) + bandpass(noise(0.08), 1500, 6000) * decay(0.08, 0.006)
    place(out, latch, 0.0, 0.8)
    # Stick-slip creak: a pulse train with wandering rate, ringing a small resonance.
    t = t_axis(1.3)
    rate = 70 + 60 * np.sin(2 * math.pi * 0.9 * t) + 30 * np.sin(2 * math.pi * 2.3 * t)
    phase = np.cumsum(rate) / RATE
    pulses = (np.diff(np.floor(phase), prepend=0) > 0).astype(float)
    creak = bandpass(pulses * 4, 500, 2200)
    creak = creak * np.interp(t, [0, 0.1, 1.0, 1.3], [0, 1, 0.7, 0])
    place(out, creak, 0.15, 0.9)
    whoosh = lowpass(noise(1.0), 300) * np.interp(t_axis(1.0), [0, 0.4, 1.0], [0, 1, 0]) * 0.8
    place(out, whoosh, 0.2)
    return out


def switch():
    """Rocker light switch."""
    out = np.zeros(int(0.25 * RATE))
    for at, g in ((0.0, 0.6), (0.012, 1.0)):
        c = bandpass(noise(0.04), 1800, 9000) * decay(0.04, 0.003) + resonator(2600, 0.04, 0.004)
        place(out, c, at, g)
    return out


def scatter():
    """Dozens of tiny legs fleeing across floorboards."""
    seconds = 1.4
    out = np.zeros(int(seconds * RATE))
    for _ in range(160):
        at = rng.uniform(0, seconds - 0.03) ** 1.3
        c = bandpass(noise(0.012), 3000, 9000) * decay(0.012, 0.0015)
        place(out, c, at, rng.uniform(0.2, 1.0) * (1 - at / seconds))
    return out


def main():
    out_dir = sys.argv[1]
    os.makedirs(out_dir, exist_ok=True)
    for name, fn in (("home_tick", tick), ("home_car", car), ("home_car_door", car_door),
                     ("home_steps", steps), ("home_keys", keys), ("home_lock", lock),
                     ("home_door", door), ("home_switch", switch), ("home_scatter", scatter)):
        write(os.path.join(out_dir, name + ".wav"), fn())
        print("wrote", name)


if __name__ == "__main__":
    main()
