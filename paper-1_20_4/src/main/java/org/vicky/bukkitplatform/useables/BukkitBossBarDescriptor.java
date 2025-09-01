/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;
import org.vicky.platform.IColor;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.utils.BossBarDescriptor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Bukkit-specific extension of BossBarDescriptor that can convert itself into
 * an org.bukkit.boss.BossBar and provides convenience methods to
 * show/hide/update.
 * <p>
 * Place this in your Bukkit module (compileOnly on paper/spigot api).
 */
public class BukkitBossBarDescriptor extends BossBarDescriptor {

    public BukkitBossBarDescriptor(Component title, Component subTitle, float progress, IColor color,
                                   BossBarOverlay overlay, String context, Map<String, Object> data) {
        super(title, subTitle, progress, color, overlay, context, data);
    }

    public BukkitBossBarDescriptor(Component title, Component subTitle, float progress, IColor color,
                                   BossBarOverlay overlay, String context) {
        super(title, subTitle, progress, color, overlay, context);
    }

    public BukkitBossBarDescriptor(Component title) {
        super(title);
    }

    /**
     * Update an existing BossBar with the descriptor values (title, progress,
     * color, style).
     */
    public static void applyTo(BossBar bar, BossBarDescriptor descriptor) {
        if (bar == null)
            return;
        // Title: try to set Component if supported, else set plain text
        try {
            Method setTitleComp = bar.getClass().getMethod("setTitle", Component.class);
            setTitleComp.invoke(bar, descriptor.title != null ? descriptor.title : Component.empty());
        } catch (NoSuchMethodException e) {
            // fallback
            bar.setTitle(PlainTextComponentSerializer.plainText()
                    .serialize(descriptor.title != null ? descriptor.title : Component.empty()));
        } catch (Throwable ignored) {
            /* ignore reflection issues */
        }

        // Progress
        try {
            bar.setProgress(Math.max(0.0F, Math.min(1.0F, descriptor.progress)));
        } catch (Throwable ignored) {
        }

        // Color & Style
        BarColor mapped = mapToBarColor(descriptor.color);
        BarStyle style = mapToBarStyle(descriptor.overlay);
        try {
            bar.setColor(mapped);
        } catch (Throwable ignored) {
        }
        try {
            bar.setStyle(style);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Convenience overload without plugin param
     */
    public BossBar toBukkitBossBar() {
        return toBukkitBossBar(null);
    }

    /**
     * Map your IColor to Bukkit BarColor. Uses reflection to extract an RGB integer
     * if possible, then picks the nearest BarColor. If color is null or cannot be
     * extracted, default WHITE.
     */
    public static BarColor mapToBarColor(IColor color) {
        if (color == null)
            return BarColor.WHITE;

        // try reflection: common getters (getRGB, asRGB, toRGB, getColor)
        Integer rgb = tryExtractRgb(color);
        if (rgb == null)
            return BarColor.WHITE;

        return nearestBarColor(rgb);
    }

    public static Integer tryExtractRgb(IColor color) {
        try {
            Class<?> cls = color.getClass();
            String[] methods = new String[]{"getRGB", "asRGB", "toRGB", "getColor", "rgb", "getValue"};
            for (String m : methods) {
                try {
                    Method method = cls.getMethod(m);
                    Object val = method.invoke(color);
                    if (val instanceof Integer)
                        return (Integer) val;
                    if (val instanceof Number)
                        return ((Number) val).intValue();
                    if (val instanceof String) {
                        String s = ((String) val).trim();
                        if (s.startsWith("#"))
                            s = s.substring(1);
                        return Integer.parseInt(s, 16);
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Useful helper to configure ticks and subtitle if server supports it
     */
    public static void configureBukkitBossBar(BossBar bar, BossBarDescriptor descriptor) {
        // Set initial values
        applyTo(bar, descriptor);

        // Some servers expose setVisible etc. We'll leave defaults; callers may adjust.
    }

    /**
     * Map BossBarOverlay (your enum) to Bukkit BarStyle by name heuristics
     */
    public static BarStyle mapToBarStyle(BossBarOverlay overlay) {
        if (overlay == null)
            return BarStyle.SOLID;
        String name = overlay.name().toUpperCase();

        // Common matches: look for digits or keywords
        if (name.contains("6"))
            return BarStyle.SEGMENTED_6;
        if (name.contains("10"))
            return BarStyle.SEGMENTED_10;
        if (name.contains("12"))
            return BarStyle.SEGMENTED_12;
        if (name.contains("20"))
            return BarStyle.SEGMENTED_20;
        if (name.contains("SEGMENT") || name.contains("NOTCH") || name.contains("NOTCHED")) {
            // default segmentation if unspecified - choose 10
            return BarStyle.SEGMENTED_10;
        }
        // fallback
        return BarStyle.SOLID;
    }

    /**
     * Show the descriptor's boss bar to the provided players (creates a new boss
     * bar internally).
     */
    public BossBar showTo(Collection<? extends org.bukkit.entity.Player> players) {
        BossBar bar = toBukkitBossBar(null);
        for (org.bukkit.entity.Player p : players) {
            bar.addPlayer(p);
        }
        return bar;
    }

    /**
     * Show to a single player. Returns the created BossBar so caller can remove
     * later.
     */
    public BossBar showTo(org.bukkit.entity.Player player) {
        BossBar bar = toBukkitBossBar(null);
        bar.addPlayer(player);
        return bar;
    }

    /**
     * Choose nearest BarColor by Euclidean distance in RGB space
     */
    public static BarColor nearestBarColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        BarColor best = BarColor.WHITE;
        double bestDist = Double.MAX_VALUE;

        for (BarColor bc : BarColor.values()) {
            int cand = approximateRgbForBarColor(bc);
            int cr = (cand >> 16) & 0xFF;
            int cg = (cand >> 8) & 0xFF;
            int cb = cand & 0xFF;
            double d = Math.pow(r - cr, 2) + Math.pow(g - cg, 2) + Math.pow(b - cb, 2);
            if (d < bestDist) {
                bestDist = d;
                best = bc;
            }
        }
        return best;
    }

    /**
     * Reasonable approximate RGB for Bukkit BarColor enums
     */
    public static int approximateRgbForBarColor(BarColor bc) {
        switch (bc) {
            case PINK:
                return 0xFF55FF;
            case BLUE:
                return 0x55FFFF;
            case RED:
                return 0xFF5555;
            case GREEN:
                return 0x55FF55;
            case YELLOW:
                return 0xFFFF55;
            case PURPLE:
                return 0xAA00FF;
            case WHITE:
            default:
                return 0xFFFFFF;
        }
    }

    /**
     * Create a Bukkit BossBar from this descriptor. If the platform supports
     * creating by Component, that is used. Otherwise fallback to plain text.
     *
     * @param plugin optional plugin used by some server APIs - not strictly necessary
     *               for Bukkit.createBossBar
     * @return created BossBar (not yet shown to any player)
     */
    public BossBar toBukkitBossBar(Plugin plugin) {
        // Determine BarColor and BarStyle
        BarColor barColor = mapToBarColor(this.color);
        BarStyle barStyle = mapToBarStyle(this.overlay);

        // Attempt: Bukkit.createBossBar(Component, BarColor, BarStyle) (Paper supports
        // Component)
        try {
            Method createMethod = Bukkit.class.getMethod("createBossBar", String.class, BarColor.class, BarStyle.class);
            Object created = createMethod.invoke(null,
                    this.title != null
                            ? PlainTextComponentSerializer.plainText().serialize(this.title)
                            : "Component.empty()",
                    barColor, barStyle);
            if (created instanceof BossBar) {
                BossBar bb = (BossBar) created;
                configureBukkitBossBar(bb, this);
                return bb;
            }
        } catch (NoSuchMethodException ignored) {
            // Server doesn't support Component param — fall through to string creation
        } catch (Throwable t) {
            // If reflection invocation fails, try fallback below
        }

        // Fallback: use text string (works on all Bukkit variants)
        String text = PlainTextComponentSerializer.plainText()
                .serialize(this.title != null ? this.title : Component.empty());
        BossBar bossBar = Bukkit.createBossBar(text, barColor, barStyle);
        configureBukkitBossBar(bossBar, this);
        return bossBar;
    }

    // Optionally override clone if you want to return BukkitBossBarDescriptor
    // specifically
    @Override
    public BukkitBossBarDescriptor clone() {
        BossBarDescriptor c = super.clone();
        // safe cast because clone returns BossBarDescriptor
        if (c instanceof BukkitBossBarDescriptor)
            return (BukkitBossBarDescriptor) c;
        // otherwise, create a new instance preserving data
        BukkitBossBarDescriptor d = new BukkitBossBarDescriptor(c.getTitle(), c.getSubTitle(), c.getProgress(),
				c.getColor(), c.getOverlay(), c.getContext(), c.getInformation());
		return d;
	}
}