package dev.testdon.mixin;

import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderingDataPreparer.class)
public interface ChunkRenderingDataPreparerInvoker {

    @Invoker("getRenderedChunk")
    ChunkBuilder.BuiltChunk testdon$getRenderedChunk(
        long sectionPos,
        ChunkBuilder.BuiltChunk chunk,
        Direction direction
    );
}
