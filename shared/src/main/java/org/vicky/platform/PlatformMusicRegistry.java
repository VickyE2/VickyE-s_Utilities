/* Licensed under Apache-2.0 2026. */
package org.vicky.platform;

import org.vicky.music.utils.MusicPiece;
import org.vicky.platform.player.PlatformPlayer;

import java.util.Collection;

public interface PlatformMusicRegistry {
	void register(MusicPiece piece);
	void playPiece(String key, PlatformPlayer player);
	Collection<MusicPiece> getRegisteredPieces();
	PlatformPlayer renderMusicPage(PlatformPlayer player, int page);

	void loadGenres();
}