package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.vicky.forge.entity.effects.ForgePlatformEffectBridge;
import org.vicky.platform.entity.RegisterEffect;
import org.vicky.platform.entity.EffectProvider;
import org.vicky.platform.entity.EffectDescriptor;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

public final class EffectBootstrap {
    private static final Type REGISTER_EFFECT_TYPE = Type.getType(RegisterEffect.class);
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void discoverAndRegisterAll() {
        LOGGER.info("Scanning Effects...");
        ServiceLoader.load(EffectProvider.class).forEach(provider -> {
            EffectDescriptor desc = provider.create();
            LOGGER.info("Successfully loaded effect descriptor from service loader: {}", desc.getKey());
            ForgePlatformEffectBridge.INSTANCE.registerEffect(desc);
        });
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData ann : data.getAnnotations()) {
                if (!ann.annotationType().equals(REGISTER_EFFECT_TYPE)) {
                    // LOGGER.info("Skipping non-type annotation: {}", ann.annotationType());
                    continue;
                }

                String providerClass = ann.clazz().getClassName();
                try {
                    // Use context classloader helper
                    Class<?> clazz = loadClassFromScan(data, providerClass);

                    if (!EffectProvider.class.isAssignableFrom(clazz)) {
                        throw new IllegalStateException(
                                "@RegisterEffect used on class that does not implement EffectProvider: " + providerClass
                        );
                    }

                    EffectProvider provider = (EffectProvider) clazz.getDeclaredConstructor().newInstance();
                    EffectDescriptor desc = provider.create();
                    LOGGER.info("Successfully loaded effect descriptor: {}", desc.getKey());

                    ForgePlatformEffectBridge.INSTANCE.registerEffect(desc);

                } catch (Throwable t) {
                    // fail fast — optionally log and continue for resilience
                    throw new RuntimeException("Failed to load effect provider: " + providerClass, t);
                }
            }
        }
    }

    /** Try to load the class using the mod's own classloader (if available), then context loader, then default. */
    private static Class<?> loadClassFromScan(ModFileScanData scanData, String className) throws ClassNotFoundException {
        // 1) Try to obtain a classloader from the scanData -> modFile -> secureJar -> classLoader (reflection, tolerant)
        try {
            // scanData.getModFile() -> net.minecraftforge.fml.loading.moddiscovery.ModFile (or similar)
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
            // reflection failed (method absent or signature changed) — continue to context loader fallback
        }

        // 2) Try thread context classloader (ModLauncher / FML commonly sets this)
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx != null) {
            try {
                return Class.forName(className, true, ctx);
            } catch (ClassNotFoundException ignored) { /* try next */ }
        }

        // 3) Fallback: default Class.forName (app/system loader)
        return Class.forName(className);
    }
}
