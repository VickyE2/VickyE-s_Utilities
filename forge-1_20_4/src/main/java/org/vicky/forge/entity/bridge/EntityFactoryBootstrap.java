package org.vicky.forge.entity.bridge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.MobRegisteringClass;
import org.vicky.platform.entity.RegisterFactory;

public final class EntityFactoryBootstrap {
    private static final Type REGISTER_FACTORY =
            Type.getType(RegisterFactory.class);

    public static void loadFactories(PlatformPlugin plugin) {
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            scanData.getAnnotations().forEach(annotation -> {

                if (!annotation.annotationType().equals(REGISTER_FACTORY)) {
                    return;
                }

                String className = annotation.clazz().getClassName();

                try {
                    Class<?> clazz = Class.forName(className);

                    // Enforce contract
                    if (!MobRegisteringClass.class.isAssignableFrom(clazz)) {
                        throw new IllegalStateException(
                                "@RegisterFactory used on non-MobRegisteringClass: " + className
                        );
                    }

                    // Instantiate (must have no-arg constructor)
                    MobRegisteringClass factory =
                            (MobRegisteringClass) clazz.getDeclaredConstructor().newInstance();

                    factory.register(plugin);

                } catch (Throwable t) {
                    throw new RuntimeException(
                            "Failed to load @RegisterFactory class: " + className, t
                    );
                }
            });
        }
    }
}
