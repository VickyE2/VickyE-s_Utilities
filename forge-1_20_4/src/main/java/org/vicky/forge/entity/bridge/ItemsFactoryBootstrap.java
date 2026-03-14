/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import com.mojang.logging.LogUtils;
import io.github.classgraph.AnnotationParameterValue;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.items.ItemDescriptor;
import org.vicky.platform.items.Items;
import org.vicky.platform.items.RegisterItem;
import org.vicky.platform.utils.ResourceLocation;

import java.util.List;
import java.util.Optional;

public final class ItemsFactoryBootstrap {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void discoverAndRegisterAll(PlatformPlugin plugin) {
		LOGGER.info("Scanning Item Descriptors... ");

		List<AnnotationScanner.ScanResult> found =
				AnnotationScanner.scanFor(FMLLoader.backgroundScanHandler,
						RegisterItem.class);

		for (AnnotationScanner.ScanResult r : found) {
			LOGGER.info("Found annotation: {}", r);
			// inspect attribute map:
			AnnotationParameterValue ns = r.annotationValues.get("namespace");
			AnnotationParameterValue path = r.annotationValues.get("path");
			if (r.kind == AnnotationScanner.MemberKind.FIELD && r.memberName != null) {
				Optional<Object> maybeValue = AnnotationScanner.resolveFieldValue(r);
				maybeValue.ifPresent(value -> {
					if (value instanceof ItemDescriptor desc) {
						if (ns.getValue() instanceof String namespace &&
								path.getValue() instanceof String path1) {
							LOGGER.info("Successfully loaded item descriptor: {}:{}", ns, path1);
							plugin.getPlatformItemFactory().registerItem(ResourceLocation.from(namespace, path1), desc);
						}
						else {
							LOGGER.error("The namespace or path for the annotation was null");
						}
					}
					else {
						LOGGER.error("Field {} annotated with @RegisterItem is not a ItemDescriptor", r.memberName);
					}
				});
			}
		}
	}
}
