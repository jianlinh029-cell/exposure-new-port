package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum SelfTimer implements StringRepresentable {
    OFF("off", 0),
    ONE("one", 1),
    TWO("two", 2),
    FIVE("five", 5),
    TEN("ten", 10);

    public static final Codec<SelfTimer> CODEC = StringRepresentable.fromEnum(SelfTimer::values);
    public static final StreamCodec<ByteBuf, SelfTimer> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(SelfTimer::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), SelfTimer::ordinal);

    private final String name;
    private final int seconds;

    SelfTimer(String name, int seconds) {
        this.name = name;
        this.seconds = seconds;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getTicks() {
        return seconds * 20;
    }

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".self_timer." + name);
    }
}
