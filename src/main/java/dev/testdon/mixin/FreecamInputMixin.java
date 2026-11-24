
package dev.testdon.mixin;

import dev.testdon.FreecamController;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class FreecamInputMixin {

    @Shadow
    public PlayerInput playerInput;

    @Shadow
    protected Vec2f movementVector;

    @Inject(method = "tick", at = @At("TAIL"))
    private void testdon$stopPlayerInput(CallbackInfo ci) {
        if (!FreecamController.isActive()) {
            return;
        }

        playerInput = PlayerInput.DEFAULT;
        movementVector = new Vec2f(0.0F, 0.0F);
    }
}
