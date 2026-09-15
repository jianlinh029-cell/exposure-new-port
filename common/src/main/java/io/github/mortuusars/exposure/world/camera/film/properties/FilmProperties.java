package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FilmProperties(ExposureType type,
                             Optional<Integer> size,
                             ResourceKey<ColorPalette> colorPalette,
                             DitherMode ditherMode,
                             FilmStyle style) {
    public FilmProperties {
        size.ifPresent(s -> Preconditions.checkArgument(s > 0 && s <= 2048,
                "size must be 1-2048: " + size));
    }

    public static final Codec<FilmProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(FilmProperties::type),
            ExtraCodecs.intRange(0, 2048).optionalFieldOf("frame_size").forGetter(FilmProperties::size),
            ResourceKey.codec(Exposure.Registries.COLOR_PALETTE).optionalFieldOf("color_palette", ColorPalettes.DEFAULT).forGetter(FilmProperties::colorPalette),
            DitherMode.CODEC.optionalFieldOf("dither_mode", DitherMode.DITHERED).forGetter(FilmProperties::ditherMode),
            FilmStyle.CODEC.optionalFieldOf("style", FilmStyle.EMPTY).forGetter(FilmProperties::style)
    ).apply(instance, FilmProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilmProperties> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf buffer, FilmProperties data) {
            ExposureType.STREAM_CODEC.encode(buffer, data.type());
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.size());
            ResourceKey.streamCodec(Exposure.Registries.COLOR_PALETTE).encode(buffer, data.colorPalette());
            DitherMode.STREAM_CODEC.encode(buffer, data.ditherMode());
            FilmStyle.STREAM_CODEC.encode(buffer, data.style());
        }

        public @NotNull FilmProperties decode(RegistryFriendlyByteBuf buffer) {
            return new FilmProperties(
                    ExposureType.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
                    ResourceKey.streamCodec(Exposure.Registries.COLOR_PALETTE).decode(buffer),
                    DitherMode.STREAM_CODEC.decode(buffer),
                    FilmStyle.STREAM_CODEC.decode(buffer));
        }
    };

    public static final FilmProperties EMPTY = new FilmProperties(
            ExposureType.COLOR,
            Optional.empty(),
            ColorPalettes.DEFAULT,
            DitherMode.DITHERED,
            FilmStyle.EMPTY);

    public FilmProperties withType(ExposureType type) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withSize(@Nullable Integer size) {
        return new FilmProperties(type, Optional.ofNullable(size), colorPalette, ditherMode, style);
    }

    public FilmProperties withColorPalette(@NotNull ResourceKey<ColorPalette> colorPalette) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withDitherMode(DitherMode ditherMode) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withStyle(@NotNull FilmStyle style) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    // --

    public int getSize() {
        return size.orElse(Config.Server.DEFAULT_FRAME_SIZE.get());
    }

    public Holder<ColorPalette> getColorPalette(RegistryAccess access) {
        return ColorPalettes.get(access, colorPalette);
    }
}