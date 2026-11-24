
package dev.testdon.mixin;

import dev.testdon.FreecamController;

import net.minecraft.client.render.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(
        method = "update",
        at = @At("TAIL")
    )
    private void testdon$freecam(
            CallbackInfo ci) {

        if (!FreecamController.isActive()) {
            return;
        }

        setPos(
            FreecamController.getX(),
            FreecamController.getY(),
            FreecamController.getZ()
        );

        setRotation(
            FreecamController.getYaw(),
            FreecamController.getPitch()
        );
    }
}
