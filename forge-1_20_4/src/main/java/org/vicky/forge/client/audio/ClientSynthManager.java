package org.vicky.forge.client.audio;

import org.vicky.forge.network.packets.NoteOnPacket;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientSynthManager {
    private static final ClientSynthManager INSTANCE = new ClientSynthManager();
    private final int sampleRate = 44100;
    private final int maxVoices = 32;
    private final Queue<SynthVoice> freeVoices = new ConcurrentLinkedQueue<>();
    private final Map<UUID, SynthVoice> activeVoices = new ConcurrentHashMap<>();
    // simple cache keyed by parameter hash -> AL buffer id
    private final Map<Long, Integer> bufferCache = new ConcurrentHashMap<>();
    private ClientSynthManager() {
        // populate voice pool
        for (int i = 0; i < maxVoices; i++) freeVoices.add(new SynthVoice());
    }

    public static ClientSynthManager get() {
        return INSTANCE;
    }

    public void noteOn(NoteOnPacket pkt) {
        // deterministically create a short[] pcm or reuse cached AL buffer if not sustainLoop
        long key = SynthUtils.hashParams(pkt.wave(), pkt.freqHz(), pkt.attack(), pkt.decay(), pkt.sustain(), pkt.release(), pkt.vibratoRate(), pkt.vibratoDepth(), pkt.velocity());
        SynthVoice voice = freeVoices.poll();
        if (voice == null) {
            // voice stealing: pick a random active or oldest active
            voice = activeVoices.values().stream().findFirst().orElse(null);
            if (voice == null) return;
            voice.stopAndFree();
        }

        activeVoices.put(pkt.uid(), voice);
        voice.uid = pkt.uid();
        voice.velocity = pkt.velocity();

        // If sustainLoop = true, we render a short loop buffer for sustain region and play it looped,
        // then when noteOff arrives we transition to release via a fresh buffer.
        if (pkt.sustainLoop()) {
            // generate a loopable sustain fragment (e.g., 0.25s)
            int loopSamples = sampleRate / 4;
            short[] loopPcm = SynthUtils.synthFragment(pkt.wave(), pkt.freqHz(), loopSamples, sampleRate, pkt.velocity(), pkt.attack(), pkt.decay(), pkt.sustain(), pkt.release(), pkt.vibratoRate(), pkt.vibratoDepth());
            int bufferId = SynthUtils.createALBufferFromShorts(loopPcm, sampleRate);
            voice.playBufferLoop(bufferId);
            // store buffer so on noteOff we can trigger release
            voice.cachedBufferId = bufferId;
            bufferCache.put(key, bufferId); // optional
        } else {
            // render full note (attack+decay+sustain_duration+release)
            float totalSec = pkt.attack() + pkt.decay() + pkt.sustain() + pkt.release();
            int totalSamples = Math.max(1, (int) (totalSec * sampleRate));
            short[] pcm = SynthUtils.synthFull(pkt.wave(), pkt.freqHz(), totalSamples, sampleRate, pkt.velocity(), pkt.attack(), pkt.decay(), pkt.sustain(), pkt.release(), pkt.vibratoRate(), pkt.vibratoDepth());
            int bufferId = SynthUtils.createALBufferFromShorts(pcm, sampleRate);
            voice.playBufferOnce(bufferId);
            voice.cachedBufferId = bufferId;
            // schedule freeing when done (voice will free when OpenAL source stopped)
        }
    }

    public void noteOff(UUID uid) {
        SynthVoice voice = activeVoices.remove(uid);
        if (voice == null) return;
        // If voice was looping sustain, we need to generate a release buffer and crossfade
        if (voice.isLooping()) {
            // produce a release buffer using remaining release envelope and play once, plus stop loop
            short[] releasePcm = SynthUtils.synthReleaseFromLoop(voice.loopWave, voice.loopFreq, sampleRate, voice.releaseSeconds, voice.velocity, voice.vibratoRate, voice.vibratoDepth);
            int releaseBuf = SynthUtils.createALBufferFromShorts(releasePcm, sampleRate);
            voice.stopLoop();
            voice.playBufferOnce(releaseBuf);
            // will free releaseBuf when play ends
        } else {
            // if playing non-loop buffer, we can let it run out or cut it short — here we release immediately
            voice.stopAndFree();
        }
        freeVoices.add(voice);
    }
}