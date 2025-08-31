package org.vicky.forge.forgeplatform.forgeplatform.useables;

import net.minecraft.resources.ResourceLocation;

public class ForgeHacks {
    public static ResourceLocation fromVicky(org.vicky.platform.utils.ResourceLocation resourceLocation) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath());
    }
}
