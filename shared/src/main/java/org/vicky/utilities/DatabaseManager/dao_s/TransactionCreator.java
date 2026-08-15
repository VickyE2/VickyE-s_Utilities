/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import java.util.function.Consumer;
import java.util.function.Function;

import org.vicky.utilities.DatabaseManager.HibernateUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TransactionCreator {
	public static void transaction(Consumer<EntityManager> work) {
		transaction(em -> {
			work.accept(em);
			return null;
		});
	}

	public static <R> R transaction(Function<EntityManager, R> work) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();

			R result = work.apply(em);

			tx.commit();
			return result;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}
}
