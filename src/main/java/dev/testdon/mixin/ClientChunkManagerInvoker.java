package dev.testdon.mixin;

import net.minecraft.client.world.ClientChunkManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientChunkManager.class)
public interface ClientChunkManagerInvoker {

    @Invoker("getActiveSections")
    LongOpenHashSet testdon$getActiveSections();
}
