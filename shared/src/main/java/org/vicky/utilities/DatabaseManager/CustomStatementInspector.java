/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.vicky.platform.PlatformPlugin;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class CustomStatementInspector implements StatementInspector {
	private final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.HIBERNATE, "LOG");

	@Override
	public String inspect(String sql) {
		if (sql.trim().toLowerCase().startsWith("select")) {
			logSelectQuery(sql);
		}
		return sql; // Return the query unmodified
	}

	private void logSelectQuery(String sql) {
		// Extract entity name and ID from the SQL query (simple example)
		String entityName = "Unknown Entity";
		String entityId = "Unknown ID";

		if (sql.contains("from")) {
			String[] parts = sql.split("from");
			String tableName = parts[1].split(" ")[1].trim();
			entityName = tableName.substring(tableName.indexOf('_') + 1); // Adjust based on your table naming
																			// convention
		}

		if (sql.contains("where")) {
			String[] conditions = sql.split("where")[1].split("=");
			entityId = conditions[1].trim();
		}

		if (PlatformPlugin.config().getBooleanValue("Debug")) {
			logger.print(String.format("Selected ~ %s having Id -> %s)", entityName, entityId));
		}
	}
}
