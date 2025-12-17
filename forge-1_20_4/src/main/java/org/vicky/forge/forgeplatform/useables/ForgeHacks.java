/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.vicky.platform.utils.IntVec3;
import org.vicky.platform.world.PlatformLocation;

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
    public static BlockPos fromVicky(IntVec3 pos) {
        return BlockPos.of(BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ()));
    }
    public static org.vicky.platform.utils.Vec3 toVicky(Vec3 vec3) {
        return org.vicky.platform.utils.Vec3.of(vec3.x, vec3.y, vec3.z);
    }
}
