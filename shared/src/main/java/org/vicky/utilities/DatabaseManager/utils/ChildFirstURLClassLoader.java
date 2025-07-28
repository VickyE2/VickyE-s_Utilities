/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.utils;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A custom URLClassLoader that uses a child-first (reverse delegation)
 * mechanism.
 *
 * <p>
 * This class loader attempts to load classes from its own URLs before
 * delegating to its parent class loader. This is useful when you want to
 * isolate classes from a specific JAR, overriding classes already loaded by the
 * parent.
 */
public class ChildFirstURLClassLoader extends URLClassLoader {

	/**
	 * Constructs a new ChildFirstURLClassLoader for the given URLs and parent.
	 *
	 * @param urls
	 *            the URLs from which to load classes and resources
	 * @param parent
	 *            the parent class loader for delegation
	 */
	public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * Loads the class with the specified binary name.
	 *
	 * <p>
	 * This method first checks if the class has already been loaded. If not, it
	 * attempts to load the class using this class loader (child-first). If that
	 * fails, it delegates to the parent class loader.
	 *
	 * @param name
	 *            The binary name of the class
	 * @param resolve
	 *            If true, then resolve the class
	 * @return The resulting {@code Class} object
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 */
	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// Check if the class has already been loaded
		Class<?> clazz = findLoadedClass(name);
		if (clazz == null) {
			try {
				// Try to load the class from this class loader first
				clazz = findClass(name);
			} catch (ClassNotFoundException e) {
				// If not found, delegate to the parent
				clazz = super.loadClass(name, resolve);
			}
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}
}
