/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ownedPieces")
public class OwnedPiece {

	@EmbeddedId
	private OwnedPieceId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("playerId") // Maps to playerId in OwnedPieceId
	@JoinColumn(name = "player_id")
	private MusicPlayer player;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("pieceId") // Maps to pieceId in OwnedPieceId
	@JoinColumn(name = "piece_id")
	private MusicPiece musicPiece;

	@Column(name = "acquired_at", nullable = false)
	private LocalDateTime acquiredAt;

	@Column(name = "favorite", nullable = false)
	private boolean favorite;

	public OwnedPiece() {
	}

	public OwnedPiece(MusicPlayer player, MusicPiece musicPiece) {
		this.player = player;
		this.musicPiece = musicPiece;
		this.id = new OwnedPieceId(player.getId(), musicPiece.getId());
		this.acquiredAt = LocalDateTime.now(); // Automatically sets the timestamp when created
		this.favorite = false;
	}

	public LocalDateTime getAcquiredAt() {
		return acquiredAt;
	}

	public MusicPiece getMusicPiece() {
		return musicPiece;
	}

	public MusicPlayer getPlayer() {
		return player;
	}

	public OwnedPieceId getId() {
		return id;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
}