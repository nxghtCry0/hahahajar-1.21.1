package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TeddySnapPayload() implements CustomPacketPayload {
    public static final Type<TeddySnapPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:teddy_snap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeddySnapPayload> CODEC = StreamCodec.unit(new TeddySnapPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
