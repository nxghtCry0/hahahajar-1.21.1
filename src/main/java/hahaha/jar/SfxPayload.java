package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SfxPayload() implements CustomPacketPayload {
    public static final Type<SfxPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:sfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SfxPayload> CODEC = StreamCodec.unit(new SfxPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
