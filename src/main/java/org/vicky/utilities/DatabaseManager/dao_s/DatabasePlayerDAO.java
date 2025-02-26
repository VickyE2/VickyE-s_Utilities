/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.UUID;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

public class DatabasePlayerDAO {

  public DatabasePlayerDAO() {}

  /**
   * Find a DatabasePlayer by its UUID.
   *
   * @param id the UUID of the player
   * @return the DatabasePlayer instance or null if not found
   */
  public DatabasePlayer findById(UUID id) {
    EntityManager em = HibernateUtil.getEntityManager(); // Open new EntityManager
    DatabasePlayer player = em.find(DatabasePlayer.class, id.toString());
    em.close(); // Close it after usage
    return player;
  }

  /**
   * Persist a new DatabasePlayer.
   *
   * @param player the DatabasePlayer to persist
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
   * @param player the DatabasePlayer to update
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
   * @param id the UUID of the player to delete
   */
  public void delete(UUID id) {
    EntityManager em = HibernateUtil.getEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      DatabasePlayer player = findById(id);
      if (player != null) {
        em.remove(player);
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
