/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.vicky.bukkitplatform.useables.BukkitBossBarDescriptor;
import org.vicky.bukkitplatform.useables.BukkitPlatformBossBar;
import org.vicky.bukkitplatform.useables.ColorAdapters;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.utils.BossBarDescriptor;

import java.lang.reflect.Method;

import static org.vicky.bukkitplatform.useables.BukkitBossBarDescriptor.*;

public class BukkitBossBarFactory implements PlatformBossBarFactory {
    public static BarColor adaptColor(IColor color) {
        return ColorAdapters.adaptColor(color);
	}

    public static BarStyle adaptOverlay(BossBarOverlay overlay) {
		return switch (overlay) {
            case PROGRESS -> BarStyle.SOLID;
            case NOTCHED_6 -> BarStyle.SEGMENTED_6;
            case NOTCHED_10 -> BarStyle.SEGMENTED_10;
            case NOTCHED_12 -> BarStyle.SEGMENTED_12;
            case NOTCHED_20 -> BarStyle.SEGMENTED_20;
		};
	}

    public static BossBar toBukkitBossBar(BossBarDescriptor descriptor) {
        // Determine BarColor and BarStyle
        BarColor barColor = mapToBarColor(descriptor.color);
        BarStyle barStyle = mapToBarStyle(descriptor.overlay);

        // Attempt: Bukkit.createBossBar(Component, BarColor, BarStyle) (Paper supports
        // Component)
        try {
            Method createMethod = Bukkit.class.getMethod("createBossBar", Component.class, BarColor.class,
                    BarStyle.class);
            Object created = createMethod.invoke(null, descriptor.title != null ? descriptor.title : Component.empty(),
                    barColor, barStyle);
            if (created instanceof BossBar) {
                BossBar bb = (BossBar) created;
                configureBukkitBossBar(bb, descriptor);
                return bb;
            }
        } catch (NoSuchMethodException ignored) {
            // Server doesn't support Component param — fall through to string creation
        } catch (Throwable t) {
            // If reflection invocation fails, try fallback below
        }

        // Fallback: use text string (works on all Bukkit variants)
        String text = PlainTextComponentSerializer.plainText()
                .serialize(descriptor.title != null ? descriptor.title : Component.empty());
        BossBar bossBar = Bukkit.createBossBar(text, barColor, barStyle);
        configureBukkitBossBar(bossBar, descriptor);
        return bossBar;
    }

    @Override
    public <T extends BossBarDescriptor> PlatformBossBar createBossBar(T descriptor) {
        if (descriptor instanceof BukkitBossBarDescriptor descriptor1) {
            org.bukkit.boss.BossBar bar = descriptor1.toBukkitBossBar();
            return new BukkitPlatformBossBar(bar, descriptor1);
        }
        return new BukkitPlatformBossBar(toBukkitBossBar(descriptor), descriptor);
    }
}
