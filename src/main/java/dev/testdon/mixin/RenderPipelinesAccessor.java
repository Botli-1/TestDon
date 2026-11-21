
package dev.testdon.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {

    @Accessor("POSITION_COLOR_SNIPPET")
    static RenderPipeline.Snippet getPositionColorSnippet() {
        throw new AssertionError();
    }
}
