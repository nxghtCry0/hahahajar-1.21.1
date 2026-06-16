package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StopMusicPayload() implements CustomPacketPayload {
    public static final Type<StopMusicPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:stop_music"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopMusicPayload> CODEC = StreamCodec.unit(new StopMusicPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
