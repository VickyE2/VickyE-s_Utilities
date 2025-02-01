package org.vicky.utilities.DatabaseManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.RandomStringGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLManager {
    public static RandomStringGenerator generator = RandomStringGenerator.getInstance();
    private String jdbcUrl;
    private final String username;
    private final String password;
    private DatabaseCreator database;
    private final String dialect;
    private final boolean showSql;
    private final boolean formatSql;
    private final String ddlAuto;
    private final List<Class<?>> mappingClasses;
    private SessionFactory sessionFactory;
    private ContextLogger logger = new ContextLogger(ContextLogger.ContextType.HIBERNATE, "MANAGER");

    public void addMappingClass(Class<?> clazz) {
        mappingClasses.add(clazz);
    }

    public Properties loadCredentials() {
        Properties properties = new Properties();

        try (FileReader reader = new FileReader("./plugins/Vicky-s_Utilities/configs/db_credentials.properties")) {
            properties.load(reader);
        } catch (IOException var7) {
            logger.printBukkit("Default credentials are absent...recreating", true);
        }

        return properties;
    }

    public SQLManager(String username, String password, String dialect,
                      boolean showSql, boolean formatSql, String ddlAuto, List<Class<?>> mappingClasses) {
        Properties dbCredentials = loadCredentials();
        this.username = dbCredentials.getOrDefault("username", username).toString();
        this.password = dbCredentials.getOrDefault("password", password).toString();
        dbCredentials.setProperty("username", this.username);
        dbCredentials.setProperty("password", this.password);
        try {
            File credentials = new File("./plugins/Vicky-s_Utilities/configs/db_credentials.properties");
            credentials.getParentFile().mkdirs();
            if (!credentials.exists()) {
                credentials.createNewFile();
            }
            FileWriter writer = new FileWriter(credentials);
            dbCredentials.store(writer, "");
        }
        catch (IOException e) {
            logger.printBukkit("Failed to save credentials: " + e.getMessage(), true);
        }
        this.saveCredentials(this.username, this.password);
        this.dialect = dialect;
        this.showSql = showSql;
        this.formatSql = formatSql;
        this.ddlAuto = ddlAuto;
        this.mappingClasses = mappingClasses != null ? new ArrayList<>(mappingClasses) : new ArrayList<>();
    }

    public void configureSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            createDatabase();
            configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC"); // Adjust driver as needed
            configuration.setProperty("hibernate.connection.url", jdbcUrl);
            configuration.setProperty("hibernate.connection.username", username);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.dialect", dialect);
            configuration.setProperty("org.hibernate.SQL", "DEBUG");
            configuration.setProperty("hibernate.show_sql", Boolean.toString(showSql));
            configuration.setProperty("hibernate.format_sql", Boolean.toString(formatSql));
            configuration.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
            configuration.setProperty("org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");
            configuration.setProperty("hibernate.jdbc.fetch_size", Integer.toString(50));

            for (Class<?> clazz : mappingClasses) {
                configuration.addAnnotatedClass(clazz);
            }

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
           logger.printBukkit("Failed to configure Hibernate SessionFactory: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void createDatabase() throws Exception {
        File parentFolder = new File("./plugins/Vicky-s_Utilities/databases/");
        parentFolder.mkdirs();
        String sqlitePath = new File(parentFolder, "global.db").getAbsolutePath();
        database = new DatabaseCreator.DatabaseBuilder().name(sqlitePath).build();
        database.createDatabase();
        this.jdbcUrl = database.getJdbcUrl();
    }

    public void startDatabase() {
        try {
            HibernateUtil.initialise(this);
        }catch (Exception e) {
            logger.printBukkit(e.getMessage() + ": " + e.getCause(), true);
        }
    }

    public void saveCredentials(String userName, String password) {
        try (FileWriter writer = new FileWriter("./plugins/Vicky-s_Utilities/configs/db_credentials.properties")) {
            Properties properties = new Properties();
            properties.setProperty("userName", userName);
            properties.setProperty("password", password);
            properties.store(writer, "Database Credentials");
        } catch (IOException var8) {
            var8.printStackTrace();
        }
    }
}
