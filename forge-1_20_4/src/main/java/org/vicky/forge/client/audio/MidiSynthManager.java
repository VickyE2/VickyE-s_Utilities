package org.vicky.forge.client.audio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.vicky.platform.PlatformPlugin;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class MidiSynthManager {
    private static MidiSynthManager INSTANCE;
    private final Synthesizer synth;
    private final Soundbank soundbank;
    private final MidiChannel[] channels;

    private MidiSynthManager(ResourceManager manager) throws Exception {
        synth = MidiSystem.getSynthesizer();
        synth.open();
        channels = synth.getChannels();
        var resource = manager.getResourceOrThrow(ResourceLocation.fromNamespaceAndPath("vicky_music", "synth/default_synth.sf2"));
        soundbank = MidiSystem.getSoundbank(resource.open());
        if (synth.isSoundbankSupported(soundbank)) {
            synth.loadAllInstruments(soundbank);
            PlatformPlugin.logger().info("[MIDI] Custom SoundFont loaded");
        } else {
            PlatformPlugin.logger().error("[MIDI] SoundFont not supported!");
        }
    }

    public static void createInstance(ResourceManager manager) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new MidiSynthManager(manager);
        } else {
            throw new IllegalStateException("MidiSynth is already initialized");
        }
    }

    public static MidiSynthManager getInstance() {
        return INSTANCE;
    }

    public void noteOn(int bank, int program, int note, int velocity, int channelIndex) {
        MidiChannel ch = channels[channelIndex % channels.length];
        ch.programChange(bank, program);
        ch.noteOn(note, velocity);
    }

    public void noteOff(int note, int channelIndex) {
        MidiChannel ch = channels[channelIndex % channels.length];
        ch.noteOff(note);
    }

    public void close() {
        synth.close();
    }
}
