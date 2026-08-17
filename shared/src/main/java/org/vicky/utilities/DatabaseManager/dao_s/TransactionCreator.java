/* Licensed under Apache-2.0 2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.vicky.utilities.DatabaseManager.HibernateUtil;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TransactionCreator {
	public static void transaction(BiConsumer<EntityManager, EntityTransaction> work) {
		transaction((em, trx) -> {
			work.accept(em, trx);
			return null;
		});
	}
	public static void transaction(Consumer<EntityManager> work) {
		transaction((em, ignored) -> {
			work.accept(em);
			return null;
		});
	}

	public static <R> R transaction(BiFunction<EntityManager, EntityTransaction, R> work) {
		EntityManager em = HibernateUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();

			R result = work.apply(em, tx);

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
