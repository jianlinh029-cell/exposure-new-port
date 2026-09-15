package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class CommonPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraDeactivateCommonPacket.TYPE, ActiveCameraDeactivateCommonPacket.STREAM_CODEC)
        );
    }
}
