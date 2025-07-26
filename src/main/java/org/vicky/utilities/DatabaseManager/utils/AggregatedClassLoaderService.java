/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;

/**
 * A ClassLoaderService implementation that delegates class/resource loading to an aggregated class loader.
 */
public class AggregatedClassLoaderService implements ClassLoaderService {
  private final ClassLoader aggregatedClassLoader;

  /**
   * Constructs the service from a list of delegate class loaders.
   *
   * @param delegate The class loader to search.
   */
  public AggregatedClassLoaderService(ClassLoader delegate) {
    // Create an AggregatedClassLoader that delegates to the provided class loaders.
    this.aggregatedClassLoader = delegate;
  }

  /**
   * Loads the class with the given name by delegating to the aggregated class loader.
   */
  @Override
  public <T> Class<T> classForName(String name) {
    try {
      System.out.println(
          "loaders: " + ((AggregatedClassLoader) aggregatedClassLoader).getLoaders());
      return (Class<T>) aggregatedClassLoader.loadClass(name);
    } catch (ClassNotFoundException e) {
      throw new ClassLoadingException("Unable to load class: " + name, e);
    }
  }

  /**
   * Loads the class with the given type name by delegating to the aggregated class loader.
   */
  @Override
  public <T> Class<T> classForTypeName(String className) {
    return classForName(className);
  }

  /**
   * Locates a resource by name using the aggregated class loader.
   */
  @Override
  public URL locateResource(String name) {
    return aggregatedClassLoader.getResource(name);
  }

  /**
   * Locates a resource by name and returns its input stream.
   */
  @Override
  public InputStream locateResourceStream(String name) {
    URL resource = locateResource(name);
    try {
      return resource != null ? resource.openStream() : null;
    } catch (IOException e) {
      throw new ClassLoadingException("Unable to open resource stream for: " + name, e);
    }
  }

  /**
   * Locates all resources with the given name.
   */
  @Override
  public List<URL> locateResources(String name) {
    try {
      Enumeration<URL> resources = aggregatedClassLoader.getResources(name);
      return Collections.list(resources);
    } catch (IOException e) {
      throw new ClassLoadingException("Unable to locate resources: " + name, e);
    }
  }

  /**
   * Discovers and instantiates implementations of the named service contract.
   */
  @Override
  public <S> Collection<S> loadJavaServices(Class<S> serviceContract) {
    ServiceLoader<S> loader = ServiceLoader.load(serviceContract, aggregatedClassLoader);
    List<S> services = new ArrayList<>();
    for (S service : loader) {
      services.add(service);
    }
    return services;
  }

  /**
   * Generates a proxy instance for the specified interfaces.
   */
  @Override
  public <T> T generateProxy(InvocationHandler handler, Class... interfaces) {
    return (T) Proxy.newProxyInstance(aggregatedClassLoader, interfaces, handler);
  }

  /**
   * Loads a Package from the classloader.
   */
  @Override
  public Package packageForNameOrNull(String packageName) {
    return Package.getPackage(packageName);
  }

  /**
   * Performs work with the class loader.
   */
  @Override
  public <T> T workWithClassLoader(Work<T> work) {
    return work.doWork(aggregatedClassLoader);
  }

  /**
   * Stops the service.
   */
  @Override
  public void stop() {
    // Implement any cleanup logic if necessary.
  }
}
