package hahaha.jar.client.mixin;

import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void onGetSkyColor(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (HahahaJarClient.hasLaughterJoined()) {
            cir.setReturnValue(new Vec3(0.01, 0.01, 0.01));
        }
    }
}
