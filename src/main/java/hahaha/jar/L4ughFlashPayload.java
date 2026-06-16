package hahaha.jar;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record L4ughFlashPayload() implements CustomPacketPayload {
    public static final Type<L4ughFlashPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:l4ugh_flash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, L4ughFlashPayload> CODEC = StreamCodec.unit(new L4ughFlashPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
