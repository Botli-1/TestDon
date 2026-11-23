
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

    private int sidebarW;
    private int contentX;
    private int contentW;

    private boolean compact;
    private boolean tiny;

    public TestDonMenuScreen() {
        super(Text.literal("Test Don"));
    }

    private void calculateLayout() {
        panelW = Math.max(1, width - 16);
        panelH = Math.max(1, height - 16);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        compact = panelW < 620 || panelH < 420;
        tiny = panelW < 390 || panelH < 300;

        sidebarW = tiny ? 76 : 100;

        contentX = panelX + sidebarW + 17;
        contentW = Math.max(1,
            panelX + panelW - contentX - (compact ? 9 : 120)
        );
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

    private int getEnabledCount() {
        int count = 0;

        if (TestDonSettings.entities) count++;
        if (TestDonSettings.chests) count++;
        if (TestDonSettings.sounds) count++;
        if (TestDonSettings.throughWalls) count++;
        if (TestDonSettings.chunkFinder) count++;

        return count;
    }

    private boolean inside(
            double mx, double my,
            int x, int y, int w, int h
    ) {
        return mx >= x && mx <= x + w
            && my >= y && my <= y + h;
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
            panelX + panelW, panelY + 40,
            0xFF0D1524
        );

        context.drawText(
            textRenderer,
            "TEST DON",
            panelX + 12,
            panelY + 10,
            0xFF55B9FF,
            false
        );

        if (!tiny) {
            context.drawText(
                textRenderer,
                "CLIENT MOD",
                panelX + 105,
                panelY + 14,
                0xFF8998AE,
                false
            );
        }

        // Sidebar
        int sideX = panelX + 5;
        int sideY = panelY + 49;
        int sideButtonW = sidebarW - 9;

        context.drawText(
            textRenderer,
            "CATEGORIES",
            sideX + 4,
            sideY,
            0xFF71839B,
            false
        );

        int categoryGap = tiny ? 23 : 26;

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];

            int cy = sideY + 19 + i * categoryGap;

            if (cy + 21 > panelY + panelH - 5) {
                break;
            }

            boolean selected =
                category.equals(selectedCategory);

            boolean hovered = inside(
                mouseX, mouseY,
                sideX, cy,
                sideButtonW, 21
            );

            if (selected) {
                context.fill(
                    sideX, cy,
                    sideX + sideButtonW, cy + 21,
                    0xFF17345C
                );
            } else if (hovered) {
                context.fill(
                    sideX, cy,
                    sideX + sideButtonW, cy + 21,
                    0xFF172334
                );
            }

            String label = category;

            if (tiny) {
                label = switch (category) {
                    case "MOVEMENT" -> "MOVE";
                    case "FAVORITES" -> "FAVS";
                    default -> category;
                };
            }

            context.drawText(
                textRenderer,
                label,
                sideX + 5,
                cy + 6,
                selected ? 0xFF65BDFF : 0xFF9AA9BD,
                false
            );
        }

        // Main content
        context.drawText(
            textRenderer,
            selectedCategory,
            contentX,
            panelY + 49,
            0xFFFFFFFF,
            false
        );

        context.drawText(
            textRenderer,
            "MODULES",
            contentX,
            panelY + 65,
            0xFF71839B,
            false
        );

        String[] modules = getModules();

        int columns = compact ? 1 : 2;
        int cardGap = tiny ? 4 : 8;
        int cardH = compact ? (tiny ? 35 : 42) : 58;
        int rowGap = compact ? 5 : 9;

        int cardW = Math.max(
            1,
            (contentW - cardGap * (columns - 1)) / columns
        );

        int startY = panelY + 84;

        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];

            int column = i % columns;
            int row = i / columns;

            int cardX =
                contentX + column * (cardW + cardGap);

            int cardY =
                startY + row * (cardH + rowGap);

            boolean enabled = isEnabled(module);

            boolean hovered = inside(
                mouseX, mouseY,
                cardX, cardY,
                cardW, cardH
            );

            // Card
            context.fill(
                cardX, cardY,
                cardX + cardW, cardY + cardH,
                hovered ? 0xFF1B2B40 : 0xFF111C2B
            );

            // Blue line
            context.fill(
                cardX, cardY,
                cardX + 2, cardY + cardH,
                enabled ? 0xFF38A9FF : 0xFF344155
            );

            context.drawText(
                textRenderer,
                module,
                cardX + 8,
                cardY + (compact ? 5 : 10),
                0xFFE7EDF7,
                false
            );

            if (!tiny) {
                String description = switch (module) {
                    case "Entity ESP" -> "Highlights entities";
                    case "Chest ESP" -> "Highlights loaded chests";
                    case "Sound ESP" -> "Shows sound locations";
                    case "Through Walls" -> "See markers through blocks";
                    case "Chunk Finder" -> "Finds storage concentrations";
                    default -> "Test Don module";
                };

                context.drawText(
                    textRenderer,
                    description,
                    cardX + 8,
                    cardY + (compact ? 22 : 32),
                    0xFF8293A9,
                    false
                );
            }

            // Toggle
            int toggleX = cardX + cardW - 36;
            int toggleY = cardY + (cardH - 17) / 2;

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
            int actionY = panelY + panelH - 28;
            int actionGap = 6;
            int actionW = Math.max(
                1, (contentW - actionGap) / 2
            );

            drawAction(
                context, mouseX, mouseY,
                contentX, actionY,
                actionW, "ENABLE ALL"
            );

            drawAction(
                context, mouseX, mouseY,
                contentX + actionW + actionGap,
                actionY,
                actionW, "DISABLE ALL"
            );
        } else {
            int rightX = panelX + panelW - 112;

            context.drawText(
                textRenderer,
                "QUICK ACTIONS",
                rightX,
                panelY + 49,
                0xFF71839B,
                false
            );

            drawAction(
                context, mouseX, mouseY,
                rightX, panelY + 72,
                100, "ENABLE ALL"
            );

            drawAction(
                context, mouseX, mouseY,
                rightX, panelY + 99,
                100, "DISABLE ALL"
            );

            context.drawText(
                textRenderer,
                "CLIENT STATS",
                rightX,
                panelY + 145,
                0xFF71839B,
                false
            );

            context.drawText(
                textRenderer,
                "MODULES: 5",
                rightX,
                panelY + 164,
                0xFF9AA9BD,
                false
            );

            context.drawText(
                textRenderer,
                "ENABLED: " + getEnabledCount(),
                rightX,
                panelY + 181,
                0xFF55B9FF,
                false
            );
        }

        super.render(context, mouseX, mouseY, delta);
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

        double mx = click.x();
        double my = click.y();

        // Category buttons
        int sideX = panelX + 5;
        int sideY = panelY + 49;
        int sideButtonW = sidebarW - 9;
        int categoryGap = tiny ? 23 : 26;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int cy = sideY + 19 + i * categoryGap;

            if (cy + 21 > panelY + panelH - 5) {
                break;
            }

            if (inside(
                    mx, my,
                    sideX, cy,
                    sideButtonW, 21
            )) {
                selectedCategory = CATEGORIES[i];
                return true;
            }
        }

        // Module buttons
        String[] modules = getModules();

        int columns = compact ? 1 : 2;
        int cardGap = tiny ? 4 : 8;
        int cardH = compact ? (tiny ? 35 : 42) : 58;
        int rowGap = compact ? 5 : 9;

        int cardW = Math.max(
            1,
            (contentW - cardGap * (columns - 1)) / columns
        );

        int startY = panelY + 84;

        for (int i = 0; i < modules.length; i++) {
            int column = i % columns;
            int row = i / columns;

            int cardX =
                contentX + column * (cardW + cardGap);

            int cardY =
                startY + row * (cardH + rowGap);

            if (inside(
                    mx, my,
                    cardX, cardY,
                    cardW, cardH
            )) {
                toggleModule(modules[i]);
                return true;
            }
        }

        // Quick actions
        if (compact) {
            int actionY = panelY + panelH - 28;
            int actionGap = 6;
            int actionW = Math.max(
                1, (contentW - actionGap) / 2
            );

            if (inside(
                    mx, my,
                    contentX, actionY,
                    actionW, 22
            )) {
                setAll(true);
                return true;
            }

            if (inside(
                    mx, my,
                    contentX + actionW + actionGap,
                    actionY,
                    actionW, 22
            )) {
                setAll(false);
                return true;
            }
        } else {
            int rightX = panelX + panelW - 112;

            if (inside(
                    mx, my,
                    rightX, panelY + 72,
                    100, 22
            )) {
                setAll(true);
                return true;
            }

            if (inside(
                    mx, my,
                    rightX, panelY + 99,
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
