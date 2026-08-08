/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import io.github.classgraph.AnnotationParameterValue;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.vicky.forge.forgeplatform.ForgePlatformCreativeTabs;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.items.CreativeTabDescriptor;
import org.vicky.platform.items.RegisterCreativeTab;
import org.vicky.platform.utils.ResourceLocation;

import java.util.List;
import java.util.Optional;

public final class CreativeTabBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll(PlatformPlugin plugin) {
		LOGGER.info("Scanning CreativeTabs Descriptors... ");

		List<AnnotationScanner.ScanResult> found =
				AnnotationScanner.scanFor(FMLLoader.backgroundScanHandler,
						RegisterCreativeTab.class);

		for (AnnotationScanner.ScanResult r : found) {
			LOGGER.info("Found annotation: {}", r);
			// inspect attribute map:
			AnnotationParameterValue ns = r.annotationValues.get("namespace");
			AnnotationParameterValue path = r.annotationValues.get("path");
			if (r.kind == AnnotationScanner.MemberKind.FIELD && r.memberName != null) {
				Optional<Object> maybeValue = AnnotationScanner.resolveFieldValue(r);
				maybeValue.ifPresent(value -> {
					if (value instanceof CreativeTabDescriptor desc) {
						if (ns.getValue() instanceof String namespace &&
								path.getValue() instanceof String path1) {
							LOGGER.info("Successfully loaded delegate tab descriptor: {}:{}", ns.toStringWithSimpleNames(), path1);
							((ForgePlatformCreativeTabs) plugin.getPlatformCreativeTabRegistry()).registerPlatformTab(ResourceLocation.from(namespace, path1), desc);
						}
						else {
							LOGGER.error("The namespace or path for the annotation was null");
						}
					}
					else {
						LOGGER.error("Field {} annotated with @RegisterCreativeTab is not a CreativeTabDescriptor", r.memberName);
					}
				});
			}
		}
	}
}
