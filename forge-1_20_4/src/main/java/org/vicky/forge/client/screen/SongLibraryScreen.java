package org.vicky.forge.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.music.SongEntry;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.packets.PlaySpecifyableSong;

import java.util.List;

import static org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeHacks.fromVicky;

public class SongLibraryScreen extends Screen {
    // GUI size like a 9x3 chest (vanilla) style
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int SLOTS_PER_ROW = 9;
    private static final int ROWS = 3;
    private static final int PAGE_SIZE = SLOTS_PER_ROW * ROWS;

    private final List<SongEntry> songs;
    // layout metrics inside the GUI
    private final int slotSize = 36; // area for the icon + text
    private final int slotPaddingX = 4;
    private final int slotPaddingY = 6;
    private int page = 0;
    private int left; // top-left of GUI on screen
    private int top;

    public SongLibraryScreen(List<String> songs) {
        super(Component.literal("Your Songs"));
        this.songs = songs.stream().map(SongEntry::new).toList();
    }

    @Override
    protected void init() {
        super.init();

        // center the GUI
        left = (this.width - GUI_WIDTH) / 2;
        top = (this.height - GUI_HEIGHT) / 2;

        // clear previous widgets
        this.clearWidgets();

        // Add Prev/Next page buttons if needed
        int buttonY = top + GUI_HEIGHT - 24;
        int prevX = left + 10;
        int nextX = left + GUI_WIDTH - 10 - 60;
        if (page > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
                page--;
                init(); // re-init to refresh buttons
            }).bounds(prevX, buttonY, 60, 20).build());
        }
        if ((page + 1) * PAGE_SIZE < songs.size()) {
            this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
                page++;
                init();
            }).bounds(nextX, buttonY, 60, 20).build());
        }

        // Create clickable areas for each slot on this page
        int startIndex = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = startIndex + i;
            if (idx >= songs.size()) break;

            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            // compute slot top-left
            int gridLeft = left + 8 + col * (slotSize + slotPaddingX);
            int gridTop = top + 20 + row * (slotSize + slotPaddingY);

            // add invisible button on top of the slot
            SongEntry entry = songs.get(idx);
            this.addRenderableWidget(Button.builder(Component.literal(""),
                            btn -> {
                                // when clicked, close UI and send play packet
                                this.onClose();
                                PacketHandler.sendToServer(new PlaySpecifyableSong(entry.id));
                            })
                    .bounds(gridLeft, gridTop, slotSize, slotSize)
                    .build());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // correct background call for MC 1.20+
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        // draw a simple GUI background box (you can replace with textured background)
        // semi-opaque dark rectangle
        graphics.fill(left - 4, top - 4, left + GUI_WIDTH + 4, top + GUI_HEIGHT + 4, 0xC0101010);
        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFF202020);

        // title
        graphics.drawCenteredString(this.font, this.title.getString(), left + GUI_WIDTH / 2, top + 6, 0xFFFFFF);

        // draw the slots for this page
        int startIndex = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = startIndex + i;
            if (idx >= songs.size()) break;

            SongEntry entry = songs.get(idx);

            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            int gridLeft = left + 8 + col * (slotSize + slotPaddingX);
            int gridTop = top + 20 + row * (slotSize + slotPaddingY);

            // slot background (simple rounded-ish rectangle look)
            graphics.fill(gridLeft - 2, gridTop - 2, gridLeft + slotSize + 2, gridTop + slotSize + 2, 0xFF101010);
            graphics.fill(gridLeft - 1, gridTop - 1, gridLeft + slotSize + 1, gridTop + slotSize + 1, 0xFF2A2A2A);

            // draw icon (centered on top portion)
            int iconSize = 20;
            int iconX = gridLeft + (slotSize - iconSize) / 2;
            int iconY = gridTop + 2;

            drawIcon(graphics, fromVicky(entry.icon), iconX, iconY, iconSize, iconSize);

            // draw title (truncate if too long)
            String title = entry.title;
            if (title.length() > 18) title = title.substring(0, 15) + "...";
            int titleX = gridLeft + 4;
            int titleY = gridTop + iconSize + 6;
            graphics.drawString(this.font, Component.literal(title), titleX, titleY, 0xFFFFFF, false);

            // draw authors in gray underneath
            String authors = entry.authors == null ? "" : entry.authors;
            if (authors.length() > 20) authors = authors.substring(0, 17) + "...";
            int authorsY = titleY + 10;
            graphics.drawString(this.font, Component.literal(authors), titleX, authorsY, 0xAAAAAA, false);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Draw a texture-based icon.
     * NOTE: If your icons are item textures that live in item model atlas, you should use ItemRenderer instead.
     * This example assumes the icon is a simple 1:1 texture PNG stored under assets/<ns>/textures/...
     */
    private void drawIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, int w, int h) {
        if (icon == null) return;

        // bind the texture and draw a simple quad
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, icon);

        // The blit variant varies between mappings/versions; this should work in most 1.20 setups:
        // draw the entire texture into the rectangle (assuming the texture is exactly w x h or will be scaled)
        graphics.blit(icon, x, y, w, h, 0, 0, w, h, w, h);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // do not pause the game while open
    }
}