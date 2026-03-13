/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import java.util.List;
import java.util.Optional;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.vicky.forge.entity.effects.ForgePlatformEffectBridge;
import org.vicky.platform.entity.EffectDescriptor;
import org.vicky.platform.entity.RegisterEffect;

import com.mojang.logging.LogUtils;

public final class EffectBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll() {
		LOGGER.info("Scanning Effects...");

		List<AnnotationScanner.ScanResult> found =
				AnnotationScanner.scanFor(FMLLoader.backgroundScanHandler,
						RegisterEffect.class);

		for (AnnotationScanner.ScanResult r : found) {
			LOGGER.info("Found annotation: {}", r);

			if (r.kind == AnnotationScanner.MemberKind.FIELD && r.memberName != null) {
				Optional<Object> maybeValue = AnnotationScanner.resolveFieldValue(r);
				maybeValue.ifPresent(value -> {
					if (value instanceof EffectDescriptor desc) {
						LOGGER.info("Successfully loaded effect descriptor: {}", desc.getKey());
						ForgePlatformEffectBridge.INSTANCE.registerEffect(desc);
					} else {
						LOGGER.warn("Field {} annotated with @RegisterEffect is not an EffectDescriptor", r.memberName);
					}
				});
			}
		}
	}
}
