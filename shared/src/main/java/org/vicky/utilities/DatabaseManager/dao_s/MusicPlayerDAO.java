/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.dao_s;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPlayer;
import org.vicky.utilities.DatabaseManager.templates.MusicPlaylist;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class MusicPlayerDAO {

	public MusicPlayerDAO() {
	}

	/**
	 * Find a MusicPlayer by its UUID.
	 *
	 * @param id
	 *            the UUID of the player
	 * @return the MusicPlayer instance or null if not found
	 */
	public Optional<MusicPlayer> findById(UUID id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			return Optional.ofNullable(em.find(MusicPlayer.class, id.toString()));
		}
	}

	public List<MusicPlaylist> getPlaylistsFor(UUID id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			List<MusicPlaylist> playlists = em
					.createQuery("FROM MusicPlaylist WHERE player_id = :id", MusicPlaylist.class).setParameter("id", id)
					.getResultList();
			em.close();
			return playlists;
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * Persist a new MusicPlayer.
	 *
	 * @param player
	 *            the MusicPlayer to persist
	 */
	public void save(MusicPlayer player) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.persist(player);
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
	 * Merge (update) an existing MusicPlayer.
	 *
	 * @param player
	 *            the MusicPlayer to update
	 * @return the managed instance of the MusicPlayer
	 */
	public MusicPlayer update(MusicPlayer player) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			MusicPlayer updatedPlayer = em.merge(player);
			transaction.commit();
			return updatedPlayer;
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
	 * Delete a MusicPlayer by UUID.
	 *
	 * @param id
	 *            the UUID of the player to delete
	 */
	public void delete(UUID id) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			Optional<MusicPlayer> player = findById(id);
			player.ifPresent(em::remove);
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
