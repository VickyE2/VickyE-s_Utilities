package org.vicky.platform;

import org.vicky.music.utils.MusicPiece;

import java.util.Collection;

public interface PlatformMusicRegistry {
    void register(MusicPiece piece);
    void playPiece(String key, PlatformPlayer player);
    Collection<MusicPiece> getRegisteredPieces();
    PlatformPlayer renderMusicPage(PlatformPlayer player, int page);
    void loadGenres(); // Load genre → color mappings
}