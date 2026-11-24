package dev.testdon.mixin;

import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.ChunkRenderingDataPreparer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderingDataPreparer.class)
public interface ChunkRenderingDataPreparerAccessor {

    @Accessor("builtChunkStorage")
    BuiltChunkStorage testdon$getBuiltChunkStorage();
}
