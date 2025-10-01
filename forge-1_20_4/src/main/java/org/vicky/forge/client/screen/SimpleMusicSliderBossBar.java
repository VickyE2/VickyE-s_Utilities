/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.client.Theme;
import org.vicky.forge.forgeplatform.useables.ForgeIColor;

public class SimpleMusicSliderBossBar implements Renderable {
    private float progress = 1.0f;
    private Component title = Component.empty();
    private Component subTitle = Component.empty();
    private ForgeIColor color = new ForgeIColor(100, 100, 0);
    private float animationProgress = 0.0f; // 0 (hidden) to 1 (fully shown)
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean visible = true;
    private Theme theme = Theme.SELECTED; // assign the current theme
    private @Nullable ResourceLocation image;

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSubTitle(Component subTitle) {
        this.subTitle = subTitle;
    }

    public void setColor(String hex) {
        this.color = ForgeIColor.decode(hex);
    }

    private static int parseHexToIntSafe(String hex, int fallback) {
        if (hex == null)
            return fallback;
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            int rgb = Integer.parseInt(h, 16);
            // convert RGB to ARGB (opaque)
            return 0xFF000000 | (rgb & 0xFFFFFF);
        } catch (Exception ex) {
            return fallback;
        }
    }

    // --- The improved render method (bottom-right anchored) ---
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Time delta for animations (seconds)
        long now = System.currentTimeMillis();
        float delta = (now - lastUpdateTime) / 1000.0f;
        lastUpdateTime = now;

        // Animate in/out
        final float ANIM_SPEED = 6.0f; // higher => faster
        if (visible && animationProgress < 1f)
            animationProgress = Math.min(1f, animationProgress + delta * ANIM_SPEED);
        if (!visible && animationProgress > 0f)
            animationProgress = Math.max(0f, animationProgress - delta * ANIM_SPEED);
        if (animationProgress <= 0.01f)
            return; // fully hidden

        // layout
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        int barWidth = 240;
        int barHeight = 28;
        int margin = 12;

        // final anchored position (bottom-right)
        int targetX = screenW - barWidth - margin;
        int targetY = screenH - barHeight - margin;

        // slide-from-right effect: offsetX transitions from +barWidth -> 0
        int offsetX = (int) ((1.0f - animationProgress) * (barWidth + 24)); // 24 = extra slide distance
        int x = targetX + offsetX;
        int y = targetY;

        // clamp progress
        float p = Math.max(0f, Math.min(1f, this.progress));
        int filled = (int) ((barWidth - 8) * p); // leave 4px padding left/right

        // Colors from theme (you store ARGB ints in Theme)
        int panelBg = theme.panelBackground();
        int contentBg = theme.contentBackground();
        int titleCol = theme.titleColor();
        int subtitleCol = theme.titleSmallColor();
        int progressCol = color.toNativeInt(); // fallback green
        int borderCol = theme.slotBorder();

        // --- Background (rounded look can be faked with two fills) ---
        graphics.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, borderCol); // border
        graphics.fill(x, y, x + barWidth, y + barHeight, panelBg); // panel

        // Progress track background
        int trackLeft = x + 4;
        int trackTop = y + 6;
        int trackRight = x + barWidth - 4;
        int trackBottom = y + 6 + 8;
        graphics.fill(trackLeft, trackTop, trackRight, trackBottom, contentBg);

        // Progress filled
        graphics.fill(trackLeft, trackTop, trackLeft + filled, trackBottom, progressCol);

        // Title & subtitle (use Component -> string)
        String titleStr = title == null ? "" : title.getString();
        String subStr = subTitle == null ? "" : subTitle.getString();

        // Title: left-aligned near the left of the bar
        int textLeft = x + 8;
        graphics.drawString(Minecraft.getInstance().font, Component.literal(titleStr), textLeft, y + 10, titleCol,
                false);

        // Subtitle: right-aligned small text next to title or below depending on space
        graphics.drawString(Minecraft.getInstance().font, Component.literal(subStr), textLeft, y + 18, subtitleCol,
                false);

        // Draw image (if present) to the left of the panel (clamped)
        if (image != null) {
            try {
                RenderSystem.setShaderTexture(0, image);
                // draw 20x20 icon inside the bar to the far right or left — here we'll put it
                // left of title
                int iconSize = 20;
                int iconX = x - iconSize - 6; // outside left of the panel by a small gap
                int iconY = y + (barHeight - iconSize) / 2;
                graphics.blit(image, iconX, iconY, iconSize, iconSize, 0, 0, iconSize, iconSize, iconSize, iconSize);
            } catch (Throwable t) {
                t.printStackTrace();
                // ignore broken image — don't crash the render thread
            }
        }
    }

    public void setImage(@Nullable ResourceLocation image) {
        if (image != null) {
            this.image = image;
        }
    }
}
