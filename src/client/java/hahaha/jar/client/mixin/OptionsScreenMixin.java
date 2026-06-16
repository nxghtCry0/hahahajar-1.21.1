package hahaha.jar.client.mixin;

import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        OptionsScreen screen = (OptionsScreen) (Object) this;
        for (GuiEventListener listener : screen.children()) {
            if (listener instanceof AbstractWidget widget) {
                if (widget.getMessage() != null && widget.getMessage().getString().equals(Component.translatable("options.sounds").getString())) {
                    widget.active = false;
                }
            }
        }
    }
}
