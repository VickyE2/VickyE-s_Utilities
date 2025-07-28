/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import java.util.*;

import org.vicky.utilities.ContextLogger.ContextLogger;

/**
 * A generic registry class for managing and accessing registered entities.
 *
 * @param <T>
 *            the type of object being registered
 * @param <R>
 *            the type of the registry subclass
 */
public abstract class Registry<T, R extends Registry<T, R>> {

	private static final Map<Class<?>, Registry<?, ?>> instances = new HashMap<>();
	protected final ContextLogger logger;

	protected Registry(String registryName) {
		this.logger = new ContextLogger(ContextLogger.ContextType.REGISTRY, registryName);
		registerInstance();
	}

	/** Register this instance for singleton-like access. */
	@SuppressWarnings("unchecked")
	private void registerInstance() {
		Class<?> clazz = this.getClass();
		if (instances.containsKey(clazz)) {
			logger.print("Attempt to register multiple instances of " + clazz.getSimpleName(),
					ContextLogger.LogType.WARNING, false);
		}
		instances.put(clazz, this);
	}

	/**
	 * Gets the singleton instance for this registry subclass.
	 *
	 * @param clazz
	 *            The class of the registry
	 * @param <T>
	 *            The type of the items in the registry
	 * @param <R>
	 *            The registry class itself
	 * @return The singleton instance
	 */
	@SuppressWarnings("unchecked")
	public static <T, R extends Registry<T, R>> R getInstance(Class<R> clazz) {
		Registry<?, ?> instance = instances.get(clazz);
		if (instance == null) {
			throw new IllegalStateException("Registry " + clazz.getSimpleName() + " accessed before initialization!");
		}
		return (R) instance;
	}

	public abstract Collection<T> getRegisteredEntities();

	public abstract void register(T item);

	public ContextLogger getLogger() {
		return logger;
	}
}
