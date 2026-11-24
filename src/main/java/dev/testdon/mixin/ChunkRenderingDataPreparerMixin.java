
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
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Queue;

@Mixin(ChunkRenderingDataPreparer.class)
public abstract class ChunkRenderingDataPreparerMixin {

    @Unique
    private boolean testdon$wasFreecamActive = false;

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

        LongOpenHashSet chunks =
            client.world.getChunkManager().getActiveSections();

        int cameraX = ChunkSectionPos.getSectionCoord(camera.getCameraPos().x);
int cameraY = ChunkSectionPos.getSectionCoord(camera.getCameraPos().y);
int cameraZ = ChunkSectionPos.getSectionCoord(camera.getCameraPos().z);
        int sectionY = MathHelper.clamp(
            cameraY,
            client.world.getBottomSectionCoord(),
            client.world.getTopSectionCoord()
        );

        int viewDistance = storage.getViewDistance();

        LongIterator iterator = chunks.iterator();

        while (iterator.hasNext()) {
            long chunkPos = iterator.nextLong();

            int chunkX = ChunkPos.getPackedX(chunkPos);
            int chunkZ = ChunkPos.getPackedZ(chunkPos);

            if (!ChunkFilter.isWithinDistanceExcludingEdge(
                    cameraX,
                    cameraZ,
                    viewDistance,
                    chunkX,
                    chunkZ)) {
                continue;
            }

            long sectionPos =
                ChunkSectionPos.asLong(chunkX, sectionY, chunkZ);

            ChunkBuilder.BuiltChunk chunk =
                ((BuiltChunkStorageInvoker) storage)
                    .testdon$getRenderedChunk(sectionPos);

            if (chunk != null) {
                queue.add(
                    ChunkInfoInvoker.testdon$createNode(
                        chunk,
                        null,
                        0
                    )
                );
            }
        }

        ci.cancel();
    }
}
