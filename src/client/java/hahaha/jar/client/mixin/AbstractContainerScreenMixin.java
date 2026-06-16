package hahaha.jar.client.mixin;

import hahaha.jar.RegistryCorruptorWakeupPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    private int hoverTicks = 0;

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void onRenderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            boolean isCorruptor = stack.getItem() == Items.OBSIDIAN && stack.getHoverName().getString().contains("§c§kHAHAHAHA§r");
            boolean isDead = stack.getItem() == Items.COAL && stack.getHoverName().getString().contains("§4l a u g h");
            if (isCorruptor || isDead) {
                int x = slot.x;
                int y = slot.y;
                int color = (System.currentTimeMillis() / 150 % 2 == 0) ? 0x908B0000 : 0x90FF0000;
                guiGraphics.fill(x, y, x + 16, y + 16, color);
            }
        }
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void onContainerTick(CallbackInfo ci) {
        if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty()) {
            ItemStack stack = hoveredSlot.getItem();
            if (stack.getItem() == Items.OBSIDIAN && stack.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                hoverTicks++;
                if (hoverTicks >= 10) {
                    ClientPlayNetworking.send(new RegistryCorruptorWakeupPayload());
                    hoverTicks = -100;
                }
            } else {
                hoverTicks = 0;
            }
        } else {
            hoverTicks = 0;
        }
    }
}
