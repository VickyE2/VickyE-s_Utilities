/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "MusicPlayer")
public class MusicPlayer extends ExtendedPlayerBase implements DatabaseTemplate {

  @JoinTable(
      name = "playlists",
      joinColumns = @JoinColumn(name = "player_id"),
      inverseJoinColumns = @JoinColumn(name = "playlist_id"))
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<MusicPlaylist> playlists = new ArrayList<>();

  @JoinTable(
      name = "ownedPieces",
      joinColumns = @JoinColumn(name = "player_id"),
      inverseJoinColumns = @JoinColumn(name = "piece_id"))
  @OneToMany(fetch = FetchType.EAGER)
  private List<MusicPiece> ownedPieces = new ArrayList<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "last_piece_id")
  private MusicPiece lastPiece;

  @Column(name = "last_tick")
  private int lastTick;

  public MusicPlayer() {}

  public List<MusicPlaylist> getPlaylists() {
    return playlists;
  }

  public MusicPiece getLastPiece() {
    return lastPiece;
  }

  public List<MusicPiece> getOwnedPieces() {
    return ownedPieces;
  }

  public void addPiece(MusicPiece piece) {
    if (piece == null) return;
    if (!ownedPieces.contains(piece)) {
      ownedPieces.add(piece);
    }
  }

  public void setLastPiece(MusicPiece lastPiece) {
    this.lastPiece = lastPiece;
  }

  public int getLastTick() {
    return lastTick;
  }

  public void setLastTick(int lastTick) {
    this.lastTick = lastTick;
  }
}
