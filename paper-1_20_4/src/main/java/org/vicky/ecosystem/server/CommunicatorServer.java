/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.ecosystem.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.vicky.ecosystem.exceptions.NoCommunicationServerInstanceException;
import org.vicky.ecosystem.exceptions.TargetPluginNotFoundException;
import org.vicky.ecosystem.plugin.Communicateable;
import org.vicky.ecosystem.plugin.PluginCommunicator;
import org.vicky.global.Global;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.utils.AggregatedClassLoader;

import com.google.gson.JsonObject;

public class CommunicatorServer {
	private static final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "COMMUNICATOR");
	private static boolean hasStarted = false;
	private static CommunicatorServer instance;
	public AggregatedClassLoader classLoader;
	private final Map<String, PluginCommunicator> plugins;

	private CommunicatorServer() {
		this.plugins = new ConcurrentHashMap<>();
		this.classLoader = Global.classLoader;
	}

	public static CommunicatorServer getInstance() {
		if (instance == null || !hasStarted) {
			throw new NoCommunicationServerInstanceException(
					"CommunicatorServer not started. Are you calling before startup?");
		}
		return instance;
	}

	public static void startupCommunicationServer() {
		instance = new CommunicatorServer();
		hasStarted = true;
		logger.print(
				ANSIColor.colorize(
						"green_bold[✓] Communication Server is now online! Listening for plugin" + " registrations..."),
				ContextLogger.LogType.AMBIENCE);
	}

	/**
	 * Registers a plugin to the communication server.
	 *
	 * @param name
	 *            Plugin name (unique)
	 * @param loader
	 *            Plugin classloader
	 * @param impl
	 *            Plugin implementation (extends Communicateable)
	 * @return PluginCommunicator for the registered plugin
	 */
	public synchronized PluginCommunicator register(String name, ClassLoader loader, Communicateable impl) {

		classLoader.getLoaders().add(loader);
		PluginCommunicator communicator = new PluginCommunicator(name, loader, impl);
		PluginCommunicator previous = plugins.put(name, communicator);

		if (previous != null) {
			logger.print(ANSIColor.colorize("yellow[Plugin already registered: " + name + "]. Overwriting..."),
					ContextLogger.LogType.WARNING);
			try {
				previous.getImpl().onShutdown();
			} catch (Exception e) {
				logger.print(
						ANSIColor
								.colorize("red[Error in onShutdown for previous " + name + ": " + e.getMessage() + "]"),
						ContextLogger.LogType.ERROR);
			}
		}

		logger.print(ANSIColor.colorize("green[✓] Plugin hooked: " + name), ContextLogger.LogType.AMBIENCE);

		try {
			impl.register(communicator);
		} catch (Exception e) {
			logger.print(ANSIColor.colorize("red[Error calling onRegister for " + name + ": " + e.getMessage() + "]"),
					ContextLogger.LogType.ERROR);
		}
		return communicator;
	}

	public CompletableFuture<Object> sendAsync(String targetPlugin, String key, JsonObject message) {
		PluginCommunicator communicator = plugins.get(targetPlugin);
		if (communicator == null) {
			logger.print(ANSIColor.colorize("red[Target plugin not found: " + targetPlugin + "]"),
					ContextLogger.LogType.ERROR);
			return CompletableFuture.failedFuture(new TargetPluginNotFoundException(targetPlugin, false));
		}
		return communicator.receiveAsync(key, message);
	}

	/**
	 * Unregisters a plugin by name.
	 *
	 * @param name
	 *            Plugin name
	 */
	public synchronized void unregister(String name) {
		PluginCommunicator communicator = plugins.remove(name);
		if (communicator != null) {
			try {
				communicator.getImpl().onShutdown(); // Lifecycle hook
			} catch (Exception e) {
				logger.print(ANSIColor.colorize("red[Error in onShutdown for " + name + ": " + e.getMessage() + "]"),
						ContextLogger.LogType.ERROR);
			}
			logger.print(ANSIColor.colorize("blue[-] Plugin unhooked: " + name), ContextLogger.LogType.BASIC);
		} else {
			logger.print(ANSIColor.colorize("yellow[Plugin not found: " + name + "]"), ContextLogger.LogType.WARNING);
		}
	}

	/**
	 * Gets a communicator by plugin name.
	 *
	 * @param name
	 *            Plugin name
	 * @return The communicator or null
	 */
	public PluginCommunicator get(String name) {
		return plugins.get(name);
	}

	/**
	 * Lists all registered plugin names.
	 *
	 * @return Map of plugin names to their communicators
	 */
	public Map<String, PluginCommunicator> getAll() {
		return Map.copyOf(plugins);
	}

	/**
	 * Broadcasts a shutdown signal to all plugins.
	 */
	public synchronized void shutdownAll() {
		for (String name : List.copyOf(plugins.keySet())) {
			unregister(name);
		}
		plugins.clear();
		logger.print("cyan[All plugins unregistered]", ContextLogger.LogType.BASIC);
	}

	/**
	 * Returns a rich ANSI-colored list of all hooked plugins.
	 *
	 * @return ANSI-formatted string of registered plugins
	 */
	public String getAllRegistered() {
		StringBuilder sb = new StringBuilder();

		sb.append(ANSIColor.colorize("cyan_bold[=== Hooked Plugins List ===]\n"));

		if (plugins.isEmpty()) {
			sb.append(ANSIColor.colorize("red_bold[No plugins are currently registered.]\n"));
		} else {
			int index = 1;
			for (Map.Entry<String, PluginCommunicator> entry : plugins.entrySet()) {
				String name = entry.getKey();
				ClassLoader loader = entry.getValue().getLoader();
				String loaderName = loader.getClass().getSimpleName();

				sb.append(ANSIColor.colorize("yellow_bold[") + index++ + ".] ")
						.append(ANSIColor.colorize("green[Plugin: ]"))
						.append(ANSIColor.colorize("blue_bold[" + name + "]"))
						.append(ANSIColor.colorize(" white[ | ClassLoader: ]"))
						.append(ANSIColor.colorize("magenta[" + loaderName + "]")).append("\n");
			}

			sb.append(ANSIColor.colorize("cyan_bold[===========================]\n"));
			sb.append(ANSIColor.colorize("green_bold[✓ Total Hooked Plugins: " + plugins.size() + "]\n"));
		}

		return sb.toString();
	}
}
