package org.vicky.forge.client.audio;

// package com.example.mymod.client.audio;

import org.lwjgl.openal.AL10;

import java.util.UUID;

public class SynthVoice {
    public int source = -1;
    public int cachedBufferId = -1;
    public boolean looping = false;
    public UUID uid;
    public byte loopWave;
    public double loopFreq;
    public double releaseSeconds;
    public double velocity;
    public double vibratoRate;
    public double vibratoDepth;

    public SynthVoice() {
        source = AL10.alGenSources();
    }

    public void playBufferLoop(int bufferId) {
        stopIfPlaying();
        AL10.alSourcei(source, AL10.AL_BUFFER, bufferId);
        AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
        AL10.alSourcePlay(source);
        looping = true;
        cachedBufferId = bufferId;
    }

    public void playBufferOnce(int bufferId) {
        stopIfPlaying();
        AL10.alSourcei(source, AL10.AL_BUFFER, bufferId);
        AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_FALSE);
        AL10.alSourcePlay(source);
        looping = false;
        cachedBufferId = bufferId;
    }

    public boolean isLooping() {
        return looping;
    }

    public void stopLoop() {
        if (looping) {
            AL10.alSourceStop(source);
            AL10.alSourcei(source, AL10.AL_BUFFER, 0);
            looping = false;
            // do NOT delete buffer here if shared; manage lifecycle in manager
        }
    }

    public void stopAndFree() {
        AL10.alSourceStop(source);
        if (cachedBufferId != -1) {
            AL10.alDeleteBuffers(cachedBufferId);
            cachedBufferId = -1;
        }
        // NOTE: we're not deleting sources to reuse them; but you can if you want
    }

    private void stopIfPlaying() {
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_PLAYING || state == AL10.AL_PAUSED) AL10.alSourceStop(source);
    }
}
