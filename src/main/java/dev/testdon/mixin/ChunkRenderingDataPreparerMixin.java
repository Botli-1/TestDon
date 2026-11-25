
package dev.testdon.mixin;

import dev.testdon.TestDonSettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;

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
            it.unimi.dsi.fastutil.longs.LongOpenHashSet activeSections,
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

        if (client.world == null || client.player == null) {
            return;
        }

        BuiltChunkStorage storage =
            ((ChunkRenderingDataPreparerAccessor) this)
                .testdon$getBuiltChunkStorage();

        if (storage == null) {
            return;
        }

        int viewDistance = client.options.getClampedViewDistance();

        int playerChunkX = client.player.getBlockPos().getX() >> 4;
        int playerChunkZ = client.player.getBlockPos().getZ() >> 4;

        int cameraChunkX = camera.getBlockPos().getX() >> 4;
        int cameraChunkZ = camera.getBlockPos().getZ() >> 4;

        int cameraSectionY = ChunkSectionPos.getSectionCoord(
            camera.getBlockPos().getY()
        );

        int bottomSection = client.world.getBottomSectionCoord();
        int topSection = client.world.getTopSectionCoord() - 1;

        cameraSectionY = Math.max(
            bottomSection,
            Math.min(topSection, cameraSectionY)
        );

        for (int x = playerChunkX - viewDistance;
             x <= playerChunkX + viewDistance;
             x++) {

            for (int z = playerChunkZ - viewDistance;
                 z <= playerChunkZ + viewDistance;
                 z++) {

                if (Math.abs(x - cameraChunkX) > viewDistance
                    || Math.abs(z - cameraChunkZ) > viewDistance) {
                    continue;
                }

                if (client.world.getChunkManager().getChunk(
                        x,
                        z,
                        ChunkStatus.FULL,
                        false
                    ) == null) {
                    continue;
                }

                long sectionPos = ChunkSectionPos.asLong(
                    x,
                    cameraSectionY,
                    z
                );

                ChunkBuilder.BuiltChunk renderedChunk =
                    ((BuiltChunkStorageInvoker) storage)
                        .testdon$getRenderedChunk(sectionPos);

                if (renderedChunk != null) {
                    queue.add(
                        ChunkInfoInvoker.testdon$createNode(
                            renderedChunk,
                            null,
                            0
                        )
                    );
                }
            }
        }

        ci.cancel();
    }
}
