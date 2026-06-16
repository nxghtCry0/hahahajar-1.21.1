package hahaha.jar.client.mixin;

import hahaha.jar.HahahaJarEventHandler;
import hahaha.jar.client.HahahaJarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin {
    @Shadow @Final private List<?> subtitles;
    private static long lastFakeSubtitleTime = 0;
    private static long nextInterval = 15000;
    private static java.lang.reflect.Constructor<?> subtitleConstructor = null;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(net.minecraft.client.gui.GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (HahahaJarEventHandler.isFreed() && HahahaJarClient.getClientThreatLevel() >= 70.0f) {
            long now = System.currentTimeMillis();
            if (now - lastFakeSubtitleTime > nextInterval) {
                lastFakeSubtitleTime = now;
                nextInterval = 15000 + mc.player.getRandom().nextInt(15000);
                String[] phrases = { "Footsteps approaching...", "Unintelligible whispers...", "Rustling nearby", "Breathing behind you", "Scraping sound", "Door creaking" };
                Component text = Component.literal(phrases[mc.player.getRandom().nextInt(phrases.length)]);
                Vec3 playerPos = mc.player.position();
                Vec3 viewVector = mc.player.getViewVector(1.0f);
                Vec3 soundPos = playerPos.subtract(viewVector.scale(5.0));
                try {
                    if (subtitleConstructor == null) {
                        Class<?> subtitleClass = Class.forName("net.minecraft.client.gui.components.SubtitleOverlay$Subtitle");
                        subtitleConstructor = subtitleClass.getDeclaredConstructor(Component.class, float.class, Vec3.class);
                        subtitleConstructor.setAccessible(true);
                    }
                    Object sub = subtitleConstructor.newInstance(text, 16.0f, soundPos);
                    ((List<Object>) subtitles).add(sub);
                } catch (Exception e) {
                }
            }
        }
    }
}
