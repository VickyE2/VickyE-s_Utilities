package org.vicky.forgeplatform;

import com.mojang.authlib.minecraft.client.ObjectMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.resources.ResourceLocation;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.forgeplatform.useables.ForgeDefaultPlatformBossBar;
import org.vicky.forgeplatform.useables.MusicScreenSlidingBossBar;

import java.util.Map;

public class ForgeBossBarFactory implements PlatformBossBarFactory {
    @Override
    public PlatformBossBar createBossBar(Component title, Float progress, BossBarColor color, BossBarOverlay overlay, String context) {
        return switch(context) {
            case "music" -> {
                String[] data = context.split("extra");
                Map<String, String> map = ObjectMapper.create().readValue(data[1], Map.class);
                var subTitle = map.get("subTitle");
                Component subT = Component.text("");
                if (subTitle != null)
                    subT = GsonComponentSerializer.gson().deserialize(subTitle);
                var image = map.get("image");
                ResourceLocation resource = null;
                if (image != null)
                    resource = new ResourceLocation(image);
                yield new MusicScreenSlidingBossBar(title, subT, progress, map.get("color") != null ? map.get("color") : color.name().toLowerCase(), resource);
            }
            default -> new ForgeDefaultPlatformBossBar(title, progress, color, overlay);
        };
    }
}
