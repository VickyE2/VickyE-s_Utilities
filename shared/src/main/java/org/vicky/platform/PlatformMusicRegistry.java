/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import java.util.Collection;

import org.vicky.music.utils.MusicPiece;

public interface PlatformMusicRegistry {
	void register(MusicPiece piece);

	void playPiece(String key, PlatformPlayer player);

	Collection<MusicPiece> getRegisteredPieces();

	PlatformPlayer renderMusicPage(PlatformPlayer player, int page);

	void loadGenres(); // Load genre → color mappings
}
