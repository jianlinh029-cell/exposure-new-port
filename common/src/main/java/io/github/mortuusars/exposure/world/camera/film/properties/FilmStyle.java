package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FilmStyle(Float sensitivity,
                        Float contrast,
                        Levels levels,
                        HSB hsb,
                        ColorBalance colorBalance,
                        Float noise) {

    public FilmStyle {
        Preconditions.checkArgument(sensitivity >= -10f && sensitivity <= 10f,
                "sensitivity must be >=-10 and <= 10. Got: " + sensitivity);
        Preconditions.checkArgument(contrast >= -1f && contrast <= 1f,
                "contrast must be >=-1 and <= 1. Got: " + contrast);
        Preconditions.checkArgument(noise >= 0f && noise <= 1f,
                "noise must be >=0 and <=1. Got: " + noise);
    }

    public static final Codec<FilmStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.floatRange(-10f, 10f).optionalFieldOf("sensitivity", 0f).forGetter(FilmStyle::sensitivity),
            Codecs.floatRange(-1f, 1f).optionalFieldOf("contrast", 0f).forGetter(FilmStyle::contrast),
            Levels.CODEC.optionalFieldOf("levels", Levels.EMPTY).forGetter(FilmStyle::levels),
            HSB.CODEC.optionalFieldOf("hsb", HSB.EMPTY).forGetter(FilmStyle::hsb),
            ColorBalance.CODEC.optionalFieldOf("color_balance", ColorBalance.EMPTY).forGetter(FilmStyle::colorBalance),
            Codecs.floatRange(0f, 1f).optionalFieldOf("noise", 0f).forGetter(FilmStyle::noise)
    ).apply(instance, FilmStyle::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilmStyle> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull FilmStyle decode(RegistryFriendlyByteBuf buffer) {
            return new FilmStyle(
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    Levels.STREAM_CODEC.decode(buffer),
                    HSB.STREAM_CODEC.decode(buffer),
                    ColorBalance.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, FilmStyle data) {
            ByteBufCodecs.FLOAT.encode(buffer, data.sensitivity);
            ByteBufCodecs.FLOAT.encode(buffer, data.contrast);
            Levels.STREAM_CODEC.encode(buffer, data.levels);
            HSB.STREAM_CODEC.encode(buffer, data.hsb);
            ColorBalance.STREAM_CODEC.encode(buffer, data.colorBalance);
            ByteBufCodecs.FLOAT.encode(buffer, data.noise);
        }
    };

    public static final FilmStyle EMPTY = new FilmStyle(
            0f,
            0f,
            Levels.EMPTY,
            HSB.EMPTY,
            ColorBalance.EMPTY,
            0f);

    public static FilmStyle create() {
        return EMPTY;
    }

    public FilmStyle withSensitivity(@Nullable Float sensitivity) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }

    public FilmStyle withContrast(@Nullable Float contrast) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }

    public FilmStyle withLevels(@Nullable Levels levels) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }

    public FilmStyle withHSB(@Nullable HSB hsb) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }

    public FilmStyle withColorBalance(@Nullable ColorBalance colorBalance) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }

    public FilmStyle withNoise(@Nullable Float noise) {
        return new FilmStyle(sensitivity, contrast, levels, hsb, colorBalance, noise);
    }
}