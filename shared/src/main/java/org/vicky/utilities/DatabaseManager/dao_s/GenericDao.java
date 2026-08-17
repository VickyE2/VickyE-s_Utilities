/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseTemplate;

import java.util.List;
import java.util.Optional;

public abstract class GenericDao<T extends DatabaseTemplate, K> {

	public abstract Optional<T> findById(K id);

	public abstract List<T> getAll();

	public void save(T entity) {
		TransactionCreator.transaction(em -> save(em, entity));
	}
	public void save(EntityManager em, T entity) {
		em.persist(entity);
	}

	public void update(T entity) {
		TransactionCreator.transaction(em -> update(em, entity));
	}
	public T update(EntityManager em, T entity) {
		return em.merge(entity);
	}

	public void delete(K id) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			Optional<T> entity = findById(id);
			entity.ifPresent(em::remove);
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
	public void delete(EntityManager em, K id) {
		Optional<T> entity = findById(id);
		entity.ifPresent(em::remove);
	}
}
