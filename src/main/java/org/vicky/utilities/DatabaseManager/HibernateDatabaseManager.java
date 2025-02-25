/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class HibernateDatabaseManager {
  private static ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.HIBERNATE, "UTIL");

  public <T> void saveEntity(T entity) {
    Transaction transaction = null;
    Session session = HibernateUtil.sessionFactory.openSession();

    try {
      transaction = session.beginTransaction();
      session.merge(entity);
      transaction.commit();
    } catch (Exception var8) {
      logger.printBukkit("Error during entity saving: " + var8, true);
      if (transaction != null) {
        transaction.rollback();
      }

      var8.printStackTrace();
      throw new RuntimeException("Error during entity saving: " + var8);
    } finally {
      session.clear();
      session.close();
    }
  }

  public <T> T getEntityById(Class<T> clazz, Object id) {
    Session session = HibernateUtil.sessionFactory.openSession();

    Object e;
    try {
      e = session.get(clazz, id);
    } catch (Exception var8) {
      throw new RuntimeException("Error during entity saving: " + var8);
    } finally {
      session.close();
    }

    return (T) e;
  }

  public <T> T getEntityByNaturalId(Class<T> clazz, Object id) {
    Session session = HibernateUtil.sessionFactory.openSession();

    T e;
    try {
      e = session.byNaturalId(clazz).using("key", id).load();
    } catch (Exception var8) {
      throw new RuntimeException("Error during entity saving: " + var8);
    } finally {
      session.close();
    }

    return e;
  }

  public <T> void updateEntity(T entity) {
    Transaction transaction = null;
    Session session = HibernateUtil.sessionFactory.openSession();

    try {
      transaction = session.beginTransaction();
      session.merge(entity);
      transaction.commit();
    } catch (Exception var8) {
      logger.printBukkit("Error during entity updating:" + var8, true);
      if (transaction != null) {
        transaction.rollback();
      }

      var8.printStackTrace();
      throw new RuntimeException("Error during entity saving: " + var8);
    } finally {
      session.close();
    }
  }

  public <T> void saveOrUpdate(T entity) {
    Transaction transaction = null;
    Session session = HibernateUtil.sessionFactory.openSession();

    try {
      transaction = session.beginTransaction();
      session.merge(entity);
      transaction.commit();
    } catch (Exception var8) {
      logger.printBukkit("Error during entity updating:" + var8, true);
      if (transaction != null) {
        transaction.rollback();
      }

      var8.printStackTrace();
      throw new RuntimeException("Error during entity saving: " + var8);
    } finally {
      session.close();
    }
  }

  public <T> boolean entityExists(Class<T> clazz, Object id) {
    Session session = HibernateUtil.sessionFactory.openSession();

    boolean queryString;
    try {
      String entityName = clazz.getSimpleName();
      String queryStringx = "SELECT COUNT(e) FROM " + entityName + " e WHERE e.id = :id";
      Long count =
          session.createQuery(queryStringx, Long.class).setParameter("id", id).uniqueResult();
      return count != null && count > 0L;
    } catch (Exception var11) {
      logger.printBukkit("Error during entity query: " + var11, true);
      var11.printStackTrace();
      queryString = false;
    } finally {
      session.close();
    }

    return queryString;
  }

  public <T> boolean entityExistsByNaturalId(
      Class<T> clazz, String naturalIdProperty, Object naturalIdValue) {
    Session session = HibernateUtil.sessionFactory.openSession();

    boolean var6;
    try {
      session.beginTransaction();
      T result = session.byNaturalId(clazz).using(naturalIdProperty, naturalIdValue).load();
      session.getTransaction().commit();
      return result != null;
    } catch (Exception var10) {
      logger.printBukkit("Error during natural ID query: " + var10, true);
      var10.printStackTrace();
      var6 = false;
    } finally {
      session.close();
    }

    return var6;
  }

  public <T> void deleteEntity(T entity) {
    Transaction transaction = null;
    Session session = HibernateUtil.sessionFactory.openSession();

    try {
      transaction = session.beginTransaction();
      session.remove(entity);
      transaction.commit();
    } catch (Exception var8) {
      logger.printBukkit("Error during entity deletion: " + var8, true);
      if (transaction != null) {
        transaction.rollback();
      }

      var8.printStackTrace();
    } finally {
      session.close();
    }
  }
}
