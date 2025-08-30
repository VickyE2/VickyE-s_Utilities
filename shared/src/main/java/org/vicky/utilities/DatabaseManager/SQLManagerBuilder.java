/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager;

import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;

import java.util.ArrayList;
import java.util.List;

public class SQLManagerBuilder {
  private final List<Class<?>> mappingClasses = new ArrayList<>();
  private final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.HIBERNATE, "MANAGER");
  private String username;
  private String password;
  private String dialect;
  private boolean showSql;
  private boolean formatSql;
  private String databaseFolder = "defaulted";
  private Hbm2DdlAutoType ddlAuto;

  public SQLManagerBuilder setUsername(String username) {
    this.username = username;
    return this;
  }

  public SQLManagerBuilder setPassword(String password) {
    this.password = password;
    return this;
  }

  public SQLManagerBuilder setDatabaseFolder(String databaseFolder) {
    this.databaseFolder = databaseFolder;
    return this;
  }

  public SQLManagerBuilder setDialect(String dialect) {
    this.dialect = dialect;
    return this;
  }

  public SQLManagerBuilder addMappingClass(Class<?> clazz) {
    this.mappingClasses.add(clazz);
    return this;
  }

  public SQLManagerBuilder addMappingClasses(List<Class<?>> clazz) {
    this.mappingClasses.addAll(clazz);
    return this;
  }

  public SQLManagerBuilder setDdlAuto(Hbm2DdlAutoType ddlAuto) {
    this.ddlAuto = ddlAuto;
    return this;
  }

  public SQLManagerBuilder setFormatSql(boolean formatSql) {
    this.formatSql = formatSql;
    return this;
  }

  public SQLManagerBuilder setShowSql(boolean showSql) {
    this.showSql = showSql;
    return this;
  }

  // Build method to create an SQLManager instance
  public SQLManager build() {
    if (username == null || password == null || dialect == null) {
      throw new IllegalStateException("Missing required fields to create SQLManager");
    }

    // Return the instance of SQLManager
    return new SQLManager(
            username, password, dialect, showSql, formatSql, ddlAuto.toString(), mappingClasses, databaseFolder);
  }
}
