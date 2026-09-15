package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;

import java.util.Optional;
import java.util.function.Function;

public record ShutterSpeedPredicate(Optional<ShutterSpeed> min, Optional<ShutterSpeed> max) {
    public static final Codec<ShutterSpeedPredicate> EXACT_CODEC = ShutterSpeed.CODEC.flatComapMap(ShutterSpeedPredicate::exact,
            predicate -> {
                if (predicate.min.isPresent() && predicate.max.isPresent() && predicate.min.get().equals(predicate.max.get())) {
                    return DataResult.success(predicate.min.get());
                }
                return DataResult.error(() -> predicate + " is not exact.");
            });

    public static final Codec<ShutterSpeedPredicate> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShutterSpeed.CODEC.optionalFieldOf("min").forGetter(ShutterSpeedPredicate::min),
            ShutterSpeed.CODEC.optionalFieldOf("max").forGetter(ShutterSpeedPredicate::max)
    ).apply(instance, ShutterSpeedPredicate::new));

    public static final Codec<ShutterSpeedPredicate> CODEC = Codec.either(FULL_CODEC, EXACT_CODEC)
            .xmap(either -> either.map(Function.identity(), Function.identity()),
                    predicate -> predicate.min.isPresent() && predicate.max.isPresent() && predicate.min.get().equals(predicate.max.get())
                            ? Either.right(predicate)
                            : Either.left(predicate));

    public ShutterSpeedPredicate {
        if (min.isPresent() && max.isPresent() && min.get().getDurationMilliseconds() > max.get().getDurationMilliseconds()) {
            throw new IllegalArgumentException("Min cannot be bigger than max. " + this);
        }
    }

    public static ShutterSpeedPredicate exact(ShutterSpeed shutterSpeed) {
        return new ShutterSpeedPredicate(Optional.ofNullable(shutterSpeed), Optional.ofNullable(shutterSpeed));
    }

    public static ShutterSpeedPredicate between(ShutterSpeed min, ShutterSpeed max) {
        return new ShutterSpeedPredicate(Optional.ofNullable(min), Optional.ofNullable(max));
    }

    public static ShutterSpeedPredicate atLeast(ShutterSpeed min) {
        return new ShutterSpeedPredicate(Optional.ofNullable(min), Optional.empty());
    }

    public static ShutterSpeedPredicate atMost(ShutterSpeed max) {
        return new ShutterSpeedPredicate(Optional.empty(), Optional.ofNullable(max));
    }

    public boolean matches(ShutterSpeed value) {
        if (this.min.isPresent() && this.min.get().getDurationMilliseconds() > value.getDurationMilliseconds()) {
            return false;
        }

        if (this.max.isPresent() && this.max.get().getDurationMilliseconds() < value.getDurationMilliseconds()) {
            return false;
        }

        return true;
    }

    public boolean matches(Optional<ShutterSpeed> value) {
        //noinspection OptionalIsPresent
        if (value.isEmpty()) return min.isEmpty() && max().isEmpty();
        return matches(value.get());
    }
}
