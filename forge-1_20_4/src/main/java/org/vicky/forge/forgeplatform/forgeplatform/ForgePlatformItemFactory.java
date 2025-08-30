package org.vicky.forge.forgeplatform.forgeplatform;

import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgePlatformItem;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgePlatformMaterial;
import org.vicky.platform.PlatformItem;
import org.vicky.platform.PlatformItemFactory;
import org.vicky.platform.world.PlatformMaterial;

public class ForgePlatformItemFactory implements PlatformItemFactory {
    @Override
    public PlatformItem fromMaterial(PlatformMaterial material) {
        if (material instanceof ForgePlatformMaterial material1) {
            return new ForgePlatformItem(material1.material().asItem().getDefaultInstance());
        }
        throw new IllegalArgumentException("Expected `ForgePlatformMaterial` got " + material.getClass().getSimpleName());
    }
}
