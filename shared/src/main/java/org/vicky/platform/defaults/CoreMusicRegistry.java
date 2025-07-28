/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.defaults;

import java.util.*;

import org.vicky.music.utils.MusicPiece;
import org.vicky.musicPlayer.MusicPlayer;

public class CoreMusicRegistry {
	protected final List<MusicPiece> musicPieces = new ArrayList<>();
	protected final MusicPlayer player = MusicPlayer.INSTANCE;

	public void register(MusicPiece piece) {
		musicPieces.add(piece);
		// Save to DB
	}

	public Collection<MusicPiece> getRegistered() {
		return Collections.unmodifiableList(musicPieces);
	}

	public Optional<MusicPiece> getPiece(String key) {
		return musicPieces.stream().filter(p -> p.key().equals(key)).findFirst();
	}

	public MusicPlayer getPlayer() {
		return player;
	}
}
