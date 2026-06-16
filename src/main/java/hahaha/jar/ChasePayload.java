package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChasePayload() implements CustomPacketPayload {
    public static final Type<ChasePayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:chase"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChasePayload> CODEC = StreamCodec.unit(new ChasePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
