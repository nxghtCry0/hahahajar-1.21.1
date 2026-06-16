package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RegistryCorruptorWakeupPayload() implements CustomPacketPayload {
    public static final Type<RegistryCorruptorWakeupPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:registry_corruptor_wakeup"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryCorruptorWakeupPayload> CODEC = StreamCodec.unit(new RegistryCorruptorWakeupPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
