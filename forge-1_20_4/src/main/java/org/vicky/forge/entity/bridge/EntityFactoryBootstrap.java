/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.MobRegisteringClass;
import org.vicky.platform.entity.RegisterFactory;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

public final class EntityFactoryBootstrap {
	private static final Type REGISTER_FACTORY = Type.getType(RegisterFactory.class);
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void loadFactories(PlatformPlugin plugin) {
		LOGGER.info("Scanning Mob Factories...");
		ServiceLoader.load(MobRegisteringClass.class).forEach(provider -> {
			provider.register(plugin);
			LOGGER.info("Successfully loaded mob factory in service loader: {}", provider.getClass());
		});
		for (ModFileScanData scanData : ModList.get().getAllScanData()) {
			scanData.getAnnotations().forEach(annotation -> {

				if (!annotation.annotationType().equals(REGISTER_FACTORY)) {
					// LOGGER.info("Skipping non-type annotation: {}", annotation.annotationType());
					return;
				}

				String className = annotation.clazz().getClassName();

				try {
					Class<?> clazz = loadClassFromScan(scanData, className);

					if (!MobRegisteringClass.class.isAssignableFrom(clazz)) {
						throw new IllegalStateException(
								"@RegisterFactory used on non-MobRegisteringClass: " + className);
					}

					MobRegisteringClass factory = (MobRegisteringClass) clazz.getDeclaredConstructor().newInstance();
					LOGGER.info("Successfully loaded mob factory: {}", factory.getClass());
					factory.register(plugin);

				} catch (Throwable t) {
					throw new RuntimeException("Failed to load @RegisterFactory class: " + className, t);
				}
			});
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
