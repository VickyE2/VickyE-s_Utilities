package org.vicky.platform.utils;

public class ResourceLocation {
    private String namespace = "minecraft";
    private String path = "";

    private ResourceLocation(String path) {
        if (path.isEmpty()) throw new IllegalArgumentException("The path of a resource locator cannot be null.");
        if (path.contains(":")) {
            this.namespace = path.split(":")[0];
            this.path = path.split(":")[1];
        } else {
            this.path = path;
        }
    }

    private ResourceLocation(String namespace, String path) {
        if (path.isEmpty()) throw new IllegalArgumentException("The path of a resource locator cannot be null.");
        if (namespace.isEmpty())
            throw new IllegalArgumentException("The namespace of a resource locator cannot be null.");
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
}