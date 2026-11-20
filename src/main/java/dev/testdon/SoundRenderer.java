
package dev.testdon;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public final class SoundRenderer {

    private SoundRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(
            SoundRenderer::render
        );
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            return;
        }

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();

        Vec3d camera = client.gameRenderer.getCamera().getPos();

        VertexConsumer vertex = consumers.getBuffer(
            RenderLayers.debugFilledBox()
        );

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        MatrixStack.Entry entry = matrices.peek();

        for (SoundMarkers.Marker marker : SoundMarkers.snapshot()) {
            float x = (float) marker.x();
            float y = (float) marker.y();
            float z = (float) marker.z();

            float size = 0.2f;

            drawBox(
                vertex,
                entry,
                x - size, y - size, z - size,
                x + size, y + size, z + size
            );
        }

        matrices.pop();
    }

    private static void drawBox(
        VertexConsumer v,
        MatrixStack.Entry m,
        float x1, float y1, float z1,
        float x2, float y2, float z2
    ) {
        int alpha = 90;

        quad(v, m, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, alpha);
        quad(v, m, x2,y1,z1, x1,y1,z1, x1,y2,z1, x2,y2,z1, alpha);
        quad(v, m, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, alpha);
        quad(v, m, x2,y1,z2, x2,y1,z1, x2,y2,z1, x2,y2,z2, alpha);
        quad(v, m, x1,y2,z2, x2,y2,z2, x2,y2,z1, x1,y2,z1, alpha);
        quad(v, m, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, alpha);
    }

    private static void quad(
        VertexConsumer v,
        MatrixStack.Entry m,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3,
        float x4, float y4, float z4,
        int alpha
    ) {
        v.vertex(m, x1, y1, z1).color(255, 0, 0, alpha);
        v.vertex(m, x2, y2, z2).color(255, 0, 0, alpha);
        v.vertex(m, x3, y3, z3).color(255, 0, 0, alpha);
        v.vertex(m, x4, y4, z4).color(255, 0, 0, alpha);
    }
}
