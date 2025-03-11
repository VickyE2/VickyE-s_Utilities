/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.utils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class AggregatedClassLoader extends ClassLoader {
  private final List<ClassLoader> delegates;

  /**
   * Construct an AggregatedClassLoader using the given delegate class loaders.
   *
   * @param delegates A list of class loaders to aggregate.
   */
  public AggregatedClassLoader(List<ClassLoader> delegates) {
    super(ClassLoader.getSystemClassLoader());
    this.delegates = delegates;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    // Try each delegate in turn.
    for (ClassLoader delegate : delegates) {
      try {
        return delegate.loadClass(name);
      } catch (ClassNotFoundException e) {

      }
    }

    throw new ClassNotAvailableException("Could not load class: " + name);
  }

  public List<ClassLoader> getLoaders() {
    return delegates;
  }

  @Override
  public URL getResource(String name) {
    for (ClassLoader delegate : delegates) {
      URL url = delegate.getResource(name);
      if (url != null) {
        return url;
      }
    }
    // Fallback to the parent class loader.
    return super.getResource(name);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    Set<URL> urls = new LinkedHashSet<>(); // Avoid duplicates

    // Loop through the registered class loaders and get resources
    for (ClassLoader loader : delegates) {
      Enumeration<URL> resources = loader.getResources(name);
      while (resources.hasMoreElements()) {
        urls.add(resources.nextElement());
      }
    }

    return Collections.enumeration(urls);
  }
}
