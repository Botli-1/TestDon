
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

    private boolean compact;

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
            double mouseX,
            double mouseY,
            int x,
            int y,
            int w,
            int h
    ) {
        return mouseX >= x && mouseX <= x + w
            && mouseY >= y && mouseY <= y + h;
    }

    private void calculateLayout() {
        panelW = Math.min(width - 16, 780);
        panelH = Math.min(height - 16, 560);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        compact = panelW < 620;
    }

    private int getContentX() {
        return panelX + (compact ? 105 : 122);
    }

    private int getContentWidth() {
        if (compact) {
            return panelW - 117;
        }

        return panelW - 250;
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        calculateLayout();

        context.fill(
            0, 0, width, height,
            0x99000000
        );

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
            "CLIENT MOD",
            panelX + 105,
            panelY + 15,
            0xFF8998AE,
            false
        );

        // Sidebar
        int sideX = panelX + 7;
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

            boolean selected =
                category.equals(selectedCategory);

            boolean hovered = inside(
                mouseX, mouseY,
                sideX, cy, 92, 23
            );

            if (selected) {
                context.fill(
                    sideX, cy,
                    sideX + 92, cy + 23,
                    0xFF17345C
                );
            } else if (hovered) {
                context.fill(
                    sideX, cy,
                    sideX + 92, cy + 23,
                    0xFF172334
                );
            }

            context.drawText(
                textRenderer,
                category,
                sideX + 6,
                cy + 7,
                selected ? 0xFF65BDFF : 0xFF9AA9BD,
                false
            );
        }

        // Main content
        int contentX = getContentX();
        int contentW = getContentWidth();

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

        int cardGap = 9;
        int cardH = 58;
        int startY = panelY + 91;

        int columns = compact ? 1 : 2;
        int cardW = (contentW - cardGap) / columns;

        if (modules.length == 0) {
            context.drawText(
                textRenderer,
                "No modules in this category",
                contentX + 5,
                startY + 15,
                0xFF8998AE,
                false
            );
        }

        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];

            int column = i % columns;
            int row = i / columns;

            int cardX =
                contentX + column * (cardW + cardGap);

            int cardY =
                startY + row * (cardH + 9);

            boolean enabled = isEnabled(module);

            boolean hovered = inside(
                mouseX, mouseY,
                cardX, cardY,
                cardW, cardH
            );

            // Card background
            context.fill(
                cardX, cardY,
                cardX + cardW, cardY + cardH,
                hovered ? 0xFF1B2B40 : 0xFF111C2B
            );

            // Blue indicator
            context.fill(
                cardX, cardY,
                cardX + 2, cardY + cardH,
                enabled ? 0xFF38A9FF : 0xFF344155
            );

            context.drawText(
                textRenderer,
                module,
                cardX + 9,
                cardY + 10,
                0xFFE7EDF7,
                false
            );

            String description = switch (module) {
                case "Entity ESP" ->
                    "Highlights entities";

                case "Chest ESP" ->
                    "Highlights loaded chests";

                case "Sound ESP" ->
                    "Shows sound locations";

                case "Through Walls" ->
                    "See markers through blocks";

                case "Chunk Finder" ->
                    "Finds storage concentrations";

                default ->
                    "Test Don module";
            };

            context.drawText(
                textRenderer,
                description,
                cardX + 9,
                cardY + 32,
                0xFF8293A9,
                false
            );

            // Toggle switch
            int toggleX = cardX + cardW - 39;
            int toggleY = cardY + 12;

            context.fill(
                toggleX, toggleY,
                toggleX + 30, toggleY + 17,
                enabled ? 0xFF1679BD : 0xFF354154
            );

            int knobX =
                enabled ? toggleX + 17 : toggleX + 3;

            context.fill(
                knobX, toggleY + 3,
                knobX + 10, toggleY + 14,
                0xFFFFFFFF
            );
        }

        // Quick actions
        if (compact) {
            int actionY = panelY + panelH - 48;
            int actionW = (contentW - 8) / 2;

            context.drawText(
                textRenderer,
                "QUICK ACTIONS",
                contentX,
                actionY - 15,
                0xFF71839B,
                false
            );

            drawAction(
                context, mouseX, mouseY,
                contentX, actionY,
                actionW, "ENABLE ALL"
            );

            drawAction(
                context, mouseX, mouseY,
                contentX + actionW + 8,
                actionY,
                actionW, "DISABLE ALL"
            );

        } else {
            int rightX = panelX + panelW - 112;

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
                100, "ENABLE ALL"
            );

            drawAction(
                context, mouseX, mouseY,
                rightX, panelY + 108,
                100, "DISABLE ALL"
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

            context.drawText(
                textRenderer,
                "ENABLED: " + getEnabledCount(),
                rightX,
                panelY + 201,
                0xFF55B9FF,
                false
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private int getEnabledCount() {
        int count = 0;

        if (TestDonSettings.entities) count++;
        if (TestDonSettings.chests) count++;
        if (TestDonSettings.sounds) count++;
        if (TestDonSettings.throughWalls) count++;
        if (TestDonSettings.chunkFinder) count++;

        return count;
    }

    private void drawAction(
            DrawContext context,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            String label
    ) {
        boolean hovered = inside(
            mouseX, mouseY,
            x, y, w, 22
        );

        context.fill(
            x, y,
            x + w, y + 22,
            hovered ? 0xFF24466A : 0xFF17263A
        );

        context.drawCenteredTextWithShadow(
            textRenderer,
            label,
            x + w / 2,
            y + 7,
            0xFFDCEBFA
        );
    }

    @Override
    public boolean mouseClicked(
            Click click,
            boolean doubled
    ) {
        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }

        calculateLayout();

        double mouseX = click.x();
        double mouseY = click.y();

        // Sidebar selection
        int sideX = panelX + 7;
        int sideY = panelY + 55;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int cy = sideY + 20 + i * 27;

            if (inside(
                    mouseX, mouseY,
                    sideX, cy, 92, 23
            )) {
                selectedCategory = CATEGORIES[i];
                return true;
            }
        }

        // Module cards
        int contentX = getContentX();
        int contentW = getContentWidth();

        String[] modules = getModules();

        int columns = compact ? 1 : 2;
        int cardGap = 9;
        int cardH = 58;
        int cardW = (contentW - cardGap) / columns;
        int startY = panelY + 91;

        for (int i = 0; i < modules.length; i++) {
            int column = i % columns;
            int row = i / columns;

            int cardX =
                contentX + column * (cardW + cardGap);

            int cardY =
                startY + row * (cardH + 9);

            if (inside(
                    mouseX, mouseY,
                    cardX, cardY,
                    cardW, cardH
            )) {
                toggleModule(modules[i]);
                return true;
            }
        }

        // Quick actions
        if (compact) {
            int actionY = panelY + panelH - 48;
            int actionW = (contentW - 8) / 2;

            if (inside(
                    mouseX, mouseY,
                    contentX, actionY,
                    actionW, 22
            )) {
                setAll(true);
                return true;
            }

            if (inside(
                    mouseX, mouseY,
                    contentX + actionW + 8,
                    actionY,
                    actionW, 22
            )) {
                setAll(false);
                return true;
            }

        } else {
            int rightX = panelX + panelW - 112;

            if (inside(
                    mouseX, mouseY,
                    rightX, panelY + 80,
                    100, 22
            )) {
                setAll(true);
                return true;
            }

            if (inside(
                    mouseX, mouseY,
                    rightX, panelY + 108,
                    100, 22
            )) {
                setAll(false);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        return super.keyPressed(input);
    }
                    }
