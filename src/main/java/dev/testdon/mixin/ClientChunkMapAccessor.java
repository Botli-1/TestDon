
package dev.testdon.mixin;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.world.ClientChunkManager$ClientChunkMap")
public interface ClientChunkMapAccessor {

    @Accessor("chunks")
    AtomicReferenceArray<WorldChunk> testdon$getChunks();
}
