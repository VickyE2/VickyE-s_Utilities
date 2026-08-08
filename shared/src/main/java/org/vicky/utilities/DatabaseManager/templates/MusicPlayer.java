/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import org.vicky.musicPlayer.MusicPlayer.MusicPriority;
import org.vicky.utilities.DatabaseTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "MusicPlayer")
public class MusicPlayer extends ExtendedPlayerBase implements DatabaseTemplate {

	@JoinTable(name = "playlists", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "playlist_id"))
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<MusicPlaylist> playlists = new ArrayList<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<OwnedPiece> ownedPieces = new ArrayList<>();

	/**
	 * What {@link MusicPriority} entries are allowed to render on the ui
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "music_player_allowed_priorities", joinColumns = @JoinColumn(name = "player_id"))
	@Column(name = "priority")
	@Enumerated(EnumType.STRING)
	private Set<MusicPriority> allowedPriorities = new HashSet<>();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "last_piece_id")
	private MusicPiece lastPiece;

	@Column(name = "last_tick")
	private int lastTick;

	@PrePersist
	public void initDefaults() {
		if (allowedPriorities == null || allowedPriorities.isEmpty()) {
			allowedPriorities = new HashSet<>();
			allowedPriorities.add(MusicPriority.PLAYER_REQUEST);
		}
	}

	public MusicPlayer() {
	}

	public List<MusicPlaylist> getPlaylists() {
		return playlists;
	}

	public MusicPiece getLastPiece() {
		return lastPiece;
	}

	public List<OwnedPiece> getOwnedPieces() {
		return ownedPieces;
	}

	public void addPiece(MusicPiece piece) {
		if (piece == null)
			return;
		if (ownedPieces.stream().anyMatch(it -> it.getMusicPiece().equals(piece)))
			return;

		OwnedPiece ownedPiece = new OwnedPiece(this, piece);
		this.ownedPieces.add(ownedPiece);
	}

	public Set<MusicPriority> getAllowedPriorities() {
		return allowedPriorities;
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
