package io.github.mortuusars.exposure.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TranslatableError(String key, String code) {
    public static final TranslatableError GENERIC = new TranslatableError("error.exposure.generic", "ERR_GENERIC");
    public static final TranslatableError TIMED_OUT = new TranslatableError("error.exposure.timed_out", "ERR_TIMED_OUT");

    public static final Codec<TranslatableError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(TranslatableError::key),
            Codec.STRING.fieldOf("code").forGetter(TranslatableError::code)
    ).apply(instance, TranslatableError::new));

    public static final StreamCodec<ByteBuf, TranslatableError> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TranslatableError::key,
            ByteBufCodecs.STRING_UTF8, TranslatableError::code,
            TranslatableError::new
    );

    public MutableComponent technical() {
        return Component.translatable(key() + ".technical");
    }

    public MutableComponent casual() {
        return Component.translatable(key() + ".casual");
    }
}
