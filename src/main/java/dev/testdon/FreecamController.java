
package dev.testdon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.glfw.GLFW;

public final class FreecamController {

    private static boolean active = false;

    private static double x;
    private static double y;
    private static double z;

    private static float yaw;
    private static float pitch;

    private static double playerX;
    private static double playerY;
    private static double playerZ;

    private FreecamController() {}

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player == null || client.getWindow() == null) {
            active = false;
            return;
        }

        if (!TestDonSettings.freecam) {
            active = false;
            return;
        }

        if (!active) {
            active = true;

            playerX = player.getX();
            playerY = player.getY();
            playerZ = player.getZ();

            x = player.getX();
            y = player.getY() + player.getStandingEyeHeight();
            z = player.getZ();

            yaw = player.getYaw();
            pitch = player.getPitch();
        }

        player.setPosition(playerX, playerY, playerZ);
        player.setVelocity(Vec3d.ZERO);

        if (client.currentScreen != null) {
            return;
        }

        yaw = player.getYaw();
        pitch = player.getPitch();

        long window = client.getWindow().getHandle();

        double speed = 0.45;

        double forward = 0;
        double sideways = 0;
        double vertical = 0;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS)
            forward++;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS)
            forward--;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS)
            sideways++;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS)
            sideways--;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS)
            vertical++;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
            vertical--;

        double radians = Math.toRadians(yaw);

        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);

        double sideX = Math.cos(radians);
        double sideZ = Math.sin(radians);

        x += (forwardX * forward + sideX * sideways) * speed;
        z += (forwardZ * forward + sideZ * sideways) * speed;
        y += vertical * speed;
    }

    public static boolean isActive() {
        return active;
    }

    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }

    public static double getZ() {
        return z;
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch() {
        return pitch;
    }
}
