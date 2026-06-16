package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CorruptorSoundPayload(boolean active, float volume) implements CustomPacketPayload {
    public static final Type<CorruptorSoundPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:corruptor_sound"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CorruptorSoundPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBoolean(payload.active());
            buf.writeFloat(payload.volume());
        },
        buf -> new CorruptorSoundPayload(buf.readBoolean(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
