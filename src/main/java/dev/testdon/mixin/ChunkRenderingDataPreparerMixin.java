
package dev.testdon.mixin;

import dev.testdon.TestDonSettings;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkRenderingDataPreparer.class)
public abstract class ChunkRenderingDataPreparerMixin {

    @Unique
    private boolean testdon$wasFreecamActive = false;

    @Unique
    private LongOpenHashSet testdon$loadedColumns = new LongOpenHashSet();

    @Unique
    private int testdon$cameraSectionY;

    @Inject(
        method = "updateSectionOcclusionGraph",
        at = @At("HEAD")
    )
    private void testdon$refreshOnFreecamChange(
            boolean cullChunks,
            Camera camera,
            Frustum frustum,
            List<ChunkBuilder.BuiltChunk> builtChunks,
            LongOpenHashSet activeSections,
            CallbackInfo ci) {

        boolean enabled = TestDonSettings.freecam;

        if (enabled != testdon$wasFreecamActive) {
            testdon$wasFreecamActive = enabled;
            ((ChunkRenderingDataPreparer) (Object) this)
                .scheduleTerrainUpdate();
        }
    }

    @Inject(
        method = "updateTerrain",
        at = @At("HEAD")
    )
    private void testdon$collectLoadedColumns(
            boolean cullChunks,
            Camera camera,
            net.minecraft.util.math.Vec3d cameraPos,
            LongOpenHashSet activeSections,
            CallbackInfo ci) {

        if (!TestDonSettings.freecam) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        testdon$loadedColumns.clear();

        testdon$cameraSectionY =
            ChunkSectionPos.getSectionCoord(
                camera.getBlockPos().getY()
            );

        ClientChunkManager manager = client.world.getChunkManager();

        ClientChunkManagerAccessor managerAccessor =
            (ClientChunkManagerAccessor) (Object) manager;

        Object chunkMap = managerAccessor.testdon$getChunkMap();

        ClientChunkMapAccessor mapAccessor =
            (ClientChunkMapAccessor) chunkMap;

        AtomicReferenceArray<WorldChunk> chunks =
            mapAccessor.testdon$getChunks();

        for (int i = 0; i < chunks.length(); i++) {

            WorldChunk chunk = chunks.get(i);

            if (chunk == null) {
                continue;
            }

            ChunkPos pos = chunk.getPos();

            WorldChunk loadedChunk = manager.getChunk(
                pos.x,
                pos.z,
                ChunkStatus.FULL,
                false
            );

            if (loadedChunk == chunk) {
                testdon$loadedColumns.add(pos.toLong());
            }
        }
    }

    @Inject(
        method = "scheduleLater",
        at = @At("HEAD"),
        cancellable = true
    )
    private void testdon$loadVisibleSections(
            Camera camera,
            Queue<ChunkRenderingDataPreparer.ChunkInfo> queue,
            CallbackInfo ci) {

        if (!TestDonSettings.freecam) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BuiltChunkStorage storage =
            ((ChunkRenderingDataPreparerAccessor) this)
                .testdon$getBuiltChunkStorage();

        if (storage == null) {
            return;
        }

        LongIterator iterator = testdon$loadedColumns.iterator();

        while (iterator.hasNext()) {

            long column = iterator.nextLong();

            int chunkX = ChunkPos.getPackedX(column);
            int chunkZ = ChunkPos.getPackedZ(column);

            long sectionPos = ChunkSectionPos.asLong(
                chunkX,
                testdon$cameraSectionY,
                chunkZ
            );

            ChunkBuilder.BuiltChunk builtChunk =
                ((BuiltChunkStorageInvoker) storage)
                    .testdon$getRenderedChunk(sectionPos);

            if (builtChunk != null) {
                queue.add(
                    ChunkInfoInvoker.testdon$createNode(
                        builtChunk,
                        null,
                        0
                    )
                );
            }
        }

        ci.cancel();
    }
}
