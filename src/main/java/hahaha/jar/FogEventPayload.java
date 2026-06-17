package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FogEventPayload(int duration) implements CustomPacketPayload {
    public static final Type<FogEventPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:fog_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FogEventPayload> CODEC = StreamCodec.composite(
        StreamCodec.of((buf, val) -> buf.writeInt(val), buf -> buf.readInt()),
        FogEventPayload::duration,
        FogEventPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
