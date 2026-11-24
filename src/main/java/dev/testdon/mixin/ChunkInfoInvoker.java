
package dev.testdon.mixin;

import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderingDataPreparer.ChunkInfo.class)
public interface ChunkInfoInvoker {

    @Invoker("<init>")
    static ChunkRenderingDataPreparer.ChunkInfo testdon$createNode(
            ChunkBuilder.BuiltChunk chunk,
            Direction direction,
            int distance) {
        throw new AssertionError();
    }
}
