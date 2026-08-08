/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.music.utils;

import java.util.ArrayList;
import java.util.List;

public record MusicPiece(String key, String pieceName, List<MusicTrack> trackList, String[] authors, String genre,
		int themeColorHex // e.g. "#b25bb4"
) {

	public MusicPiece {
		trackList = trackList != null ? trackList : new ArrayList<>();
	}

	public MusicPiece(String key, String pieceName, MusicComposition trackList, String[] authors, String genre,
			int themeColorHex // e.g. "#b25bb4"
	) {
		this(key, pieceName, trackList.voices(), authors, genre, themeColorHex);
	}

	public void addPart(MusicTrack musicTrack) {
		this.trackList.add(musicTrack);
	}

	public long totalDuration() {
		return trackList.stream().flatMap(t -> t.getEvents().stream()).mapToLong(MusicEvent::timeOffset).max()
				.orElse(0L);
	}
}
