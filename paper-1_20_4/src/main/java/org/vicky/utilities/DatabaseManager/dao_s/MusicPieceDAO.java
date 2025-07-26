/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPiece;

public class MusicPieceDAO {

  public MusicPieceDAO() {}

  /**
   * Find a MusicPiece by its UUID.
   *
   * @param id the UUID of the theme
   * @return the MusicPiece instance or null if not found
   */
  public MusicPiece findById(String id) {
    try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
      MusicPiece piece =
          em.createQuery("FROM MusicPiece WHERE id = :id", MusicPiece.class)
              .setParameter("id", id)
              .getSingleResult();
      em.close(); // Close it after usage
      return piece;
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Find all MusicPieces.
   *
   * @return the List of MusicPieces
   */
  public List<MusicPiece> getAll() {
    try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
      List<MusicPiece> pieces =
          em.createQuery("SELECT t FROM MusicPiece t", MusicPiece.class).getResultList();
      em.close();
      return pieces;
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Persist a new MusicPiece.
   *
   * @param theme the MusicPiece to persist
   */
  public void save(MusicPiece theme) {
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
   * Merge (update) an existing MusicPiece.
   *
   * @param theme the MusicPiece to update
   * @return the managed instance of the MusicPiece
   */
  public MusicPiece update(MusicPiece theme) {
    EntityManager em = HibernateUtil.getEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      MusicPiece updatedMusicPiece = em.merge(theme);
      transaction.commit();
      return updatedMusicPiece;
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
   * Delete a MusicPiece by UUID.
   *
   * @param id the UUID of the theme to delete
   */
  public void delete(String id) {
    EntityManager em = HibernateUtil.getEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      MusicPiece theme = findById(id);
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
