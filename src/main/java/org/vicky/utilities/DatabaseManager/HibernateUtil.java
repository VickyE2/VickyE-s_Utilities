package org.vicky.utilities.DatabaseManager;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class HibernateUtil {
    public static SessionFactory sessionFactory = null;
    private static ContextLogger logger = new ContextLogger(ContextLogger.ContextType.HIBERNATE, "UTIL");
    public static void initialise(SQLManager manager) {
        try {
            Thread.sleep(2000L);

            sessionFactory = manager.getSessionFactory();
            sessionFactory.openSession();

            if (sessionFactory == null) {
                throw new IllegalStateException("SessionFactoryBuilder could not be initialized.");
            }

            EventListenerRegistry eventListenerRegistry = ((SessionFactoryImpl) sessionFactory)
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
            eventListenerRegistry.appendListeners(EventType.SAVE_UPDATE, new CustomDatabaseLogger());
            eventListenerRegistry.appendListeners(EventType.SAVE, new CustomDatabaseLogger());
            eventListenerRegistry.appendListeners(EventType.UPDATE, new CustomDatabaseLogger());
            eventListenerRegistry.appendListeners(EventType.MERGE, new CustomDatabaseLogger());

            logger.printBukkit(ANSIColor.colorize("green[Session started successfully]"));
        } catch (Throwable var3) {
            logger.printBukkit(ANSIColor.colorize("red[Initial SessionFactory creation failed: " + var3 + "]"), true);
            throw new ExceptionInInitializerError(var3);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
