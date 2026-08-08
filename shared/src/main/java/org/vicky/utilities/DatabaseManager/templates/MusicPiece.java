/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "RegisteredMusicPieces")
public class MusicPiece implements DatabaseTemplate {
	@Id
	@Column(name = "piece_id", unique = true, nullable = false)
	private String id;

	@NaturalId
	@Column(name = "piece_name", unique = true, nullable = false)
	private String name;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "piece_authors", unique = true, nullable = false)
	private String[] authors;

	@Column(name = "piece_genre", unique = true, nullable = false)
	private String genre;

	public MusicPiece() {
	}

	public MusicPiece(String id, String name, String genre, String[] authors) {
		this.id = id;
		this.name = name;
		this.genre = genre;
		this.authors = authors;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getGenre() {
		return genre;
	}

	public String[] getAuthors() {
		return authors;
	}
}
