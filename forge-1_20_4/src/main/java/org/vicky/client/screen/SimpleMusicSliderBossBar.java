package org.vicky.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.vicky.forgeplatform.useables.ForgeIColor;

public class SimpleMusicSliderBossBar implements Renderable {
    private float progress = 1.0f;
    private Component title = Component.empty();
    private Component subTitle = Component.empty();
    private ForgeIColor color = new ForgeIColor(100, 100, 0);
    private float animationProgress = 0.0f; // 0 (hidden) to 1 (fully shown)
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean visible = true;
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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Calculate delta for animations
        long now = System.currentTimeMillis();
        float delta = (now - lastUpdateTime) / 1000.0f;
        lastUpdateTime = now;

        if (visible && animationProgress < 1.0f) {
            animationProgress += delta * 4.0f; // Slide-in speed
        } else if (!visible && animationProgress > 0.0f) {
            animationProgress -= delta * 4.0f; // Slide-out speed
        }

        animationProgress = Math.max(0.0f, Math.min(1.0f, animationProgress));

        if (animationProgress <= 0.01f) return; // Fully hidden

        int width = graphics.guiWidth();
        int barWidth = 200;
        int barHeight = 20;
        int x = (width - barWidth) / 2;
        int y = (int) (30 - (1.0f - animationProgress) * 40); // Slide from top

        // Background
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xAA000000);

        // Progress Bar
        int filled = (int) (barWidth * progress);
        graphics.fill(x + 2, y + 2, x + 2 + filled - 4, y + barHeight - 2, 0xFF00FF00);

        // Title
        graphics.drawCenteredString(Minecraft.getInstance().font, title, x + barWidth / 2, y + 5, 0xFFFFFFFF);
        graphics.drawCenteredString(Minecraft.getInstance().font, subTitle, x + barWidth / 2, y + 14, 0xFFFFFFFF);

        graphics.fill(x, barHeight - 5, width, barHeight, color.toNativeInt());

        if (image != null) {
            graphics.blit(image, x - 24, y, 24, 24, 0, 0, 24, 24, 24, 24);
        }
    }

    public void setImage(@Nullable ResourceLocation image) {
        if (image != null) {
            this.image = image;
        }
    }
}

