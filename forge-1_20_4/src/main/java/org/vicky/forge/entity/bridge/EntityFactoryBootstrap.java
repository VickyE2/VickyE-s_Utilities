/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.vicky.forge.annotationssystem.AnnotationRegisterEvent;
import org.vicky.forge.annotationssystem.PostAnnotationScanEvent;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.MobEntityDescriptor;
import org.vicky.platform.entity.RegisterMob;

import java.util.List;
import java.util.Optional;

public final class EntityFactoryBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll(PlatformPlugin plugin, PostAnnotationScanEvent event) {
		LOGGER.info("Scanning Mob Factories...");

		List<AnnotationScanner.ScanResult> found = event.getResultsFor(RegisterMob.class);

		for (AnnotationScanner.ScanResult r : found) {
			LOGGER.info("Found annotation: {}", r);

			if (r.kind == AnnotationScanner.MemberKind.FIELD && r.memberName != null) {
				Optional<Object> maybeValue = AnnotationScanner.resolveFieldValue(r);
				maybeValue.ifPresent(value -> {
					if (value instanceof MobEntityDescriptor desc) {
						LOGGER.info("Successfully loaded mob descriptor: {}", desc.getMobDetails().getMobKey());
						plugin.registerMobEntityDescriptor(desc);
					} else {
						LOGGER.warn("Field {} annotated with @RegisterMob is not a MobEntityDescriptor", r.memberName);
					}
				});
			}
		}
	}

    public static void registerTo(AnnotationRegisterEvent event) {
        event.addAnnotation(RegisterMob.class);
    }
}
