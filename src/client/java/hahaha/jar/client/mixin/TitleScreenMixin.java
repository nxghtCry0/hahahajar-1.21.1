package hahaha.jar.client.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private static final ResourceLocation STATIC_BACKGROUND = ResourceLocation.parse("hahahajar:textures/gui/hahahapanorama.png");

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void onRenderPanorama(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        guiGraphics.blit(STATIC_BACKGROUND, 0, 0, 0, 0, screen.width, screen.height, screen.width, screen.height);
        ci.cancel();
    }
}
