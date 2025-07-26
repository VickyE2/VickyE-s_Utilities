/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.io.File;
import java.net.URL;
import java.util.*;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.utils.ChildFirstURLClassLoader;
import org.vicky.vicky_utils;

public class JarClassScanner {
  private static final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.SYSTEM, "REFLECTION");

  // Map to track existing ChildFirstURLClassLoaders by jar absolute path.
  private static final Map<String, ChildFirstURLClassLoader> jarLoaderMap = new HashMap<>();

  // Also, track all class loaders used (for aggregation later)
  private final List<ClassLoader> classLoaders = new ArrayList<>();

  public JarClassScanner() {}

  public List<ClassLoader> getClassLoaders() {
    return classLoaders;
  }

  /**
   * Returns classes from a given JAR, scanning the specified package.
   * If the JAR was already scanned, it reuses the existing ChildFirstURLClassLoader.
   *
   * @param jarFileName The {@link java.util.jar.JarFile contextJar} to scan
   * @param packageName The {@link String packageName} to scan for classes in the {@link java.util.jar.JarFile contextJar}
   * @param parentClass The {@link Class class} required to extend or implement to be reflected
   * @return A {@link List} of Classes that extend or implement the {@link Class ParentClass}
   */
  public List<Class<?>> getClassesFromJar(
      String jarFileName, String packageName, Class<?> parentClass) {
    List<Class<?>> matchingClasses = new ArrayList<>();
    try {
      // Construct the File object for the dependent JAR
      File jarFile =
          new File(vicky_utils.getPlugin().getDataFolder().getParent(), jarFileName + ".jar");
      if (!jarFile.exists()) {
        logger.printBukkit("JAR file not found: " + jarFile.getAbsolutePath(), true);
        throw new RuntimeException("JAR not found");
      }

      // Use the jar file's absolute path as a key.
      String jarKey = jarFile.getAbsolutePath();

      // Create the JAR URL in the proper format.
      URL jarUrl = new URL("jar:file:" + jarKey + "!/");
      logger.printBukkit("JAR URL: " + jarUrl, false);

      // Reuse existing loader if available; otherwise create a new ChildFirstURLClassLoader.
      ChildFirstURLClassLoader classLoader = jarLoaderMap.get(jarKey);
      if (classLoader == null) {
        classLoader =
            new ChildFirstURLClassLoader(
                new URL[] {jarUrl}, JarClassScanner.class.getClassLoader());
        jarLoaderMap.put(jarKey, classLoader);
        logger.printBukkit("Created new Child-first ClassLoader for: " + jarFileName, false);
      } else {
        logger.printBukkit("Reusing existing Child-first ClassLoader for: " + jarFileName, false);
      }

      // Add to our list for later aggregation if not already added.
      if (!classLoaders.contains(classLoader)) {
        classLoaders.add(classLoader);
      }

      // Log the URLs being scanned (from the given package in this class loader)
      for (URL url : org.reflections.util.ClasspathHelper.forPackage(packageName, classLoader)) {
        logger.printBukkit("Scanning URL: " + url.toString(), false);
      }

      // Configure Reflections to scan the specified package using the child-first loader.
      Reflections reflections =
          new Reflections(
              new ConfigurationBuilder()
                  .setUrls(jarUrl) // Use the JAR's URL directly
                  .addClassLoaders(classLoader) // Ensure we're scanning with our custom loader
                  .filterInputsBy(new FilterBuilder().includePackage(packageName))
                  .setScanners(Scanners.SubTypes));

      logger.printBukkit("Scanning package: " + packageName, false);
      // Load the parentClass using our class loader so that we compare using the same loader.
      Class<?> parentClassFromChild = classLoader.loadClass(parentClass.getName());
      Set<? extends Class<?>> allClasses =
          reflections.getSubTypesOf((Class<?>) parentClassFromChild);
      if (allClasses.isEmpty()) {
        logger.printBukkit("No classes found in package: " + packageName, true);
      }
      for (Class<?> clazz : allClasses) {
        if (clazz != null) {
          String className = clazz.getName();
          logger.printBukkit("Found Class that matches criteria: " + className, false);
          matchingClasses.add(clazz);
        }
      }
    } catch (Exception e) {
      logger.printBukkit("Error occurred while reflecting: ", true);
      e.printStackTrace();
    }
    return matchingClasses;
  }

  /**
   * Overloaded version if you want to scan based solely on the parent class.
   *
   * @param jarFileName The {@link java.util.jar.JarFile contextJar} to scan
   * @param parentClass The {@link Class class} required to extend or implement to be reflected
   * @return A {@link List} of Classes that extend or implement the {@link Class ParentClass}
   */
  public List<Class<?>> getClassesFromJar(String jarFileName, Class<?> parentClass) {
    return getClassesFromJar(jarFileName, "", parentClass);
  }
}
