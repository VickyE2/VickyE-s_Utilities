/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.vicky.platform.utils.IntVec3;

public class ForgeHacks {
    public static ResourceLocation fromVicky(org.vicky.platform.utils.ResourceLocation resourceLocation) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath());
    }
    public static org.vicky.platform.utils.ResourceLocation toVicky(ResourceLocation resourceLocation) {
        return org.vicky.platform.utils.ResourceLocation.from(resourceLocation.getNamespace(), resourceLocation.getPath());
    }
    public static IntVec3 toVicky(BlockPos pos) {
        return IntVec3.of(pos.getX(), pos.getY(), pos.getZ());
    }
}
