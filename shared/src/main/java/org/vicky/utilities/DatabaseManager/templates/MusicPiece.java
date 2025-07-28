/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.templates;

import org.vicky.utilities.DatabaseTemplate;

import jakarta.persistence.*;

@Entity
@Table(name = "RegisteredMusicPieces")
public class MusicPiece implements DatabaseTemplate {
	@Id
	@Column(name = "piece_id", unique = true, nullable = false)
	private String id;

	public MusicPiece() {
	}

	public MusicPiece(String id) {
		this.id = id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
