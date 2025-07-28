/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.dao_s;

import java.util.Optional;
import java.util.UUID;

import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class DatabasePlayerDAO {

	public DatabasePlayerDAO() {
	}

	/**
	 * Find a DatabasePlayer by its UUID.
	 *
	 * @param id
	 *            the UUID of the player
	 * @return the DatabasePlayer instance or null if not found
	 */
	public Optional<DatabasePlayer> findById(UUID id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			return Optional.ofNullable(em.find(DatabasePlayer.class, id.toString()));
		}
	}

	/**
	 * Persist a new DatabasePlayer.
	 *
	 * @param player
	 *            the DatabasePlayer to persist
	 */
	public void save(DatabasePlayer player) {
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
	 * Merge (update) an existing DatabasePlayer.
	 *
	 * @param player
	 *            the DatabasePlayer to update
	 * @return the managed instance of the DatabasePlayer
	 */
	public DatabasePlayer update(DatabasePlayer player) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			DatabasePlayer updatedPlayer = em.merge(player);
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
	 * Delete a DatabasePlayer by UUID.
	 *
	 * @param id
	 *            the UUID of the player to delete
	 */
	public void delete(UUID id) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			Optional<DatabasePlayer> player = findById(id);
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
