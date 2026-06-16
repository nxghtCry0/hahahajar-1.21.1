package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GaslightBlockPayload() implements CustomPacketPayload {
    public static final Type<GaslightBlockPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:gaslight_block"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GaslightBlockPayload> CODEC = StreamCodec.unit(new GaslightBlockPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
