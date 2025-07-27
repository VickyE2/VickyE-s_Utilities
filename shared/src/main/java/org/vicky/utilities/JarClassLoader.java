/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.net.URL;
import java.net.URLClassLoader;

public class JarClassLoader extends URLClassLoader {
  public JarClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }
}
