/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OwnedPieceId implements Serializable {
	private String playerId;
	private String pieceId;

	public OwnedPieceId() {
	}

	public OwnedPieceId(String playerId, String pieceId) {
		this.playerId = playerId;
		this.pieceId = pieceId;
	}

	public String getPieceId() {
		return pieceId;
	}

	public String getPlayerId() {
		return playerId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OwnedPieceId that))
			return false;
		return Objects.equals(playerId, that.playerId) && Objects.equals(pieceId, that.pieceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerId, pieceId);
	}
}