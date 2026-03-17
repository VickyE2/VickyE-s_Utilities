// MemoryJarURL.java
package org.vicky.forge.entity.bridge;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory URL provider for nested jars.
 *
 * Usage:
 *   MemoryJarURL.register("modid:nested-1.jar", bytes);
 *   URL url = MemoryJarURL.urlFor("modid:nested-1.jar");
 *
 * The returned URL can be passed to ClassGraph.overrideClasspath(...) (as url.toString())
 * or used to create a URLClassLoader.
 */
public final class MemoryJarURL {
    // thread-safe map of id -> bytes
    private static final Map<String, byte[]> REGISTRY = new ConcurrentHashMap<>();

    // small custom protocol name; doesn't collide with file/http
    public static final String PROTOCOL = "memjar";

    private MemoryJarURL() {}

    /** Register bytes under a stable id. */
    public static void register(String id, byte[] bytes) {
        REGISTRY.put(id, bytes);
    }

    /** Unregister when you want to free memory (optional). */
    public static void unregister(String id) {
        REGISTRY.remove(id);
    }

    /** Get a URL that uses the custom handler to serve the bytes. */
    public static URL urlFor(String id) {
        try {
            // each URL must use the same handler instance type, but we pass the id in the path
            return new URL(null, PROTOCOL + ":" + id, new MemoryJarURLStreamHandler());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Internal URLStreamHandler that looks up the id and returns a URLConnection */
    static class MemoryJarURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) {
            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    // nothing to do for connection
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    // URL is of form memjar:id or memjar:id?whatever
                    String raw = null;
                    try {
                        raw = url.toURI().getSchemeSpecificPart();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    String id = raw;
                    int q = raw.indexOf('?');
                    if (q >= 0) id = raw.substring(0, q);
                    byte[] bytes = REGISTRY.get(id);
                    if (bytes == null) throw new IOException("No REGISTRY entry for " + id);
                    return new ByteArrayInputStream(bytes);
                }

                @Override
                public int getContentLength() {
                    String raw = null;
                    try {
                        raw = url.toURI().getSchemeSpecificPart();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    String id = raw;
                    int q = raw.indexOf('?');
                    if (q >= 0) id = raw.substring(0, q);
                    byte[] b = REGISTRY.get(id);
                    return b == null ? -1 : b.length;
                }
            };
        }
    }
}