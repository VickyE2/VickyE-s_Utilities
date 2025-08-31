package org.vicky.forge.forgeplatform;

import net.minecraft.resources.ResourceLocation;
import org.vicky.forge.forgeplatform.useables.ForgeDefaultPlatformBossBar;
import org.vicky.forge.forgeplatform.useables.MusicScreenSlidingBossBar;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.utils.BossBarDescriptor;

public class ForgeBossBarFactory implements PlatformBossBarFactory {
    @Override
    public <T extends BossBarDescriptor> PlatformBossBar createBossBar(T descriptor) {
        return switch (descriptor.context) {
            case "music" -> {
                var image = descriptor.getInformation().get("icon");
                if (image == null) {
                    image = "null";
                }
                @SuppressWarnings("removal")
                ResourceLocation resource = new ResourceLocation((String) image);
                yield new MusicScreenSlidingBossBar(
                        descriptor,
                        resource
                );
            }
            default -> new ForgeDefaultPlatformBossBar(descriptor);
        };
    }
}
