
package dev.testdon.mixin;

import dev.testdon.FreecamController;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityLookMixin {

    @Inject(
        method = "changeLookDirection(DD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void testdon$separateCameraLook(
            double cursorDeltaX,
            double cursorDeltaY,
            CallbackInfo ci) {

        if (!FreecamController.isActive()) {
            return;
        }

        if ((Object) this == MinecraftClient.getInstance().player) {
            FreecamController.rotateByMouse(
                cursorDeltaX,
                cursorDeltaY
            );

            ci.cancel();
        }
    }
}
