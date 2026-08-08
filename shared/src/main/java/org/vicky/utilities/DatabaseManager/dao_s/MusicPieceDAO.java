/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPiece;

import java.util.List;
import java.util.Optional;

public class MusicPieceDAO extends GenericDao<MusicPiece, String> {

	public MusicPieceDAO() {
	}

	/**
	 * Find a MusicPiece by its UUID.
	 *
	 * @param id
	 *            the UUID of the theme
	 * @return the MusicPiece instance or null if not found
	 */
	public Optional<MusicPiece> findById(String id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			MusicPiece piece = em.createQuery("FROM MusicPiece WHERE id = :id", MusicPiece.class).setParameter("id", id)
					.getSingleResult();
			em.close(); // Close it after usage
			return Optional.of(piece);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	/**
	 * Find all MusicPieces.
	 *
	 * @return the List of MusicPieces
	 */
	public List<MusicPiece> getAll() {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			List<MusicPiece> pieces = em.createQuery("SELECT t FROM MusicPiece t", MusicPiece.class).getResultList();
			em.close();
			return pieces;
		} catch (NoResultException e) {
			return null;
		}
	}
}
