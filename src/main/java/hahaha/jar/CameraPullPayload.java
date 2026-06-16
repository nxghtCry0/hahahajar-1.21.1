package hahaha.jar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CameraPullPayload() implements CustomPacketPayload {
    public static final Type<CameraPullPayload> TYPE = new Type<>(ResourceLocation.parse("hahahajar:camera_pull"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CameraPullPayload> CODEC = StreamCodec.unit(new CameraPullPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
