package org.vicky.forgeplatform.useables;

import net.minecraft.resources.ResourceLocation;

public class ForgeHacks {
    public static ResourceLocation fromVicky(org.vicky.platform.utils.ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.asString());
    }
}
