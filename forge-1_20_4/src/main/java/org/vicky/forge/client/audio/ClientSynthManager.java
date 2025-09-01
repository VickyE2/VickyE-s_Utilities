package org.vicky.forge.client.audio;

import org.vicky.forge.network.registeredpackets.NoteOnPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSynthManager {
    private static final ClientSynthManager INSTANCE = new ClientSynthManager();
    private final int sampleRate = 44100;
    private final Map<UUID, Integer> activeVoices = new ConcurrentHashMap<>();
    private final Map<Long, Integer> bufferCache = new ConcurrentHashMap<>();

    private ClientSynthManager() {
    }

    public static ClientSynthManager get() {
        return INSTANCE;
    }

    public void noteOn(NoteOnPacket pkt) {
        int[] bankProg = pkt.midiId(); // map enum → bank/program
        int midiNote = (int) (69 + 12 * (Math.log(pkt.freqHz() / 440.0) / Math.log(2))); // convert Hz → MIDI note
        int velocity = (int) (pkt.velocity() * 127);

        MidiSynthManager.getInstance().noteOn(bankProg[0], bankProg[1], midiNote, velocity, 0);
        activeVoices.put(pkt.uid(), midiNote);
    }

    public void noteOff(UUID uid) {
        Integer midiNote = activeVoices.remove(uid);
        if (midiNote != null) {
            MidiSynthManager.getInstance().noteOff(midiNote, 0);
        }
    }
}