package org.vicky.forge.client.audio;

// package com.example.mymod.client.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.nio.ShortBuffer;
import java.util.Random;

public final class SynthUtils {
    private SynthUtils() {
    }

    public static long hashParams(byte wave, float freq, float a, float d, float s, float r, float vr, float vd, float vel) {
        long h = 1469598103934665603L;
        h ^= wave;
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(freq);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(a);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(d);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(s);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(r);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(vr);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(vd);
        h *= 1099511628211L;
        h ^= Float.floatToIntBits(vel);
        h *= 1099511628211L;
        return h;
    }

    public static short[] synthFull(byte wave, double freqHz, int totalSamples, int sampleRate, double velocity,
                                    double a, double d, double s, double r, double vibratoRate, double vibratoDepth) {
        short[] out = new short[totalSamples];
        double sr = sampleRate;
        Random rng = new Random(0); // deterministic if desired
        for (int i = 0; i < totalSamples; i++) {
            double t = i / sr;

            // vibrato cents -> frequency multiplier
            double vib = Math.sin(2 * Math.PI * vibratoRate * t) * vibratoDepth;
            double f = freqHz * Math.pow(2.0, vib / 1200.0);

            double phase = 2 * Math.PI * f * t;
            double sample = switch (wave) {
                case 0 -> Math.sin(phase); // sine
                case 1 -> Math.signum(Math.sin(phase)); // square
                case 2 -> // saw
                        (2.0 / Math.PI) * (fmod(phase + Math.PI, 2 * Math.PI) - Math.PI);
                case 3 -> // triangle
                        (2.0 / Math.PI) * Math.asin(Math.sin(phase));
                case 4 -> rng.nextDouble() * 2.0 - 1.0;
                default -> 0.0; // noise
            };

            // ADSR envelope (attack, decay, sustain, release)
            double env;
            double attackEnd = a;
            double decayEnd = a + d;
            double releaseStart = (totalSamples / sr) - r;
            if (t < attackEnd) {
                env = t / Math.max(attackEnd, 1e-9);
            } else if (t < decayEnd) {
                double u = (t - attackEnd) / Math.max(d, 1e-9);
                env = 1.0 + (s - 1.0) * u;
            } else if (t < releaseStart) {
                env = s;
            } else {
                double u = (t - releaseStart) / Math.max(r, 1e-9);
                env = s * (1.0 - u);
            }

            double val = sample * env * velocity;
            val = Math.max(-1.0, Math.min(1.0, val));
            out[i] = (short) (val * Short.MAX_VALUE);
        }
        return out;
    }

    public static short[] synthFragment(byte wave, double freqHz, int fragmentSamples, int sampleRate, double velocity,
                                        double a, double d, double s, double r, double vibratoRate, double vibratoDepth) {
        // render a short looped fragment representing the sustain region (optionally applying partial ADS)
        return synthFull(wave, freqHz, fragmentSamples, sampleRate, velocity, Math.min(a, 0.02), Math.min(d, 0.02), s, r, vibratoRate, vibratoDepth);
    }

    public static short[] synthReleaseFromLoop(byte wave, double freqHz, int sampleRate, double releaseSeconds, double velocity, double vibratoRate, double vibratoDepth) {
        int samples = (int) (releaseSeconds * sampleRate);
        if (samples < 1) samples = sampleRate / 8;
        return synthFull(wave, freqHz, samples, sampleRate, velocity, 0.001, 0.001, 1.0, releaseSeconds, vibratoRate, vibratoDepth);
    }

    public static int createALBufferFromShorts(short[] samples, int sampleRate) {
        ShortBuffer sb = BufferUtils.createShortBuffer(samples.length);
        sb.put(samples);
        sb.flip();
        int buf = AL10.alGenBuffers();
        AL10.alBufferData(buf, AL10.AL_FORMAT_MONO16, sb, sampleRate);
        int err = AL10.alGetError();
        if (err != AL10.AL_NO_ERROR) {
            System.err.println("[AL ERROR] alBufferData error " + err);
        }
        return buf;
    }

    private static double fmod(double x, double y) {
        return x - Math.floor(x / y) * y;
    }
}