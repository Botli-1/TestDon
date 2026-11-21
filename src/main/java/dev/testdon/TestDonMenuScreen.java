
package dev.testdon;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TestDonMenuScreen extends Screen {

    public TestDonMenuScreen() {
        super(Text.literal("Test Don"));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 90;
        int y = this.height / 2 - 78;

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Entity ESP: " + (TestDonSettings.entities ? "ON" : "OFF")),
                button -> {
                    TestDonSettings.entities = !TestDonSettings.entities;
                    button.setMessage(Text.literal(
                        "Entity ESP: " + (TestDonSettings.entities ? "ON" : "OFF")
                    ));
                }
            ).dimensions(x, y, 180, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Chest ESP: " + (TestDonSettings.chests ? "ON" : "OFF")),
                button -> {
                    TestDonSettings.chests = !TestDonSettings.chests;
                    button.setMessage(Text.literal(
                        "Chest ESP: " + (TestDonSettings.chests ? "ON" : "OFF")
                    ));
                }
            ).dimensions(x, y + 26, 180, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Sound ESP: " + (TestDonSettings.sounds ? "ON" : "OFF")),
                button -> {
                    TestDonSettings.sounds = !TestDonSettings.sounds;
                    button.setMessage(Text.literal(
                        "Sound ESP: " + (TestDonSettings.sounds ? "ON" : "OFF")
                    ));
                }
            ).dimensions(x, y + 52, 180, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Through Walls: " + (TestDonSettings.throughWalls ? "ON" : "OFF")),
                button -> {
                    TestDonSettings.throughWalls = !TestDonSettings.throughWalls;
                    button.setMessage(Text.literal(
                        "Through Walls: " + (TestDonSettings.throughWalls ? "ON" : "OFF")
                    ));
                }
            ).dimensions(x, y + 78, 180, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Chunk Finder: " + (TestDonSettings.chunkFinder ? "ON" : "OFF")),
                button -> {
                    TestDonSettings.chunkFinder = !TestDonSettings.chunkFinder;
                    button.setMessage(Text.literal(
                        "Chunk Finder: " + (TestDonSettings.chunkFinder ? "ON" : "OFF")
                    ));
                }
            ).dimensions(x, y + 104, 180, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close()
            ).dimensions(x, y + 136, 180, 20).build()
        );
    }
}
