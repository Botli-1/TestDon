
package dev.testdon;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.testdon.mixin.RenderLayerAccessor;
import dev.testdon.mixin.RenderPipelinesAccessor;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class ThroughWallRenderer {

    private ThroughWallRenderer() {}

    private static final List<BlockPos> CHEST_POSITIONS =
        new ArrayList<>();

    private static long lastChestScan = 0L;

    private static final RenderPipeline PIPELINE =
        RenderPipelinesAccessor.register(
            RenderPipeline.builder(
                RenderPipelinesAccessor.getPositionColorSnippet()
            )
            .withLocation(
                Identifier.of("testdon", "pipeline/through_walls")
            )
            .withVertexFormat(
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.QUADS
            )
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build()
        );

    private static final RenderLayer LAYER =
        RenderLayerAccessor.testdon$create(
            "testdon_through_walls",
            RenderSetup.builder(PIPELINE).translucent().build()
        );

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(
            ThroughWallRenderer::render
        );
    }

    private static void refreshChestPositions(
        MinecraftClient client,
        Vec3d camera,
        double maxDistance
    ) {
        long now = System.currentTimeMillis();

        if (now - lastChestScan < 1000L) {
            return;
        }

        lastChestScan = now;
        CHEST_POSITIONS.clear();

        int centerX = client.player.getChunkPos().x;
        int centerZ = client.player.getChunkPos().z;

        int radius = (int) Math.ceil(maxDistance / 16.0);

        var chunkManager = client.world.getChunkManager();

        for (int cx = centerX - radius;
             cx <= centerX + radius; cx++) {

            for (int cz = centerZ - radius;
                 cz <= centerZ + radius; cz++) {

                if (!chunkManager.isChunkLoaded(cx, cz)) {
                    continue;
                }

                WorldChunk chunk =
                    chunkManager.getWorldChunk(cx, cz);

                if (chunk == null) {
                    continue;
                }

                for (var blockEntity :
                        chunk.getBlockEntities().values()) {

                    var state = blockEntity.getCachedState();

                    if (!state.isOf(Blocks.CHEST)
                            && !state.isOf(Blocks.TRAPPED_CHEST)
                            && !state.isOf(Blocks.ENDER_CHEST)) {
                        continue;
                    }

                    BlockPos pos = blockEntity.getPos();

                    double dx = pos.getX() + 0.5 - camera.x;
                    double dy = pos.getY() + 0.5 - camera.y;
                    double dz = pos.getZ() + 0.5 - camera.z;

                    if (dx * dx + dy * dy + dz * dz
                            <= maxDistance * maxDistance) {
                        CHEST_POSITIONS.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            return;
        }

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();

        if (matrices == null || consumers == null) {
            return;
        }

        Vec3d camera =
            client.gameRenderer.getCamera().getCameraPos();

        double maxDistance =
            client.options.getViewDistance().getValue() * 16.0;

        refreshChestPositions(client, camera, maxDistance);

        VertexConsumer vertex = consumers.getBuffer(LAYER);

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        MatrixStack.Entry entry = matrices.peek();

        // Entity ESP
        for (Entity entity : client.world.getEntities()) {

            if (entity == client.player || entity.isRemoved()) {
                continue;
            }

            double dx = entity.getX() - camera.x;
            double dy = entity.getY() - camera.y;
            double dz = entity.getZ() - camera.z;

            if (dx * dx + dy * dy + dz * dz
                    > maxDistance * maxDistance) {
                continue;
            }

            var box = entity.getBoundingBox();

            drawBox(
                vertex, entry,
                (float) box.minX,
                (float) box.minY,
                (float) box.minZ,
                (float) box.maxX,
                (float) box.maxY,
                (float) box.maxZ,
                150
            );
        }

        // Chest ESP - no sound or interaction required
        for (BlockPos pos : CHEST_POSITIONS) {

            drawBox(
                vertex, entry,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 1,
                pos.getZ() + 1,
                125
            );
        }

        // Sound markers
        for (SoundMarkers.Marker marker : SoundMarkers.snapshot()) {

            float x = (float) marker.x();
            float y = (float) marker.y();
            float z = (float) marker.z();

            float size = 0.2f;

            drawBox(
                vertex, entry,
                x - size, y - size, z - size,
                x + size, y + size, z + size,
                90
            );
        }

        matrices.pop();
    }

    private static void drawBox(
        VertexConsumer v,
        MatrixStack.Entry m,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        int alpha
    ) {
        quad(v, m,
            x1,y1,z2, x2,y1,z2,
            x2,y2,z2, x1,y2,z2, alpha);

        quad(v, m,
            x2,y1,z1, x1,y1,z1,
            x1,y2,z1, x2,y2,z1, alpha);

        quad(v, m,
            x1,y1,z1, x1,y1,z2,
            x1,y2,z2, x1,y2,z1, alpha);

        quad(v, m,
            x2,y1,z2, x2,y1,z1,
            x2,y2,z1, x2,y2,z2, alpha);

        quad(v, m,
            x1,y2,z2, x2,y2,z2,
            x2,y2,z1, x1,y2,z1, alpha);

        quad(v, m,
            x1,y1,z1, x2,y1,z1,
            x2,y1,z2, x1,y1,z2, alpha);
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
