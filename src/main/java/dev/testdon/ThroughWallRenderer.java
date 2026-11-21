
package dev.testdon;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public final class ThroughWallRenderer {

    private ThroughWallRenderer() {}

    private static final RenderPipeline PIPELINE =
        RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(
                    Identifier.of("testdon", "pipeline/through_walls")
                )
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build()
        );

    private static final ByteBufferBuilder ALLOCATOR =
        new ByteBufferBuilder(4 * 1024 * 1024);

    private static final Vector4f COLOR_MODULATOR =
        new Vector4f(1f, 1f, 1f, 1f);

    private static final Vector3f MODEL_OFFSET =
        new Vector3f();

    private static final Matrix4f TEXTURE_MATRIX =
        new Matrix4f();

    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(
            ThroughWallRenderer::render
        );
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            return;
        }

        MatrixStack matrices = context.matrices();
        Vec3d camera =
            client.gameRenderer.getCamera().getCameraPos();

        double maxDistance =
            client.options.getViewDistance().getValue() * 16.0;

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Matrix4f matrix =
            matrices.peek().getPositionMatrix();

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || entity.isRemoved()) {
                continue;
            }

            double dx = entity.getX() - camera.x;
            double dy = entity.getY() - camera.y;
            double dz = entity.getZ() - camera.z;

            if (dx * dx + dy * dy + dz * dz >
                    maxDistance * maxDistance) {
                continue;
            }

            var box = entity.getBoundingBox();

            addBox(
                matrix,
                (float) box.minX,
                (float) box.minY,
                (float) box.minZ,
                (float) box.maxX,
                (float) box.maxY,
                (float) box.maxZ,
                1f, 0f, 0f, 0.35f
            );
        }

        for (SoundMarkers.Marker marker : SoundMarkers.snapshot()) {
            float x = (float) marker.x();
            float y = (float) marker.y();
            float z = (float) marker.z();

            float size = 0.2f;

            addBox(
                matrix,
                x - size, y - size, z - size,
                x + size, y + size, z + size,
                1f, 0f, 0f, 0.25f
            );
        }

        matrices.pop();

        if (buffer != null) {
            draw(client);
        }
    }

    private static void addBox(
        Matrix4f matrix,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float r, float g, float b, float a
    ) {
        if (buffer == null) {
            buffer = new BufferBuilder(
                ALLOCATOR,
                PIPELINE.getVertexFormatMode(),
                PIPELINE.getVertexFormat()
            );
        }

        face(matrix, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, r,g,b,a);
        face(matrix, x2,y1,z1, x1,y1,z1, x1,y2,z1, x2,y2,z1, r,g,b,a);
        face(matrix, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, r,g,b,a);
        face(matrix, x2,y1,z2, x2,y1,z1, x2,y2,z1, x2,y2,z2, r,g,b,a);
        face(matrix, x1,y2,z2, x2,y2,z2, x2,y2,z1, x1,y2,z1, r,g,b,a);
        face(matrix, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, r,g,b,a);
    }

    private static void face(
        Matrix4f matrix,
        float x1,float y1,float z1,
        float x2,float y2,float z2,
        float x3,float y3,float z3,
        float x4,float y4,float z4,
        float r,float g,float b,float a
    ) {
        buffer.addVertex(matrix, x1,y1,z1).setColor(r,g,b,a);
        buffer.addVertex(matrix, x2,y2,z2).setColor(r,g,b,a);
        buffer.addVertex(matrix, x3,y3,z3).setColor(r,g,b,a);
        buffer.addVertex(matrix, x4,y4,z4).setColor(r,g,b,a);
    }

    private static void draw(MinecraftClient client) {
        MeshData built = buffer.buildOrThrow();
        MeshData.DrawState state = built.drawState();
        VertexFormat format = state.format();

        GpuBuffer vertices = upload(state, format, built);

        drawBuffer(client, built, state, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(
        MeshData.DrawState state,
        VertexFormat format,
        MeshData built
    ) {
        int size = state.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < size) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(
                () -> "testdon through wall buffer",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                size
            );
        }

        CommandEncoder encoder =
            RenderSystem.getDevice().createCommandEncoder();

        try (
            GpuBuffer.MappedView mapped =
                encoder.mapBuffer(
                    vertexBuffer.currentBuffer().slice(
                        0, built.vertexBuffer().remaining()
                    ),
                    false,
                    true
                )
        ) {
            MemoryUtil.memCopy(
                built.vertexBuffer(),
                mapped.data()
            );
        }

        return vertexBuffer.currentBuffer();
    }

    private static void drawBuffer(
        MinecraftClient client,
        MeshData built,
        MeshData.DrawState state,
        GpuBuffer vertices,
        VertexFormat format
    ) {
        built.sortQuads(
            ALLOCATOR,
            RenderSystem.getProjectionType().vertexSorting()
        );

        GpuBuffer indices =
            PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(
                built.indexBuffer()
            );

        VertexFormat.IndexType indexType =
            built.drawState().indexType();

        GpuBufferSlice transforms =
            RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                COLOR_MODULATOR,
                MODEL_OFFSET,
                TEXTURE_MATRIX
            );

        try (
            RenderPass pass =
                RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                        () -> "testdon through wall rendering",
                        client.getFramebuffer().getColorAttachmentView(),
                        OptionalInt.empty(),
                        client.getFramebuffer().getDepthAttachmentView(),
                        OptionalDouble.empty()
                    )
        ) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(
                0,
                0,
                state.indexCount(),
                1
            );
        }

        built.close();
    }

    public static void close() {
        ALLOCATOR.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
