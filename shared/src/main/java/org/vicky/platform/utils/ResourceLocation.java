package org.vicky.platform.utils;

import java.util.Objects;

public class ResourceLocation {
    private String namespace = "minecraft";
    private final String path;

    private ResourceLocation(String path) {
        if (path.isEmpty()) throw new IllegalArgumentException("The path of a resource locator cannot be null.");
        if (path.contains(":")) {
            String[] parts = path.split(":", 2);
            if (!parts[0].matches("[a-z0-9_.-/]+")) throw new IllegalArgumentException("Invalid namespace " + namespace);
            if (!parts[1].matches("[a-z0-9_.-/]+")) throw new IllegalArgumentException("Invalid path " + path);
            this.namespace = parts[0];
            this.path = parts[1];
        } else {
            if (!path.matches("[a-z0-9_.-/]+")) throw new IllegalArgumentException("Invalid path " + path);
            this.path = path;
        }
    }

    private ResourceLocation(String namespace, String path) {
        if (path.isEmpty()) throw new IllegalArgumentException("The path of a resource locator cannot be null.");
        if (namespace.isEmpty())
            throw new IllegalArgumentException("The namespace of a resource locator cannot be null.");
        if (!namespace.matches("[a-z0-9_.-/]+")) throw new IllegalArgumentException("Invalid namespace " + namespace);
        if (!path.matches("[a-z0-9_.-/]+")) throw new IllegalArgumentException("Invalid path " + path);
        this.path = path;
        this.namespace = namespace;
    }

    public static ResourceLocation getEMPTY() {
        return new ResourceLocation("empty", "empty");
    }

    /**
     * This has minecraft as the default namespace
     *
     * @param path the context resource path
     * @return A ResourceLocator with minecraf as the namespace
     */
    public static ResourceLocation from(String path) {
        return new ResourceLocation(path);
    }

    public static ResourceLocation from(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public String getPath() {
        return path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String asString() {
        return namespace + ":" + path;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourceLocation)) {
            return false;
        }
        if (!Objects.equals(this.namespace, ((ResourceLocation) obj).namespace)) {
            return false;
        }
        return Objects.equals(this.path, ((ResourceLocation) obj).path);
    }
}