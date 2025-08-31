package org.vicky.forge.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.client.Theme;
import org.vicky.forge.music.SongEntry;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.packets.PlaySpecifyableSong;

import java.util.List;

/**
 * Scrollable, themeable song library screen.
 * - Responsive columns based on available GUI width
 * - Smooth scroll with mouse wheel
 * - Click slots via mouseClicked (no Button widgets so positions don't desync)
 * - Theme values are simple ARGB ints for quick tweaking
 */
public class SongLibraryScreen extends Screen {
    // base GUI size (used for centering); internal layout adjusts to client size
    private static final int BASE_GUI_WIDTH = 280;
    private static final int BASE_GUI_HEIGHT = 220;

    // slot defaults (tweak in Theme if you want)
    private final int slotSize = 36; // icon + small text area
    private final int slotPaddingX = 6;
    private final int slotPaddingY = 8;

    private final List<SongEntry> songs;
    private final Theme theme;

    // layout state
    private int left;
    private int top;
    private int guiWidth = BASE_GUI_WIDTH;
    private int guiHeight = BASE_GUI_HEIGHT;

    // scrolling state
    private int scrollY = 0;
    private int contentHeight = 0;
    private int innerHeight = 0;

    public SongLibraryScreen(List<String> songs) {
        super(Component.literal("Your Songs"));
        this.songs = songs.stream().map(SongEntry::new).toList();
        this.theme = Theme.SELECTED;
    }

    private static int clamp(int v, int a, int b) {
        if (v < a) return a;
        if (v > b) return b;
        return v;
    }

    @Override
    protected void init() {
        super.init();

        // adapt guiWidth to screen (max width)
        guiWidth = Math.min(BASE_GUI_WIDTH, this.width - 40);
        guiHeight = Math.min(BASE_GUI_HEIGHT, this.height - 40);

        left = (this.width - guiWidth) / 2;
        top = (this.height - guiHeight) / 2;

        // inner height for content area (leave room for title and padding)
        innerHeight = guiHeight - 40; // title + footer area

        // compute contentHeight based on columns/rows
        int columns = computeColumns();
        int rows = (int) Math.ceil((double) songs.size() / Math.max(1, columns));
        contentHeight = rows * (slotSize + slotPaddingY) + 8;

        // clamp scroll
        scrollY = clamp(scrollY, 0, Math.max(0, contentHeight - innerHeight));
    }

    private int computeColumns() {
        // use available width inside GUI (padding 16)
        int available = guiWidth - 16;
        int colWidth = slotSize + slotPaddingX;
        int cols = Math.max(1, available / colWidth);
        // limit to 12 or so to avoid extreme counts
        return Math.min(cols, 12);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // background dim
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        // panel background
        drawRoundedPanel(graphics, left, top, guiWidth, guiHeight, theme.panelBackground());

        // title
        int titleX = left + guiWidth / 2;
        graphics.drawCenteredString(this.font, this.title.getString(), titleX, top + 8, theme.titleColor());

        // content clip area (simple manual clipping by skipping draw outside area)
        int contentLeft = left + 8;
        int contentTop = top + 26;
        int contentRight = left + guiWidth - 8;
        int contentBottom = contentTop + innerHeight;

        // draw content background
        graphics.fill(contentLeft - 2, contentTop - 2, contentRight + 2, contentBottom + 2, theme.contentBackground());

        // draw slots
        int columns = computeColumns();
        int colWidth = slotSize + slotPaddingX;

        // visible range calculation to avoid drawing everything
        int firstRow = Math.max(0, (scrollY) / (slotSize + slotPaddingY));
        int lastRow = Math.min((int) Math.ceil((double) contentHeight / (slotSize + slotPaddingY)), firstRow + (innerHeight / (slotSize + slotPaddingY)) + 2);

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = 0; col < columns; col++) {
                int idx = row * columns + col;
                if (idx >= songs.size()) break;

                int x = contentLeft + col * colWidth;
                int y = contentTop + row * (slotSize + slotPaddingY) - scrollY;

                // skip drawing if outside clip vertically
                if (y + slotSize < contentTop || y > contentBottom) continue;

                // slot background
                graphics.fill(x - 2, y - 2, x + slotSize + 2, y + slotSize + 2, theme.slotBorder());
                graphics.fill(x - 1, y - 1, x + slotSize + 1, y + slotSize + 1, theme.slotBackground());

                SongEntry entry = songs.get(idx);

                // draw icon centered in top area
                int iconSize = Math.min(20, slotSize - 6);
                int iconX = x + (slotSize - iconSize) / 2;
                int iconY = y + 2;
                drawIcon(graphics, entry.icon, iconX, iconY, iconSize, iconSize);

                // draw labels
                String title = entry.title.length() > 18 ? entry.title.substring(0, 15) + "..." : entry.title;
                graphics.drawString(this.font, Component.literal(title), x + 4, y + iconSize + 6, theme.titleSmallColor(), false);

                String authors = entry.authors == null ? "" : entry.authors;
                if (authors.length() > 20) authors = authors.substring(0, 17) + "...";
                graphics.drawString(this.font, Component.literal(authors), x + 4, y + iconSize + 16, theme.subliminalText(), false);

                // hover highlight & tooltip
                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    graphics.fill(x - 1, y - 1, x + slotSize + 1, y + slotSize + 1, theme.hoverOverlay());
                    // render tooltip as usual
                    graphics.renderTooltip(Minecraft.getInstance().font, Component.literal(entry.title), mouseX, mouseY);
                }
            }
        }

        // draw scrollbar if needed
        if (contentHeight > innerHeight) {
            drawScrollbar(graphics, contentRight + 4, contentTop, 8, innerHeight, scrollY, contentHeight);
        }

        // footer hint or page info
        String hint = "Click an icon to play · Scroll to browse";
        graphics.drawString(this.font, Component.literal(hint), left + 10, top + guiHeight - 18, theme.footerColor(), false);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void drawRoundedPanel(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        // simple rectangle for now (rounded requires texture); keep it small and efficient
        graphics.fill(x, y, x + w, y + h, color);
    }

    private void drawIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, int w, int h) {
        if (icon == null) return;
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, icon);
        graphics.blit(icon, x, y, w, h, 0, 0, w, h, w, h);
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int scroll, int totalContent) {
        // background track
        graphics.fill(x, y, x + width, y + height, theme.scrollTrack());

        // compute thumb height and position
        float visibleFrac = (float) innerHeight / (float) totalContent;
        int thumbH = Math.max(16, (int) (height * visibleFrac));
        float maxScroll = totalContent - innerHeight;
        int thumbY = y + (int) ((scroll / (maxScroll <= 0 ? 1f : maxScroll)) * (height - thumbH));
        graphics.fill(x, y + thumbY, x + width, y + thumbY + thumbH, theme.scrollThumb());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double p_299502_) {
        // wheel: delta positive = up, negative = down
        int step = (int) Math.ceil(Math.abs(delta) * 20);
        if (delta > 0) scrollY = clamp(scrollY - step, 0, Math.max(0, contentHeight - innerHeight));
        else scrollY = clamp(scrollY + step, 0, Math.max(0, contentHeight - innerHeight));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // only left click
        // check if click is inside content area and match a slot
        int contentLeft = left + 8;
        int contentTop = top + 26;
        int columns = computeColumns();
        int colWidth = slotSize + slotPaddingX;

        // compute clicked relative to contentTop and scroll
        double relX = mouseX - contentLeft;
        double relY = mouseY - contentTop + scrollY;

        if (relX < 0 || relY < 0) return false;
        int col = (int) (relX / colWidth);
        int row = (int) (relY / (slotSize + slotPaddingY));
        if (col < 0 || col >= columns) return false;

        int idx = row * columns + col;
        if (idx >= 0 && idx < songs.size()) {
            SongEntry entry = songs.get(idx);
            // close and tell server to play
            this.onClose();
            PacketHandler.sendToServer(new PlaySpecifyableSong(entry.id));
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
