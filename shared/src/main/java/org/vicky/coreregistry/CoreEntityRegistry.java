/* Licensed under Apache-2.0 2025. */
package org.vicky.coreregistry;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.MobEntityDescriptor;
import org.vicky.platform.utils.ResourceLocation;

/**
 * Core-side registry for MobEntityDescriptors. Core modules can call
 * CoreDimensionRegistry.register(...) during static init. The platform should
 * call installInto(plugin) during startup to consume registrations.
 */
public class CoreEntityRegistry {
	private static final ConcurrentHashMap<ResourceLocation, MobEntityDescriptor> DESCRIPTORS = new ConcurrentHashMap<>();

	private CoreEntityRegistry() {
	}

	/**
	 * Called by core modules to register a dimension descriptor early (static init
	 * ok).
	 */
	public static void register(MobEntityDescriptor descriptor) {
		if (descriptor == null)
			throw new IllegalArgumentException("descriptor");
		ResourceLocation id = descriptor.getMobDetails().getMobKey();
		DESCRIPTORS.putIfAbsent(id, descriptor);
	}

	/**
	 * Convenience: register many
	 */
	public static void registerAll(Collection<MobEntityDescriptor> descriptors) {
		if (descriptors == null)
			return;
		final var descriptorsStatic = Collections.unmodifiableCollection(descriptors);
		for (MobEntityDescriptor d : descriptorsStatic)
			register(d);
	}

	/**
	 * Returns unmodifiable snapshot for discovery.
	 */
	public static Collection<MobEntityDescriptor> getRegisteredDescriptors() {
		return Collections.unmodifiableCollection(DESCRIPTORS.values());
	}

	/**
	 * Install all currently-registered descriptors into the provided platform. This
	 * is what the platform should call during startup (once).
	 */
	public static void installInto(PlatformPlugin plugin) {
		if (plugin == null)
			throw new IllegalArgumentException("plugin");
		for (MobEntityDescriptor d : getRegisteredDescriptors()) {
			try {
				plugin.registerMobEntityDescriptor(d); // platform must implement this
			} catch (Exception ex) {
				// platform is allowed to log/handle. swallow here to avoid breaking
				// installation loop.
				plugin.getPlatformLogger()
						.error("Failed to register core entity descriptor: " + d.getMobDetails().getMobKey(), ex);
			}
		}
	}

	/**
	 * If platform is already running, let core ask platform to create a dimension
	 * immediately.
	 */
	public static void registerAndCreateNow(MobEntityDescriptor descriptor, PlatformPlugin plugin) {
		register(descriptor);
		if (plugin != null) {
			try {
				plugin.registerMobEntityDescriptor(descriptor);
				plugin.processPendingEntities();
			} catch (Exception ex) {
				plugin.getPlatformLogger()
						.error("registerAndCreateNow failed for " + descriptor.getMobDetails().getMobKey(), ex);
			}
		}
	}
}
