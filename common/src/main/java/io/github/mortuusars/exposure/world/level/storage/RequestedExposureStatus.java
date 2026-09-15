package io.github.mortuusars.exposure.world.level.storage;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum RequestedExposureStatus {
    NOT_REQUESTED,
    AWAITED,
    TIMED_OUT,
    INVALID_ID,
    NOT_FOUND,
    CANNOT_LOAD,
    SUCCESS,
    NEEDS_REFRESH;

    public static final StreamCodec<ByteBuf, RequestedExposureStatus> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> RequestedExposureStatus.values()[id], RequestedExposureStatus::ordinal
    );
}
