/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class AggregatedClassLoader extends ClassLoader {
  private final List<ClassLoader> delegates;

  /**
   * Construct an AggregatedClassLoader using the given delegate class loaders.
   *
   * @param delegates A list of class loaders to aggregate.
   */
  public AggregatedClassLoader(List<ClassLoader> delegates) {
    // Optionally, you can choose a parent class loader here. For example, the system class loader.
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
    Vector<URL> resources = new Vector<>();
    for (ClassLoader delegate : delegates) {
      Enumeration<URL> urls = delegate.getResources(name);
      while (urls.hasMoreElements()) {
        resources.add(urls.nextElement());
      }
    }
    return resources.elements();
  }
}
