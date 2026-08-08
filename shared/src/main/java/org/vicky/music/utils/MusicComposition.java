/* Licensed under Apache-2.0 2026. */
package org.vicky.music.utils;

import java.util.ArrayList;
import java.util.List;

public class MusicComposition {
	private final List<MusicTrack> voices = new ArrayList<>();

	public MusicComposition addVoice(MusicTrack track) {
		voices.add(track);
		return this;
	}

	public List<MusicTrack> voices() {
		return voices;
	}
}