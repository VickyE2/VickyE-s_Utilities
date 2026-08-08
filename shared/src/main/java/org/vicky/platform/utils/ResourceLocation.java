/* Licensed under Apache-2.0 2025-2026. */
package org.vicky.platform.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.Locale;

public class ResourceLocation {
	private static final LoadingCache<String, ResourceLocation> CACHE = Caffeine.newBuilder().maximumSize(10_000)
			.softValues().build(ResourceLocation::new);

	private String namespace = "minecraft";
	private final String path;

	private ResourceLocation(String parseable) {
		parseable = parseable.trim().replace("\\", "/");

		if (parseable.isEmpty())
			throw new IllegalArgumentException("The parseable of a resource locator cannot be null.");
		if (parseable.contains(":")) {
			String[] parts = parseable.split(":", 2);
			if (!parts[0].matches("[a-z0-9_./]+"))
				throw new IllegalArgumentException("Invalid namespace " + namespace);
			if (!parts[1].matches("[a-z0-9_./]+"))
				throw new IllegalArgumentException("Invalid path " + parseable);
			this.namespace = parts[0];
			this.path = parts[1];
		} else {
			if (!parseable.matches("[a-z0-9_.-/]+"))
				throw new IllegalArgumentException("Invalid path " + parseable);
			this.path = parseable;
		}
	}
	private ResourceLocation(String namespace, String path) {
		namespace = namespace.trim();
		path = path.trim().replace("\\", "/");

		if (path.isEmpty())
			throw new IllegalArgumentException("The path of a resource locator cannot be null.");
		if (namespace.isEmpty())
			throw new IllegalArgumentException("The namespace of a resource locator cannot be null.");
		if (!namespace.matches("[a-z0-9_./]+"))
			throw new IllegalArgumentException("Invalid namespace " + namespace);
		if (!path.matches("[a-z0-9_./]+"))
			throw new IllegalArgumentException("Invalid path " + path);

		this.path = path;
		this.namespace = namespace;
	}

	public static ResourceLocation getEMPTY() {
		return new ResourceLocation("empty", "empty");
	}

	private static String normalize(String input) {
		return input.toLowerCase(Locale.ROOT);
	}

	/**
	 * This has minecraft as the default namespace
	 *
	 * @param path
	 *            the context resource path
	 * @return A ResourceLocator with minecraf as the namespace
	 */
	public static ResourceLocation from(String path) {
		return CACHE.get(normalize(path));
	}

	public static ResourceLocation from(String namespace, String path) {
		return from(namespace + ":" + path);
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
		if (this == obj)
			return true;
		if (!(obj instanceof ResourceLocation other))
			return false;

		if (!this.namespace.equals(other.namespace))
			return false;
		return this.path.equals(other.path);
	}

	@Override
	public int hashCode() {
		int result = namespace.hashCode();
		result = 31 * result + path.hashCode();
		return result;
	}
}