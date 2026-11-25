
package dev.testdon;

import dev.testdon.mixin.ClientPlayNetworkHandlerAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.glfw.GLFW;

public final class FreecamController {

    private static final int CHUNK_VIEW = 12;

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
    private static int lastLoadDistance = Integer.MIN_VALUE;

    private static long lastUpdate;

    private FreecamController() {}

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player == null) {
            active = false;
            lastUpdate = 0;
            resetChunkTracking();
            return;
        }

        if (!TestDonSettings.freecam) {
            if (active) {
                restoreChunkView(client);
            }

            active = false;
            lastUpdate = 0;
            resetChunkTracking();
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

            resetChunkTracking();
        }

        updateCameraChunkView(client);

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

        updateCameraChunkView(client);
    }

    private static int getServerChunkRadius(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return client.options.getClampedViewDistance();
        }

        return Math.max(
            2,
            ((ClientPlayNetworkHandlerAccessor) client.getNetworkHandler())
                .testdon$getChunkLoadDistance()
        );
    }

    private static void updateCameraChunkView(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        int playerChunkX = MathHelper.floor(playerX) >> 4;
        int playerChunkZ = MathHelper.floor(playerZ) >> 4;

        int cameraChunkX = MathHelper.floor(x) >> 4;
        int cameraChunkZ = MathHelper.floor(z) >> 4;

        int distance = Math.max(
            Math.abs(cameraChunkX - playerChunkX),
            Math.abs(cameraChunkZ - playerChunkZ)
        );

        int baseRadius = MathHelper.clamp(
            CHUNK_VIEW,
            2,
            32
        );

        int serverRadius = getServerChunkRadius(client);

        int requiredRadius = MathHelper.clamp(
            Math.max(baseRadius, distance / 2 + 3),
            2,
            32
        );

        int loadDistance = Math.max(
            serverRadius,
            requiredRadius
        );

        int centerX;
        int centerZ;

        if (distance > baseRadius) {
            centerX = playerChunkX
                + (cameraChunkX - playerChunkX) / 2;

            centerZ = playerChunkZ
                + (cameraChunkZ - playerChunkZ) / 2;
        } else {
            centerX = cameraChunkX;
            centerZ = cameraChunkZ;
        }

        int margin = loadDistance - serverRadius;

        centerX = MathHelper.clamp(
            centerX,
            playerChunkX - margin,
            playerChunkX + margin
        );

        centerZ = MathHelper.clamp(
            centerZ,
            playerChunkZ - margin,
            playerChunkZ + margin
        );

        if (centerX == lastCenterX
            && centerZ == lastCenterZ
            && loadDistance == lastLoadDistance) {
            return;
        }

        client.world.getChunkManager().setChunkMapCenter(
            centerX,
            centerZ
        );

        client.world.getChunkManager().updateLoadDistance(
            loadDistance
        );

        lastCenterX = centerX;
        lastCenterZ = centerZ;
        lastLoadDistance = loadDistance;
    }

    private static void restoreChunkView(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        int playerChunkX = MathHelper.floor(playerX) >> 4;
        int playerChunkZ = MathHelper.floor(playerZ) >> 4;

        int serverRadius = getServerChunkRadius(client);

        client.world.getChunkManager().setChunkMapCenter(
            playerChunkX,
            playerChunkZ
        );

        client.world.getChunkManager().updateLoadDistance(
            serverRadius
        );
    }

    private static void resetChunkTracking() {
        lastCenterX = Integer.MIN_VALUE;
        lastCenterZ = Integer.MIN_VALUE;
        lastLoadDistance = Integer.MIN_VALUE;
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
