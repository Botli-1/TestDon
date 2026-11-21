
package dev.testdon;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class TestDonKeybinds {

    private TestDonKeybinds() {}

    public static final KeyBinding OPEN_MENU =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.testdon.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
            )
        );

    public static void initialize() {
        // Forces key registration during mod initialization.
    }
}
