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

        int teddyTicks = HahahaJarClient.getTeddyJumpscareTicks();
        if (teddyTicks > 0) {
            HahahaJarClient.setTeddyJumpscareTicks(teddyTicks - 1);
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            guiGraphics.fill(0, 0, width, height, 0xFF000000);
            float progress = (30.0f - teddyTicks) / 30.0f;
            float scale = 0.5f + progress * 2.5f;
            int shakeX = (int) ((Math.random() - 0.5) * 40.0 * (1.0f - progress));
            int shakeY = (int) ((Math.random() - 0.5) * 40.0 * (1.0f - progress));
            int centerX = width / 2 + shakeX;
            int centerY = height / 2 + shakeY;
            int baseSize = (int) (120 * scale);
            int earRadius = (int) (baseSize * 0.3);
            guiGraphics.fill(centerX - (int)(baseSize * 0.5), centerY - (int)(baseSize * 0.5), centerX - (int)(baseSize * 0.5) + earRadius * 2, centerY - (int)(baseSize * 0.5) + earRadius * 2, 0xFF3E2723);
            guiGraphics.fill(centerX + (int)(baseSize * 0.5) - earRadius * 2, centerY - (int)(baseSize * 0.5), centerX + (int)(baseSize * 0.5), centerY - (int)(baseSize * 0.5) + earRadius * 2, 0xFF3E2723);
            int innerEarRadius = (int) (earRadius * 0.6);
            guiGraphics.fill(centerX - (int)(baseSize * 0.5) + (earRadius - innerEarRadius), centerY - (int)(baseSize * 0.5) + (earRadius - innerEarRadius), centerX - (int)(baseSize * 0.5) + (earRadius + innerEarRadius), centerY - (int)(baseSize * 0.5) + (earRadius + innerEarRadius), 0xFF880E4F);
            guiGraphics.fill(centerX + (int)(baseSize * 0.5) - (earRadius + innerEarRadius), centerY - (int)(baseSize * 0.5) + (earRadius - innerEarRadius), centerX + (int)(baseSize * 0.5) - (earRadius - innerEarRadius), centerY - (int)(baseSize * 0.5) + (earRadius + innerEarRadius), 0xFF880E4F);
            int headRadius = (int) (baseSize * 0.6);
            guiGraphics.fill(centerX - headRadius, centerY - headRadius, centerX + headRadius, centerY + headRadius, 0xFF4E342E);
            int snoutW = (int) (baseSize * 0.35);
            int snoutH = (int) (baseSize * 0.25);
            guiGraphics.fill(centerX - snoutW, centerY, centerX + snoutW, centerY + snoutH, 0xFF5D4037);
            int noseW = (int) (baseSize * 0.12);
            int noseH = (int) (baseSize * 0.08);
            guiGraphics.fill(centerX - noseW, centerY, centerX + noseW, centerY + noseH, 0xFF000000);
            int eyeOffset = (int) (baseSize * 0.25);
            int eyeSize = (int) (baseSize * 0.15);
            guiGraphics.fill(centerX - eyeOffset - eyeSize/2, centerY - (int)(baseSize * 0.2) - eyeSize/2, centerX - eyeOffset + eyeSize/2, centerY - (int)(baseSize * 0.2) + eyeSize/2, 0xFFFF0000);
            guiGraphics.fill(centerX + eyeOffset - eyeSize/2, centerY - (int)(baseSize * 0.2) - eyeSize/2, centerX + eyeOffset + eyeSize/2, centerY - (int)(baseSize * 0.2) + eyeSize/2, 0xFFFF0000);
            int pupilSize = Math.max(2, (int)(eyeSize * 0.3));
            guiGraphics.fill(centerX - eyeOffset - pupilSize/2, centerY - (int)(baseSize * 0.2) - pupilSize/2, centerX - eyeOffset + pupilSize/2, centerY - (int)(baseSize * 0.2) + pupilSize/2, 0xFFFFFFFF);
            guiGraphics.fill(centerX + eyeOffset - pupilSize/2, centerY - (int)(baseSize * 0.2) - pupilSize/2, centerX + eyeOffset + pupilSize/2, centerY - (int)(baseSize * 0.2) + pupilSize/2, 0xFFFFFFFF);
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
