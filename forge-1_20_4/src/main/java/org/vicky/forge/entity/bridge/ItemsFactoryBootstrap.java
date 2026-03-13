/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.MobEntityDescriptor;
import org.vicky.platform.items.ItemDescriptor;
import org.vicky.platform.items.RegisterItem;
import org.vicky.platform.utils.ResourceLocation;

import java.lang.reflect.Method;

public final class ItemsFactoryBootstrap {
	private static final Type REGISTER_ITEM = Type.getType(RegisterItem.class);
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void loadFactories(PlatformPlugin plugin) {
		LOGGER.info("Scanning Item Descriptors...");

		for (ModFileScanData scanData : ModList.get().getAllScanData()) {

			String modId = scanData.getIModInfoData()
					.stream()
					.flatMap(file -> file.getMods().stream())
					.map(IModInfo::getModId)
					.findFirst()
					.orElse("unknown");

			LOGGER.info("Scanning mod: {}", modId);

			for (ModFileScanData.AnnotationData ann : scanData.getAnnotations()) {
				if (!ann.annotationType().equals(REGISTER_ITEM)) {
					// LOGGER.info("Skipping non-type annotation: {}", annotation.annotationType());
					continue;
				}

				String className = ann.clazz().getClassName();

				try {
					Class<?> clazz = loadClassFromScan(scanData, className);
					String memberName = ann.memberName();

					if (memberName != null && !memberName.isEmpty()) {
						java.lang.reflect.Field field = clazz.getDeclaredField(memberName);
						field.setAccessible(true);
						Object instance = null;

						if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
							try {
								java.lang.reflect.Field instanceField = clazz.getDeclaredField("INSTANCE");
								instanceField.setAccessible(true);
								instance = instanceField.get(null);
							} catch (NoSuchFieldException e) {
								// Try standard instantiation if not singleton
								try {
									instance = clazz.getDeclaredConstructor().newInstance();
								} catch (Exception ex) {
									LOGGER.error("Could not instantiate class {} to access field {}", className, memberName);
									continue;
								}
							}
						}

						Object value = field.get(instance);
						if (value instanceof ItemDescriptor desc) {
							var data = ann.annotationData();
							Object raw = data.get("fieldName");
							Object rawNamespace = data.get("fieldName");

							if (rawNamespace instanceof String namespace && raw instanceof String path) {
								LOGGER.info("Successfully loaded mob descriptor: {}", desc.getDisplayName());
								plugin.getPlatformItemFactory().registerItem(ResourceLocation.from(namespace, path), desc);
							}
						} else {
							LOGGER.warn("Field {} annotated with @RegisterMob is not a MobEntityDescriptor", memberName);
						}
					}

				} catch (Throwable t) {
					throw new RuntimeException("Failed to load @RegisterMob field: " + className, t);
				}
			}
		}
	}

	/**
	 * Try to load the class using the mod's own classloader (if available), then
	 * context loader, then default.
	 */
	private static Class<?> loadClassFromScan(ModFileScanData scanData, String className)
			throws ClassNotFoundException {
		// 1) Try to obtain a classloader from the scanData -> modFile -> secureJar ->
		// classLoader (reflection, tolerant)
		try {
			// scanData.getModFile() -> net.minecraftforge.fml.loading.moddiscovery.ModFile
			// (or similar)
			Method getModFile = scanData.getClass().getMethod("getModFile");
			Object modFile = getModFile.invoke(scanData);
			if (modFile != null) {
				// ModFile.getSecureJar()
				Method getSecureJar = modFile.getClass().getMethod("getSecureJar");
				Object secureJar = getSecureJar.invoke(modFile);
				if (secureJar != null) {
					// SecureJar (cpw.mods.jarhandling.SecureJar) may provide a class loader
					// try SecureJar.getClassLoader() (reflection)
					Method getClassLoader = secureJar.getClass().getMethod("getClassLoader");
					Object loaderObj = getClassLoader.invoke(secureJar);
					if (loaderObj instanceof ClassLoader) {
						ClassLoader modClassLoader = (ClassLoader) loaderObj;
						try {
							return Class.forName(className, true, modClassLoader);
						} catch (ClassNotFoundException ignored) {
							// fall through to other attempts
						}
					}
				}
			}
		} catch (ReflectiveOperationException ignored) {
			// reflection failed (method absent or signature changed) — continue to context
			// loader fallback
		}

		// 2) Try thread context classloader (ModLauncher / FML commonly sets this)
		ClassLoader ctx = Thread.currentThread().getContextClassLoader();
		if (ctx != null) {
			try {
				return Class.forName(className, true, ctx);
			} catch (ClassNotFoundException ignored) {
				/* try next */ }
		}

		// 3) Fallback: default Class.forName (app/system loader)
		return Class.forName(className);
	}

}
