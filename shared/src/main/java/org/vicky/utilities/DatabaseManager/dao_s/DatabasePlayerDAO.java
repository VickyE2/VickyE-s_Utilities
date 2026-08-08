/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabasePlayerDAO extends GenericDao<DatabasePlayer, UUID> {

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

	@Override
	public List<DatabasePlayer> getAll() {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			List<DatabasePlayer> pieces = em.createQuery("SELECT t FROM DatabasePlayer t", DatabasePlayer.class)
					.getResultList();
			em.close();
			return pieces;
		} catch (NoResultException e) {
			return null;
		}
	}
}
