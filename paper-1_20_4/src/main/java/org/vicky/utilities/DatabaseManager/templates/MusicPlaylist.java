/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "CreatedPlaylists")
public class MusicPlaylist implements DatabaseTemplate {
  @JoinTable(
      name = "pieces",
      joinColumns = @JoinColumn(name = "piece_id"),
      inverseJoinColumns = @JoinColumn(name = "playlist_id"))
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<MusicPiece> musicPieces = new ArrayList<>();

  @Id
  @Column(name = "playlist_id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private String playlistId;

  public MusicPlaylist() {}

  public List<MusicPiece> getMusicPieces() {
    return musicPieces;
  }

  public String getPlaylistId() {
    return playlistId;
  }

  public void setMusicPieces(List<MusicPiece> musicPieces) {
    this.musicPieces = musicPieces;
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }
}
