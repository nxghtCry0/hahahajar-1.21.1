package hahaha.jar.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRenderLogo4(GuiGraphics guiGraphics, int screenWidth, float alpha, int yOffset, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRenderLogo3(GuiGraphics guiGraphics, int screenWidth, float alpha, CallbackInfo ci) {
        ci.cancel();
    }
}
