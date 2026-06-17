package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TooltipEventPayload(int duration) implements CustomPacketPayload {
    public static final Type<TooltipEventPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:tooltip_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TooltipEventPayload> CODEC = StreamCodec.composite(
        StreamCodec.of((buf, val) -> buf.writeInt(val), buf -> buf.readInt()),
        TooltipEventPayload::duration,
        TooltipEventPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
