package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Projection(String path, DitherMode mode) {
    public static final Codec<Projection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("path").forGetter(Projection::path),
            DitherMode.CODEC.optionalFieldOf("mode", DitherMode.DITHERED).forGetter(Projection::mode)
    ).apply(instance, Projection::new));

    public static final StreamCodec<FriendlyByteBuf, Projection> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Projection::path,
            DitherMode.STREAM_CODEC, Projection::mode,
            Projection::new
    );
}
