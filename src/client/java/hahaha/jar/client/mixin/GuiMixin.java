package hahaha.jar.client.mixin;

import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    private int heartRenderIndex = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        int frames = HahahaJarClient.getSaccadeFlashFrames();
        if (frames > 0) {
            HahahaJarClient.setSaccadeFlashFrames(frames - 1);
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            guiGraphics.blit(net.minecraft.resources.ResourceLocation.parse("hahahajar:textures/entity/saccade.png"), 0, 0, 0.0f, 0.0f, width, height, width, height);
        }
    }

    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void onRenderHeartsHead(CallbackInfo ci) {
        heartRenderIndex = 0;
    }

    @Inject(method = "renderHeart", at = @At("TAIL"))
    private void onRenderHeartTail(GuiGraphics guiGraphics, @org.spongepowered.asm.mixin.injection.Coerce Enum<?> heartType, int x, int y, boolean blink, boolean half, boolean hardcore, CallbackInfo ci) {
        if (heartType != null && !heartType.name().equals("CONTAINER")) {
            if (HahahaJarClient.isClientDecayActive()) {
                int bleedingCount = HahahaJarClient.getBleedingHeartCount();
                int startIndex = HahahaJarClient.getBleedingHeartIndexStart();
                if (heartRenderIndex >= startIndex && heartRenderIndex < startIndex + bleedingCount) {
                    guiGraphics.blit(net.minecraft.resources.ResourceLocation.parse("hahahajar:textures/entity/bleeding_heart.png"), x, y, 0.0f, 0.0f, 9, 9, 9, 9);
                }
            }
            heartRenderIndex++;
        }
    }

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"))
    private int onGetAirSupply(net.minecraft.world.entity.player.Player player) {
        if (HahahaJarClient.isFauxSuffocationActive()) {
            return HahahaJarClient.getSimulatedAir();
        }
        return player.getAirSupply();
    }
}
