
package dev.testdon.mixin;

import dev.testdon.TestDonSettings;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.chunk.ChunkBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ChunkRenderingDataPreparer.class)
public abstract class ChunkRenderingDataPreparerMixin {

    @Unique
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

        LongOpenHashSet sections =
            client.world.getChunkManager().getActiveSections();

        LongIterator iterator = sections.iterator();

        while (iterator.hasNext()) {
            long sectionPos = iterator.nextLong();

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
