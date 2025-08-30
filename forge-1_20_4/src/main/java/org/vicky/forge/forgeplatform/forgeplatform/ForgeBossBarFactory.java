package org.vicky.forge.forgeplatform.forgeplatform;

import net.minecraft.resources.ResourceLocation;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeDefaultPlatformBossBar;
import org.vicky.forge.forgeplatform.forgeplatform.useables.MusicScreenSlidingBossBar;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.utils.BossBarDescriptor;

public class ForgeBossBarFactory implements PlatformBossBarFactory {
    @Override
    public <T extends BossBarDescriptor> PlatformBossBar createBossBar(T descriptor) {
        return switch (descriptor.context) {
            case "music" -> {
                var image = descriptor.getInformation().get("icon");
                @SuppressWarnings("deprection, removal")
                ResourceLocation resource = new ResourceLocation((String) image);
                yield new MusicScreenSlidingBossBar(
                        descriptor,
                        descriptor.title, descriptor.subTitle,
                        descriptor.progress, descriptor.color.toHex(),
                        resource
                );
            }
            default -> new ForgeDefaultPlatformBossBar(descriptor);
        };
    }
}
