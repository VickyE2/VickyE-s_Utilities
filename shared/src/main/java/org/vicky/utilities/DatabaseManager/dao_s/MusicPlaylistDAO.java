/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPlaylist;

import java.util.List;
import java.util.Optional;

public class MusicPlaylistDAO extends GenericDao<MusicPlaylist, String> {

	public MusicPlaylistDAO() {
	}

	/**
	 * Find a MusicPlaylist by its UUID.
	 *
	 * @param id
	 *            the UUID of the theme
	 * @return the MusicPlaylist instance or null if not found
	 */
	public Optional<MusicPlaylist> findById(String id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			MusicPlaylist piece = em.createQuery("FROM MusicPlaylist WHERE playlistId = :id", MusicPlaylist.class)
					.setParameter("id", id).getSingleResult();
			em.close(); // Close it after usage
			return Optional.of(piece);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	/**
	 * Find all MusicPlaylists.
	 *
	 * @return the List of MusicPlaylists
	 */
	public List<MusicPlaylist> getAll() {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			List<MusicPlaylist> pieces = em.createQuery("SELECT t FROM MusicPlaylist t", MusicPlaylist.class)
					.getResultList();
			em.close();
			return pieces;
		} catch (NoResultException e) {
			return null;
		}
	}
}
