package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShutterSpeed implements StringRepresentable {
    public static final ShutterSpeed DEFAULT = new ShutterSpeed("1/60");

    public static final Codec<ShutterSpeed> CODEC = Codec.STRING.comapFlatMap(str -> {
        try {
            return DataResult.success(new ShutterSpeed(str));
        } catch (Exception e) {
            return DataResult.error(e::getMessage);
        }
    }, ShutterSpeed::getNotation);

    public static final StreamCodec<ByteBuf, ShutterSpeed> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ShutterSpeed::new, ShutterSpeed::getNotation);

    private final float valueMilliseconds;
    private final String notation;

    /**
     * Expected format is 1/60, 1/125, 2", 15", etc.
     */
    public ShutterSpeed(String notation) {
        notation = notation.trim();

        if (notation.endsWith("\"")) {
            this.valueMilliseconds = Integer.parseInt(notation.replace("\"", "")) * 1000;
            this.notation = notation;
        } else if (notation.contains("1/")) {
            this.valueMilliseconds = 1f / Integer.parseInt(notation.replace("1/", "")) * 1000;
            this.notation = notation;
        } else {
            throw new IllegalArgumentException("'%s' is not a valid shutter speed. Format should be 1/60, 2\", etc.".formatted(notation));
        }
    }

    public String getNotation() {
        return notation;
    }

    public float getDurationMilliseconds() {
        return valueMilliseconds;
    }

    /**
     * Should be at least 1 tick. Otherwise, it's probably not going to work correctly.
     */
    public int getDurationTicks() {
        return Math.max(1, (int) (valueMilliseconds / SharedConstants.MILLIS_PER_TICK));
    }

    public boolean shouldCauseTickingSound() {
        return valueMilliseconds > 999; // 1" and above
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(valueMilliseconds / relative.getDurationMilliseconds()) / Math.log(2));
    }

    public float getStops() {
        return getStopsDifference(DEFAULT);
    }

    public float getBrightness() {
        return 1f + getStops() * 0.2f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShutterSpeed that = (ShutterSpeed) o;
        return Float.compare(valueMilliseconds, that.valueMilliseconds) == 0 && Objects.equals(notation, that.notation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueMilliseconds, notation);
    }

    @Override
    public @NotNull String getSerializedName() {
        return getNotation();
    }
}
