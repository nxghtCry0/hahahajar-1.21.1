package hahaha.jar.client.mixin;

import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void onSetupFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickMinFog, float partialTick, CallbackInfo ci) {
        if (HahahaJarClient.isFogEventActive()) {
            RenderSystem.setShaderFogStart(0.0f);
            RenderSystem.setShaderFogEnd(20.0f);
        }
    }

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void onSetupColor(Camera camera, float partialTick, net.minecraft.client.multiplayer.ClientLevel level, int renderDistance, float bossColorModifier, CallbackInfo ci) {
        if (HahahaJarClient.hasLaughterJoined()) {
            RenderSystem.clearColor(0.01f, 0.01f, 0.01f, 1.0f);
            RenderSystem.setShaderFogColor(0.01f, 0.01f, 0.01f, 1.0f);
        }
    }
}
