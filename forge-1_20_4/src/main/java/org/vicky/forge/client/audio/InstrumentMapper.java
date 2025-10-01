/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.client.audio;

import org.vicky.music.utils.Sound;

public class InstrumentMapper {
    public static int[] getBankAndProgram(Sound sound) {
        return switch (sound) {
            case PIANO -> new int[]{0, 0};
            case RHODES_PIANO -> new int[]{0, 4};
            case CHORUSED_PIANO -> new int[]{0, 5};
            case GUITAR -> new int[]{0, 24};
            case ACOUSTIC_STEEL -> new int[]{0, 25};
            case OVER_DRIVEN -> new int[]{0, 29};
            case DISTORTION -> new int[]{0, 30};
            case VIOLIN -> new int[]{0, 40};
            case VIOLA -> new int[]{0, 41};
            case CELLO -> new int[]{0, 42};
            case STRINGS -> new int[]{0, 45};
            case HARP -> new int[]{0, 46};
            case SAX -> new int[]{0, 65};
            case FLUTE -> new int[]{0, 73};
            case PAN_FLUTE -> new int[]{0, 75};
            case TRUMPET -> new int[]{0, 56};
            case MUTED_TRUMPET -> new int[]{0, 59};
            case TROMBONE -> new int[]{0, 57};
            case BRASS -> new int[]{0, 61};
            case LEAD_CHIFF -> new int[]{0, 83};
            case LEAD_BASS -> new int[]{0, 87};
        };
    }
}
