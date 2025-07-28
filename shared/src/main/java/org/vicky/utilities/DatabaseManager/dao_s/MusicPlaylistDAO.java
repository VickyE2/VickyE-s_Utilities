/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.dao_s;

import java.util.List;

import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPlaylist;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class MusicPlaylistDAO {

	public MusicPlaylistDAO() {
	}

	/**
	 * Find a MusicPlaylist by its UUID.
	 *
	 * @param id
	 *            the UUID of the theme
	 * @return the MusicPlaylist instance or null if not found
	 */
	public MusicPlaylist findById(String id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			MusicPlaylist piece = em.createQuery("FROM MusicPlaylist WHERE playlistId = :id", MusicPlaylist.class)
					.setParameter("id", id).getSingleResult();
			em.close(); // Close it after usage
			return piece;
		} catch (NoResultException e) {
			return null;
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

	/**
	 * Persist a new MusicPlaylist.
	 *
	 * @param theme
	 *            the MusicPlaylist to persist
	 */
	public void save(MusicPlaylist theme) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.persist(theme);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	/**
	 * Merge (update) an existing MusicPlaylist.
	 *
	 * @param theme
	 *            the MusicPlaylist to update
	 * @return the managed instance of the MusicPlaylist
	 */
	public MusicPlaylist update(MusicPlaylist theme) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			MusicPlaylist updatedMusicPlaylist = em.merge(theme);
			transaction.commit();
			return updatedMusicPlaylist;
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	/**
	 * Delete a MusicPlaylist by UUID.
	 *
	 * @param id
	 *            the UUID of the theme to delete
	 */
	public void delete(String id) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			MusicPlaylist theme = findById(id);
			if (theme != null) {
				em.remove(theme);
			}
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}
}
