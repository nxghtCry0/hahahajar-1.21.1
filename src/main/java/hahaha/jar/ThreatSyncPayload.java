package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThreatSyncPayload(float threatLevel) implements CustomPacketPayload {
    public static final Type<ThreatSyncPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:threat_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThreatSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeFloat(payload.threatLevel()),
        buf -> new ThreatSyncPayload(buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
