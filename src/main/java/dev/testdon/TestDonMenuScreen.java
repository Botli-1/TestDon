
package dev.testdon;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

public final class TestDonMenuScreen extends Screen {

    private String selectedCategory = "VISUALS";

    private static final String[] CATEGORIES = {
        "COMBAT", "MOVEMENT", "PLAYER",
        "WORLD", "RENDER", "VISUALS",
        "CLIENT", "MISC", "FAVORITES"
    };

    private int panelX, panelY, panelW, panelH;
    private int sidebarW, contentX, contentW;
    private int cardStartY, cardH, cardGap;
    private int actionY, actionW;

    private int listBottom;
    private int listHeight;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private boolean compact, tiny;

    public TestDonMenuScreen() {
        super(Text.literal("Test Don"));
    }

    private void calculateLayout() {
        panelW = Math.min(width - 24,
            Math.max(280, (int) (width * 0.62)));

        panelH = Math.min(height - 24,
            Math.max(220, (int) (height * 0.70)));

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        compact = panelW < 620 || panelH < 390;
        tiny = panelW < 390 || panelH < 300;

        sidebarW = tiny ? 82 : compact ? 105 : 150;

        contentX = panelX + sidebarW + 18;
        contentW = Math.max(1,
            panelX + panelW - contentX - 20);

        cardStartY = panelY + 55;

        cardH = tiny ? 32 : compact ? 38 : 44;
        cardGap = tiny ? 6 : 7;

        actionY = panelY + panelH - 31;
        actionW = Math.max(1, (contentW - 10) / 2);

        listBottom = actionY - 8;
        listHeight = Math.max(1, listBottom - cardStartY);

        int totalContentHeight =
            getModules().length * (cardH + cardGap) - cardGap;

        maxScroll = Math.max(0,
            totalContentHeight - listHeight);

        clampScroll();
    }

    private void clampScroll() {
        scrollOffset = Math.max(0,
            Math.min(scrollOffset, maxScroll));
    }

    private String[] getModules() {
        return switch (selectedCategory) {
            case "VISUALS" -> new String[] {
                "Entity ESP",
                "Chest ESP",
                "Sound ESP"
            };

            case "WORLD" -> new String[] {
                "Chunk Finder"
            };

            case "PLAYER" -> new String[] {
                "Freecam"
            };

            default -> new String[] {};
        };
    }

    private boolean isEnabled(String module) {
        return switch (module) {
            case "Entity ESP" -> TestDonSettings.entities;
            case "Chest ESP" -> TestDonSettings.chests;
            case "Sound ESP" -> TestDonSettings.sounds;
            case "Chunk Finder" -> TestDonSettings.chunkFinder;
            case "Freecam" -> TestDonSettings.freecam;
            default -> false;
        };
    }

    private void toggleModule(String module) {
        switch (module) {
            case "Entity ESP" ->
                TestDonSettings.entities = !TestDonSettings.entities;

            case "Chest ESP" ->
                TestDonSettings.chests = !TestDonSettings.chests;

            case "Sound ESP" ->
                TestDonSettings.sounds = !TestDonSettings.sounds;

            case "Chunk Finder" ->
                TestDonSettings.chunkFinder = !TestDonSettings.chunkFinder;

            case "Freecam" ->
                TestDonSettings.freecam = !TestDonSettings.freecam;
        }
    }

    private void setAll(boolean value) {
        TestDonSettings.entities = value;
        TestDonSettings.chests = value;
        TestDonSettings.sounds = value;
        TestDonSettings.chunkFinder = value;
        TestDonSettings.freecam = value;

        // Through Walls remains enabled permanently.
        TestDonSettings.throughWalls = true;
    }

    private int enabledCount() {
        int count = 0;

        if (TestDonSettings.entities) count++;
        if (TestDonSettings.chests) count++;
        if (TestDonSettings.sounds) count++;
        if (TestDonSettings.chunkFinder) count++;
        if (TestDonSettings.freecam) count++;

        return count;
    }

    private boolean inside(double mx, double my,
            int x, int y, int w, int h) {
        return mx >= x && mx <= x + w
            && my >= y && my <= y + h;
    }

    private void fillRoundedRect(
            DrawContext context,
            int x, int y, int w, int h,
            int radius, int color) {

        for (int row = 0; row < h; row++) {
            int dy = 0;

            if (row < radius) {
                dy = radius - row;
            } else if (row >= h - radius) {
                dy = radius - (h - 1 - row);
            }

            int inset = 0;

            if (dy > 0) {
                inset = (int) Math.ceil(
                    radius - Math.sqrt(
                        radius * radius - dy * dy
                    )
                );
            }

            context.fill(
                x + inset,
                y + row,
                x + w - inset,
                y + row + 1,
                color
            );
        }
    }

    private void fillTopRoundedRect(
            DrawContext context,
            int x, int y, int w, int h,
            int radius, int color) {

        for (int row = 0; row < h; row++) {
            int dy = row < radius ? radius - row : 0;

            int inset = 0;

            if (dy > 0) {
                inset = (int) Math.ceil(
                    radius - Math.sqrt(
                        radius * radius - dy * dy
                    )
                );
            }

            context.fill(
                x + inset,
                y + row,
                x + w - inset,
                y + row + 1,
                color
            );
        }
    }

    @Override
    public void render(DrawContext context,
            int mouseX, int mouseY, float delta) {

        calculateLayout();

        context.fill(0, 0, width, height, 0x99000000);

        fillRoundedRect(context,
            panelX - 2, panelY - 2,
            panelW + 4, panelH + 4,
            16, 0xFF263B55);

        fillRoundedRect(context,
            panelX, panelY,
            panelW, panelH,
            14, 0xFF080E19);

        // Header
        fillTopRoundedRect(context,
            panelX, panelY,
            panelW, 43,
            13, 0xFF0D1728);

        context.fill(panelX, panelY + 42,
            panelX + panelW, panelY + 43,
            0xFF1B3550);

        context.drawText(textRenderer, "TEST DON",
            panelX + 17, panelY + 14,
            0xFF42B9FF, false);

        if (!tiny) {
            context.drawText(textRenderer, "CLIENT MOD",
                panelX + 130, panelY + 15,
                0xFF899CB5, false);
        }

        // Sidebar
        int sideX = panelX + 8;
        int sideY = panelY + 55;
        int sideButtonW = sidebarW - 13;
        int categoryGap = panelH < 360 ? 22 : 27;

        context.drawText(textRenderer, "CATEGORIES",
            sideX + 5, sideY,
            0xFF8295AE, false);

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];
            int cy = sideY + 22 + i * categoryGap;

            if (cy + 22 > panelY + panelH - 8) break;

            boolean selected = category.equals(selectedCategory);
            boolean hovered = inside(mouseX, mouseY,
                sideX, cy, sideButtonW, 22);

            if (selected) {
                context.fill(sideX, cy,
                    sideX + sideButtonW, cy + 22,
                    0xFF193B63);

                context.fill(sideX, cy,
                    sideX + 3, cy + 22,
                    0xFF35B5FF);

            } else if (hovered) {
                context.fill(sideX, cy,
                    sideX + sideButtonW, cy + 22,
                    0xFF17283C);
            }

            String label = category;

            if (tiny) {
                label = switch (category) {
                    case "MOVEMENT" -> "MOVE";
                    case "FAVORITES" -> "FAVS";
                    default -> category;
                };
            }

            context.drawText(textRenderer, label,
                sideX + 7, cy + 7,
                selected ? 0xFF62C4FF : 0xFF9AAAC0,
                false);
        }

        // Module cards
        String[] modules = getModules();

        context.enableScissor(
            contentX,
            cardStartY,
            contentX + contentW,
            listBottom
        );

        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];

            int cardY = cardStartY
                + i * (cardH + cardGap)
                - scrollOffset;

            if (cardY + cardH < cardStartY
                    || cardY > listBottom) {
                continue;
            }

            boolean enabled = isEnabled(module);

            boolean hovered = inside(mouseX, mouseY,
                contentX, cardY, contentW, cardH);

            context.fill(contentX, cardY,
                contentX + contentW, cardY + cardH,
                hovered ? 0xFF1C3047 : 0xFF111E2E);

            context.fill(contentX, cardY,
                contentX + 3, cardY + cardH,
                enabled ? 0xFF35B5FF : 0xFF34465D);

            context.drawText(textRenderer, module,
                contentX + 10,
                cardY + (cardH - 8) / 2,
                0xFFEAF2FC, false);

            int tx = contentX + contentW - 38;
            int ty = cardY + (cardH - 16) / 2;

            context.fill(tx, ty, tx + 28, ty + 16,
                enabled ? 0xFF168BD0 : 0xFF35465C);

            int knobX = enabled ? tx + 15 : tx + 2;

            context.fill(knobX, ty + 2,
                knobX + 11, ty + 14,
                0xFFFFFFFF);
        }

        context.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            int barX = contentX + contentW - 3;
            int barY = cardStartY;
            int barH = listHeight;

            context.fill(barX, barY,
                barX + 3, barY + barH,
                0xFF17283C);

            int thumbH = Math.max(18,
                barH * barH / (barH + maxScroll));

            int thumbY = barY
                + (barH - thumbH) * scrollOffset
                / maxScroll;

            context.fill(barX, thumbY,
                barX + 3, thumbY + thumbH,
                0xFF35B5FF);
        }

        // Quick actions
        drawAction(context, mouseX, mouseY,
            contentX, actionY, actionW, "ENABLE ALL");

        drawAction(context, mouseX, mouseY,
            contentX + actionW + 10, actionY,
            actionW, "DISABLE ALL");

        if (!compact) {
            context.drawText(textRenderer,
                "ENABLED: " + enabledCount(),
                panelX + panelW - 115,
                panelY + 15,
                0xFF55B9FF, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawAction(DrawContext context,
            int mouseX, int mouseY,
            int x, int y, int w, String label) {

        boolean hovered = inside(mouseX, mouseY, x, y, w, 23);

        context.fill(x, y, x + w, y + 23,
            hovered ? 0xFF244D75 : 0xFF172B43);

        context.fill(x, y, x + w, y + 1,
            0xFF28527A);

        context.drawCenteredTextWithShadow(
            textRenderer, label,
            x + w / 2, y + 8,
            0xFFE8F3FF
        );
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }

        calculateLayout();

        double mx = click.x();
        double my = click.y();

        // Category selection
        int sideX = panelX + 8;
        int sideY = panelY + 55;
        int sideButtonW = sidebarW - 13;
        int categoryGap = panelH < 360 ? 22 : 27;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int cy = sideY + 22 + i * categoryGap;

            if (cy + 22 > panelY + panelH - 8) break;

            if (inside(mx, my, sideX, cy, sideButtonW, 22)) {
                selectedCategory = CATEGORIES[i];
                scrollOffset = 0;
                return true;
            }
        }

        // Module selection
        if (inside(mx, my,
                contentX, cardStartY,
                contentW, listHeight)) {

            String[] modules = getModules();

            for (int i = 0; i < modules.length; i++) {
                int cardY = cardStartY
                    + i * (cardH + cardGap)
                    - scrollOffset;

                if (inside(mx, my,
                        contentX, cardY, contentW, cardH)) {
                    toggleModule(modules[i]);
                    return true;
                }
            }
        }

        if (inside(mx, my, contentX, actionY, actionW, 23)) {
            setAll(true);
            return true;
        }

        if (inside(mx, my,
                contentX + actionW + 10,
                actionY, actionW, 23)) {
            setAll(false);
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
            double horizontalAmount, double verticalAmount) {

        calculateLayout();

        if (inside(mouseX, mouseY,
                contentX, cardStartY,
                contentW, listHeight)
                && maxScroll > 0) {

            scrollOffset -= (int) (verticalAmount * 20);
            clampScroll();
            return true;
        }

        return super.mouseScrolled(
            mouseX, mouseY, horizontalAmount, verticalAmount
        );
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_DOWN) {
            scrollOffset += 25;
            clampScroll();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_UP) {
            scrollOffset -= 25;
            clampScroll();
            return true;
        }

        return super.keyPressed(input);
    }
}
