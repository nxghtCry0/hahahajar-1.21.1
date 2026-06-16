package hahaha.jar.client.mixin;

import hahaha.jar.HahahaJarEventHandler;
import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static float lastYaw = 0.0f;
    private static float lastPitch = 0.0f;
    private static long lastSaccadeTime = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(net.minecraft.client.DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        float deltaYaw = Math.abs(currentYaw - lastYaw);
        float deltaPitch = Math.abs(currentPitch - lastPitch);
        lastYaw = currentYaw;
        lastPitch = currentPitch;

        if (HahahaJarEventHandler.isFreed() && HahahaJarClient.getClientThreatLevel() >= 50.0f) {
            if (deltaYaw > 20.0f || deltaPitch > 20.0f) {
                long now = System.currentTimeMillis();
                if (now - lastSaccadeTime > 45000) {
                    lastSaccadeTime = now;
                    HahahaJarClient.setSaccadeFlashFrames(2);
                }
            }
        }
    }
}
