package hahaha.jar.client.mixin;

import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getLatency", at = @At("HEAD"), cancellable = true)
    private void onGetLatency(CallbackInfoReturnable<Integer> cir) {
        if (HahahaJarClient.isLagActive()) {
            cir.setReturnValue(HahahaJarClient.getLagPing());
        }
    }
}
