
package dev.testdon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
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
    private static double playerZ;

    private static int lastCenterX = Integer.MIN_VALUE;
    private static int lastCenterZ = Integer.MIN_VALUE;

    private static long lastUpdate;

    private FreecamController() {}

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player == null) {
            active = false;
            lastUpdate = 0;
            lastCenterX = Integer.MIN_VALUE;
            lastCenterZ = Integer.MIN_VALUE;
            return;
        }

        if (!TestDonSettings.freecam) {
            if (active) {
                updateChunkCenter(
                    client,
                    MathHelper.floor(player.getX()) >> 4,
                    MathHelper.floor(player.getZ()) >> 4
                );
            }

            active = false;
            lastUpdate = 0;
            lastCenterX = Integer.MIN_VALUE;
            lastCenterZ = Integer.MIN_VALUE;
            return;
        }

        if (!active) {
            active = true;

            playerX = player.getX();
            playerZ = player.getZ();

            x = player.getX();
            y = player.getY() + player.getStandingEyeHeight();
            z = player.getZ();

            yaw = player.getYaw();
            pitch = player.getPitch();

            lastUpdate = System.nanoTime();

            lastCenterX = Integer.MIN_VALUE;
            lastCenterZ = Integer.MIN_VALUE;
        }

        updateCameraChunkCenter(client);

        Vec3d velocity = player.getVelocity();
        double fallVelocity = Math.min(velocity.y, 0.0);

        player.setPosition(
            playerX,
            player.getY(),
            playerZ
        );

        player.setVelocity(0, fallVelocity, 0);

        player.setSneaking(false);
        client.options.sneakKey.setPressed(false);
        client.options.jumpKey.setPressed(false);
    }

    public static void updateCamera() {
        if (!active) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.currentScreen != null) {
            lastUpdate = System.nanoTime();
            return;
        }

        long now = System.nanoTime();

        double delta = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdate = now;

        delta = Math.min(delta, 0.05);

        long window = client.getWindow().getHandle();

        double speed = 8.0 * delta;

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

        double sideX = -Math.cos(radians);
        double sideZ = -Math.sin(radians);

        x += (forwardX * forward + sideX * sideways) * speed;
        z += (forwardZ * forward + sideZ * sideways) * speed;
        y += vertical * speed;

        updateCameraChunkCenter(client);
    }

    private static void updateCameraChunkCenter(MinecraftClient client) {
        int chunkX = MathHelper.floor(x) >> 4;
        int chunkZ = MathHelper.floor(z) >> 4;

        updateChunkCenter(client, chunkX, chunkZ);
    }

    private static void updateChunkCenter(
        MinecraftClient client,
        int chunkX,
        int chunkZ
    ) {
        if (client.world == null) return;

        if (chunkX == lastCenterX && chunkZ == lastCenterZ) {
            return;
        }

        client.world.getChunkManager().setChunkMapCenter(
            chunkX,
            chunkZ
        );

        lastCenterX = chunkX;
        lastCenterZ = chunkZ;
    }

    public static void rotateByMouse(double deltaX, double deltaY) {
        if (!active) return;

        float sensitivity = 0.15F;

        yaw += (float) deltaX * sensitivity;
        pitch += (float) deltaY * sensitivity;

        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
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
