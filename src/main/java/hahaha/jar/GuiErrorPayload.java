package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GuiErrorPayload() implements CustomPacketPayload {
    public static final Type<GuiErrorPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:gui_error"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiErrorPayload> CODEC = StreamCodec.unit(new GuiErrorPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
