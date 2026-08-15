/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import java.util.List;
import java.util.Optional;

import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public abstract class GenericDao<T extends DatabaseTemplate, K> {

	public abstract Optional<T> findById(K id);

	public abstract List<T> getAll();

	public void save(T theme) {
		save(HibernateUtil.getEntityManager(), theme);
	}
	public void save(EntityManager em, T theme) {
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
		return update(HibernateUtil.getEntityManager(), theme);
	}
	public T update(EntityManager em, T theme) {
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
		delete(HibernateUtil.getEntityManager(), id);
	}
	public void delete(EntityManager em, K id) {
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
