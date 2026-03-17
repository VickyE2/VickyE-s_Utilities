// NestedJarClasspathBuilder.java
package org.vicky.forge.entity.bridge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class NestedJarClasspathBuilder {
    private NestedJarClasspathBuilder() {}

    /**
     * Builds a list of classpath entries for scanning:
     *   - the outer jar path (jar:file:/... -> will be scanned as normal)
     *   - plus memjar: ids (registered in MemoryJarURL) for nested jar entries
     * <p>
     * The returned list contains URL strings suitable for ClassGraph.overrideClasspath(...)
     * (i.e. url.toString()).
     */
    public static List<String> classpathEntriesForOuterJar(Path outerJarPath) {
        List<String> entries = new ArrayList<>();
        if (outerJarPath != null) {
            // outer jar as a normal file URL
            entries.add(outerJarPath.toUri().toString());

            try (JarFile jf = new JarFile(outerJarPath.toFile())) {
                Enumeration<JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    String name = je.getName();
                    if (!name.endsWith(".jar")) continue;

                    // Only process known container locations (same logic as your previous code)
                    if (!(name.startsWith("META-INF/jarjar/") || name.startsWith("BOOT-INF/lib/") || name.startsWith("lib/"))) {
                        continue;
                    }

                    try (InputStream in = jf.getInputStream(je)) {
                        byte[] bytes = readAllBytes(in);
                        // create a stable id — use outer jar name + nested entry path to avoid collisions
                        String id = outerJarPath.getFileName().toString() + "!" + name;
                        MemoryJarURL.register(id, bytes);

                        // Create URL and add to classpath list as string
                        URL u = MemoryJarURL.urlFor(id);
                        entries.add(u.toString());
                    } catch (IOException e) {
                        // log and continue
                        // use your logger instead of System.err in real code
                        System.err.println("Failed to read nested jar " + name + " in " + outerJarPath + ": " + e);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to open outer jar " + outerJarPath + ": " + e);
            }
        }
        return entries;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toByteArray();
    }
}
