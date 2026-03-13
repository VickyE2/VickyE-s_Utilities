/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import java.util.List;
import java.util.Optional;

import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.RegisterMob;
import org.vicky.platform.entity.MobEntityDescriptor;

import com.mojang.logging.LogUtils;

public final class EntityFactoryBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll(PlatformPlugin plugin) {
		LOGGER.info("Scanning Mob Factories...");

		List<AnnotationScanner.ScanResult> found =
				AnnotationScanner.scanFor(FMLLoader.backgroundScanHandler,
						RegisterMob.class);

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
}
