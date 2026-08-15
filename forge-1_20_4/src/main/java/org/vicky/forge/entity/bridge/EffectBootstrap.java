/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.vicky.forge.annotationssystem.AnnotationRegisterEvent;
import org.vicky.forge.annotationssystem.PostAnnotationScanEvent;
import org.vicky.forge.entity.effects.ForgePlatformEffectBridge;
import org.vicky.platform.entity.EffectDescriptor;
import org.vicky.platform.entity.RegisterEffect;

import java.util.List;
import java.util.Optional;

public final class EffectBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll(PostAnnotationScanEvent event) {
		LOGGER.info("Scanning Effects...");

		List<AnnotationScanner.ScanResult> found =
				event.getResultsFor(RegisterEffect.class);

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

    public static void registerTo(AnnotationRegisterEvent event) {
        event.addAnnotation(RegisterEffect.class);
    }
}
