/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.RegisterMob;
import org.vicky.platform.entity.MobEntityDescriptor;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

public final class EntityFactoryBootstrap {
	private static final Type REGISTER_MOB_TYPE = Type.getType(RegisterMob.class);
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void loadFactories(PlatformPlugin plugin) {
		LOGGER.info("Scanning Mob Factories...");

		for (ModFileScanData scanData : ModList.get().getAllScanData()) {

			String modId = scanData.getIModInfoData()
					.stream()
					.flatMap(file -> file.getMods().stream())
					.map(IModInfo::getModId)
					.findFirst()
					.orElse("unknown");

			LOGGER.info("Scanning mod: {}", modId);

			for (ModFileScanData.AnnotationData ann : scanData.getAnnotations()) {

				if (!ann.annotationType().equals(REGISTER_MOB_TYPE)) {
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
						if (value instanceof MobEntityDescriptor desc) {
							LOGGER.info("Successfully loaded mob descriptor: {}", desc.getMobDetails().getMobKey());
							plugin.registerMobEntityDescriptor(desc);
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
