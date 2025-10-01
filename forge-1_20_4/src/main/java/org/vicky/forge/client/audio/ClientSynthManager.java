/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.client.audio;

import org.vicky.forge.network.registeredpackets.NoteOnPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSynthManager {
    private static final ClientSynthManager INSTANCE = new ClientSynthManager();
    private final int sampleRate = 44100;
    private final Map<Integer, Integer> activeVoices = new ConcurrentHashMap<>();

    private ClientSynthManager() {
    }

    public static ClientSynthManager get() {
        return INSTANCE;
    }

    public void noteOn(NoteOnPacket pkt) {
        int[] bankProg = pkt.midiId(); // map enum → bank/program
        int velocity = (int) (pkt.velocity() * 127);

        MidiSynthManager.getInstance().noteOn(bankProg[0], bankProg[1], pkt.midiNote(), velocity, 0);
        activeVoices.put(pkt.uid(), pkt.midiNote());
    }

    public void noteOff(Integer uid) {
        Integer midiNote = activeVoices.remove(uid);
        if (midiNote != null) {
            MidiSynthManager.getInstance().noteOff(midiNote, 0);
        }
    }
}