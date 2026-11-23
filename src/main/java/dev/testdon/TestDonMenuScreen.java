
package dev.testdon;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TestDonMenuScreen extends Screen {

    private String selectedCategory = "VISUALS";

    private static final String[] CATEGORIES = {
        "COMBAT",
        "MOVEMENT",
        "PLAYER",
        "WORLD",
        "RENDER",
        "VISUALS",
        "CLIENT",
        "MISC",
        "FAVORITES"
    };

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public TestDonMenuScreen() {
        super(Text.literal("Test Don"));
    }

    private String[] getModules() {
        return switch (selectedCategory) {
            case "VISUALS" -> new String[] {
                "Entity ESP",
                "Chest ESP",
                "Sound ESP",
                "Through Walls"
            };
            case "WORLD" -> new String[] {
                "Chunk Finder"
            };
            default -> new String[] {};
        };
    }

    private boolean isEnabled(String module) {
        return switch (module) {
            case "Entity ESP" -> TestDonSettings.entities;
            case "Chest ESP" -> TestDonSettings.chests;
            case "Sound ESP" -> TestDonSettings.sounds;
            case "Through Walls" -> TestDonSettings.throughWalls;
            case "Chunk Finder" -> TestDonSettings.chunkFinder;
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

            case "Through Walls" ->
                TestDonSettings.throughWalls = !TestDonSettings.throughWalls;

            case "Chunk Finder" ->
                TestDonSettings.chunkFinder = !TestDonSettings.chunkFinder;
        }
    }

    private void setAll(boolean value) {
        TestDonSettings.entities = value;
        TestDonSettings.chests = value;
        TestDonSettings.sounds = value;
        TestDonSettings.throughWalls = value;
        TestDonSettings.chunkFinder = value;
    }

    private boolean inside(
            double mouseX, double mouseY,
            int x, int y, int w, int h
    ) {
        return mouseX >= x && mouseX <= x + w
            && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        context.fill(0, 0, width, height, 0x99000000);

        panelW = Math.min(width - 20, 780);
        panelH = Math.min(height - 20, 500);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        // Main window
        context.fill(
            panelX, panelY,
            panelX + panelW, panelY + panelH,
            0xFF080E19
        );

        // Header
        context.fill(
            panelX, panelY,
            panelX + panelW, panelY + 42,
            0xFF0D1524
        );

        context.drawText(
            textRenderer,
            "TEST DON",
            panelX + 14,
            panelY + 10,
            0xFF55B9FF,
            false
        );

        context.drawText(
            textRenderer,
            "CLIENT MOD  |  MODULES & SETTINGS",
            panelX + 125,
            panelY + 15,
            0xFF8998AE,
            false
        );

        // Sidebar
        int sideX = panelX + 10;
        int sideY = panelY + 55;

        context.drawText(
            textRenderer,
            "CATEGORIES",
            sideX + 5,
            sideY,
            0xFF71839B,
            false
        );

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];

            int cy = sideY + 20 + i * 27;

            boolean selected = category.equals(selectedCategory);
            boolean hovered = inside(
                mouseX, mouseY,
                sideX, cy, 102, 23
            );

            if (selected) {
                context.fill(
                    sideX, cy,
                    sideX + 102, cy + 23,
                    0xFF17345C
                );
            } else if (hovered) {
                context.fill(
                    sideX, cy,
                    sideX + 102, cy + 23,
                    0xFF172334
                );
            }

            context.drawText(
                textRenderer,
                category,
                sideX + 7,
                cy + 7,
                selected ? 0xFF65BDFF : 0xFF9AA9BD,
                false
            );
        }

        // Content area
        int contentX = panelX + 122;
        int rightX = panelX + panelW - 112;
        int contentW = rightX - contentX - 12;

        context.drawText(
            textRenderer,
            selectedCategory,
            contentX,
            panelY + 55,
            0xFFFFFFFF,
            false
        );

        context.drawText(
            textRenderer,
            "MODULES",
            contentX,
            panelY + 70,
            0xFF71839B,
            false
        );

        String[] modules = getModules();

        int cardGap = 10;
        int cardW = (contentW - cardGap) / 2;
        int cardH = 58;
        int startY = panelY + 91;

        if (modules.length == 0) {
            context.drawText(
                textRenderer,
                "No modules in this category",
                contentX + 10,
                startY + 15,
                0xFF8998AE,
                false
            );
        }

        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];

            int column = i % 2;
            int row = i / 2;

            int cardX = contentX + column * (cardW + cardGap);
            int cardY = startY + row * (cardH + 9);

            boolean enabled = isEnabled(module);
            boolean hovered = inside(
                mouseX, mouseY,
                cardX, cardY, cardW, cardH
            );

            // Module card
            context.fill(
                cardX, cardY,
                cardX + cardW, cardY + cardH,
                hovered ? 0xFF1B2B40 : 0xFF111C2B
            );

            context.fill(
                cardX, cardY,
                cardX + 2, cardY + cardH,
                enabled ? 0xFF38A9FF : 0xFF344155
            );

            context.drawText(
                textRenderer,
                module,
                cardX + 10,
                cardY + 10,
                0xFFE7EDF7,
                false
            );

            String description = switch (module) {
                case "Entity ESP" -> "Highlights nearby entities";
                case "Chest ESP" -> "Highlights loaded chests";
                case "Sound ESP" -> "Shows received sound locations";
                case "Through Walls" -> "Render markers through blocks";
                case "Chunk Finder" -> "Finds storage concentrations";
                default -> "Test Don module";
            };

            context.drawText(
                textRenderer,
                description,
                cardX + 10,
                cardY + 31,
                0xFF8293A9,
                false
            );

            // Toggle
            int toggleX = cardX + cardW - 43;
            int toggleY = cardY + 12;

            context.fill(
                toggleX, toggleY,
                toggleX + 32, toggleY + 17,
                enabled ? 0xFF1679BD : 0xFF354154
            );

            int knobX = enabled ? toggleX + 18 : toggleX + 3;

            context.fill(
                knobX, toggleY + 3,
                knobX + 11, toggleY + 14,
                0xFFFFFFFF
            );
        }

        // Right panel
        context.drawText(
            textRenderer,
            "QUICK ACTIONS",
            rightX,
            panelY + 58,
            0xFF71839B,
            false
        );

        drawAction(
            context, mouseX, mouseY,
            rightX, panelY + 80,
            "ENABLE ALL"
        );

        drawAction(
            context, mouseX, mouseY,
            rightX, panelY + 108,
            "DISABLE ALL"
        );

        context.drawText(
            textRenderer,
            "CLIENT STATS",
            rightX,
            panelY + 166,
            0xFF71839B,
            false
        );

        context.drawText(
            textRenderer,
            "MODULES: 5",
            rightX,
            panelY + 185,
            0xFF9AA9BD,
            false
        );

        int enabledCount = 0;

        if (TestDonSettings.entities) enabledCount++;
        if (TestDonSettings.chests) enabledCount++;
        if (TestDonSettings.sounds) enabledCount++;
        if (TestDonSettings.throughWalls) enabledCount++;
        if (TestDonSettings.chunkFinder) enabledCount++;

        context.drawText(
            textRenderer,
            "ENABLED: " + enabledCount,
            rightX,
            panelY + 201,
            0xFF55B9FF,
            false
        );

        context.drawText(
            textRenderer,
            "RIGHT SHIFT: MENU",
            rightX,
            panelY + panelH - 24,
            0xFF71839B,
            false
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawAction(
            DrawContext context,
            int mouseX,
            int mouseY,
            int x,
            int y,
            String label
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, 100, 22);

        context.fill(
            x, y,
            x + 100, y + 22,
            hovered ? 0xFF24466A : 0xFF17263A
        );

        context.drawText(
            textRenderer,
            label,
            x + 7,
            y + 7,
            0xFFDCEBFA,
            false
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int sideX = panelX + 10;
        int sideY = panelY + 55;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int cy = sideY + 20 + i * 27;

            if (inside(mouseX, mouseY, sideX, cy, 102, 23)) {
                selectedCategory = CATEGORIES[i];
                return true;
            }
        }

        int contentX = panelX + 122;
        int rightX = panelX + panelW - 112;
        int contentW = rightX - contentX - 12;

        String[] modules = getModules();

        int cardGap = 10;
        int cardW = (contentW - cardGap) / 2;
        int cardH = 58;
        int startY = panelY + 91;

        for (int i = 0; i < modules.length; i++) {
            int column = i % 2;
            int row = i / 2;

            int cardX = contentX + column * (cardW + cardGap);
            int cardY = startY + row * (cardH + 9);

            if (inside(mouseX, mouseY, cardX, cardY, cardW, cardH)) {
                toggleModule(modules[i]);
                return true;
            }
        }

        if (inside(mouseX, mouseY, rightX, panelY + 80, 100, 22)) {
            setAll(true);
            return true;
        }

        if (inside(mouseX, mouseY, rightX, panelY + 108, 100, 22)) {
            setAll(false);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (keyCode == 256) {
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
