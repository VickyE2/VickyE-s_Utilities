/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.UUID;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.Theme;

public class ThemeDAO {

  public ThemeDAO() {}

  /**
   * Find a Theme by its UUID.
   *
   * @param id the UUID of the theme
   * @return the Theme instance or null if not found
   */
  public Theme findById(String id) {
    try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
      Theme theme =
          em.createQuery("FROM Theme WHERE id = :id", Theme.class)
              .setParameter("id", id)
              .getSingleResult();
      em.close(); // Close it after usage
      return theme;
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Find a Theme by its unique name.
   *
   * @param name the name of the theme
   * @return the Theme instance or null if not found
   */
  public Theme findByName(String name) {
    try (EntityManager em = HibernateUtil.getEntityManager()) {
      // Open new EntityManager
      Theme theme =
          em.createQuery("FROM Theme WHERE name = :name", Theme.class)
              .setParameter("name", name)
              .getSingleResult();
      return theme;
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Persist a new Theme.
   *
   * @param theme the Theme to persist
   */
  public void save(Theme theme) {
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
   * Merge (update) an existing Theme.
   *
   * @param theme the Theme to update
   * @return the managed instance of the Theme
   */
  public Theme update(Theme theme) {
    EntityManager em = HibernateUtil.getEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      Theme updatedTheme = em.merge(theme);
      transaction.commit();
      return updatedTheme;
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
   * Delete a Theme by UUID.
   *
   * @param id the UUID of the theme to delete
   */
  public void delete(String id) {
    EntityManager em = HibernateUtil.getEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      Theme theme = findById(id);
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
