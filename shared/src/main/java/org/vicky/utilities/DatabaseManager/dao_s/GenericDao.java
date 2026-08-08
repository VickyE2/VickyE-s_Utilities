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

	public void save(T theme) {
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

	public T update(T theme) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T updatedObject = em.merge(theme);
			transaction.commit();
			return updatedObject;
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	public void delete(K id) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			Optional<T> theme = findById(id);
			theme.ifPresent(em::remove);
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
