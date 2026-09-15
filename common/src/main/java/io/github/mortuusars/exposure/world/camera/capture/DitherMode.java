package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum DitherMode implements StringRepresentable {
    DITHERED("dithered"),
    CLEAN("clean");

    private static final IntFunction<DitherMode> BY_ID =
            ByIdMap.continuous(DitherMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<DitherMode> CODEC = StringRepresentable.fromEnum(DitherMode::values);
    public static final StreamCodec<ByteBuf, DitherMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DitherMode::ordinal);

    private final String name;

    DitherMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public Component translate() {
        return Component.translatable("item.exposure.interplanar_projector.mode." + getSerializedName());
    }

    public DitherMode cycle() {
        return BY_ID.apply(ordinal() + 1);
    }
}
