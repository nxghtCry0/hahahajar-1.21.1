package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThreadBleederLagPayload(boolean active, int ping) implements CustomPacketPayload {
    public static final Type<ThreadBleederLagPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:thread_bleeder_lag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThreadBleederLagPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBoolean(payload.active());
            buf.writeInt(payload.ping());
        },
        buf -> new ThreadBleederLagPayload(buf.readBoolean(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
