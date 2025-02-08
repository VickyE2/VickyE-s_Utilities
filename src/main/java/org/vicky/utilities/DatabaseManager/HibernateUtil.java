/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class HibernateUtil {
  public static SessionFactory sessionFactory = null;
  private static ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.HIBERNATE, "UTIL");
  private static EntityManagerFactory ENTITY_MANAGER_FACTORY = null;

  public static void initialise(SQLManager manager) {
    try {
      Thread.sleep(2000L);

      sessionFactory = manager.getSessionFactory();
      sessionFactory.openSession();

      HibernatePersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
      ENTITY_MANAGER_FACTORY =
          persistenceProvider.createEntityManagerFactory("vUtls", sessionFactory.getProperties());

      if (sessionFactory == null) {
        throw new IllegalStateException("SessionFactoryBuilder could not be initialized.");
      }

      EventListenerRegistry eventListenerRegistry =
          ((SessionFactoryImpl) sessionFactory)
              .getServiceRegistry()
              .getService(EventListenerRegistry.class);

      if (eventListenerRegistry == null) {
        throw new IllegalStateException("EventListenerRegistry could not be initialized.");
      }

      eventListenerRegistry.appendListeners(EventType.POST_INSERT, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.POST_UPDATE, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.POST_DELETE, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.REPLICATE, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.REFRESH, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.LOAD, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.POST_LOAD, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.PERSIST, new CustomDatabaseLogger());
      eventListenerRegistry.appendListeners(EventType.MERGE, new CustomDatabaseLogger());

      logger.printBukkit(ANSIColor.colorize("green[Session started successfully]"));
    } catch (Throwable var3) {
      logger.printBukkit(
          ANSIColor.colorize("red[Initial SessionFactory creation failed: " + var3 + "]"), true);
      throw new ExceptionInInitializerError(var3);
    }
  }

  public static EntityManager getEntityManager() {
    return ENTITY_MANAGER_FACTORY.createEntityManager();
  }

  public static void close() {
    ENTITY_MANAGER_FACTORY.close();
  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public static void shutdown() {
    getSessionFactory().close();
  }
}
