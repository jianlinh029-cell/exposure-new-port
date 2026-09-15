package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Type-safe wrapper around {@link CompoundTag}. <br>
 * {@link Type} is meant to be stored in a static final field in appropriate places.
 * <p>
 * Note: In Minecraft 1.21.11 {@link CompoundTag} is final, so this class holds a tag by composition
 * instead of extending it (as it did on 1.21.1).
 */
public class ExtraData {
    public static final Codec<ExtraData> CODEC = CompoundTag.CODEC.xmap(ExtraData::new, data -> data.getTag());
    public static final StreamCodec<ByteBuf, ExtraData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(ExtraData::new, data -> data.getTag());

    public static final ExtraData EMPTY = new ExtraData(new CompoundTag());

    private final CompoundTag tag;

    protected ExtraData(CompoundTag tag) {
        this.tag = tag;
    }

    public ExtraData() {
        this(new CompoundTag());
    }

    public CompoundTag getTag() {
        return tag;
    }

    public <T> Optional<T> get(@NotNull ExtraData.Type<T> type) {
        if (!contains(type.key())) return Optional.empty();
        try {
            return Optional.ofNullable(type.getter().apply(this, type.key()));
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public <T> T getOrDefault(@NotNull ExtraData.Type<T> type, T defaultValue) {
        if (!contains(type.key())) return defaultValue;
        try {
            @Nullable T value = type.getter().apply(this, type.key());
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return defaultValue;
        }
    }

    public <T> void put(Type<T> type, @NotNull T value) {
        Preconditions.checkNotNull(value, "value");
        type.setter().accept(this, type.key(), value);
    }

    public <T> void remove(Type<T> type) {
        remove(type.key());
    }

    // -- Delegated CompoundTag methods --

    public boolean contains(String key) {
        return tag.contains(key);
    }

    public boolean contains(String key, int tagType) {
        return tag.contains(key);
    }

    public String getString(String key) {
        return tag.getStringOr(key, "");
    }

    public ExtraData putString(String key, String value) {
        tag.putString(key, value);
        return this;
    }

    public boolean getBoolean(String key) {
        return tag.getBooleanOr(key, false);
    }

    public ExtraData putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
        return this;
    }

    public int getInt(String key) {
        return tag.getIntOr(key, 0);
    }

    public ExtraData putInt(String key, int value) {
        tag.putInt(key, value);
        return this;
    }

    public long getLong(String key) {
        return tag.getLongOr(key, 0L);
    }

    public ExtraData putLong(String key, long value) {
        tag.putLong(key, value);
        return this;
    }

    public float getFloat(String key) {
        return tag.getFloatOr(key, 0F);
    }

    public ExtraData putFloat(String key, float value) {
        tag.putFloat(key, value);
        return this;
    }

    public double getDouble(String key) {
        return tag.getDoubleOr(key, 0D);
    }

    public ExtraData putDouble(String key, double value) {
        tag.putDouble(key, value);
        return this;
    }

    public Tag get(String key) {
        return tag.get(key);
    }

    public ExtraData put(String key, Tag value) {
        tag.put(key, value);
        return this;
    }

    public ListTag getList(String key, int type) {
        return tag.getListOrEmpty(key);
    }

    public CompoundTag getCompound(String key) {
        return tag.getCompound(key).orElseGet(CompoundTag::new);
    }

    public Set<String> getAllKeys() {
        return tag.keySet();
    }

    public ExtraData remove(String key) {
        tag.remove(key);
        return this;
    }

    // --

    public @NotNull ExtraData copy() {
        return new ExtraData(tag.copy());
    }

    public @NotNull ExtraData merge(ExtraData other) {
        return merge(other.getTag());
    }

    public @NotNull ExtraData merge(CompoundTag other) {
        for (String key : other.keySet()) {
            Tag otherTag = other.get(key);
            assert otherTag != null;
            if (otherTag.getId() == 10) {
                if (this.contains(key, 10)) {
                    CompoundTag compoundTag = this.getCompound(key);
                    compoundTag.merge((CompoundTag) otherTag);
                } else {
                    this.put(key, otherTag.copy());
                }
            } else {
                this.put(key, otherTag.copy());
            }
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtraData other)) return false;
        return tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return tag.toString();
    }

    // --

    public record Type<T>(String key, BiFunction<ExtraData, String, @Nullable T> getter,
                          TriConsumer<ExtraData, String, T> setter) {
        public static Type<String> string(String key) {
            return new Type<>(key, ExtraData::getString, ExtraData::putString);
        }

        public static Type<Boolean> bool(String key) {
            return new Type<>(key, ExtraData::getBoolean, ExtraData::putBoolean);
        }

        public static Type<Integer> intVal(String key) {
            return new Type<>(key, ExtraData::getInt, ExtraData::putInt);
        }

        public static Type<Long> longVal(String key) {
            return new Type<>(key, ExtraData::getLong, ExtraData::putLong);
        }

        public static Type<Float> floatVal(String key) {
            return new Type<>(key, ExtraData::getFloat, ExtraData::putFloat);
        }

        public static Type<Double> doubleVal(String key) {
            return new Type<>(key, ExtraData::getDouble, ExtraData::putDouble);
        }

        public static <T extends StringRepresentable> Type<T> stringRepresentable(String key, Function<String, @Nullable T> deserializeFunction) {
            return new Type<>(key,
                    (data, k) -> deserializeFunction.apply(data.getString(k)),
                    (data, k, value) -> data.putString(k, value.getSerializedName()));
        }

        public static Type<Vec3> vec3(String key) {
            return new Type<>(key,
                    (data, k) -> {
                        ListTag pos = data.getList(k, DoubleTag.TAG_DOUBLE);
                        return new Vec3(pos.getDoubleOr(0, 0D), pos.getDoubleOr(1, 0D), pos.getDoubleOr(2, 0D));
                    },
                    (data, k, value) -> {
                        ListTag pos = new ListTag();
                        pos.add(DoubleTag.valueOf(value.x()));
                        pos.add(DoubleTag.valueOf(value.y()));
                        pos.add(DoubleTag.valueOf(value.z()));
                        data.put(k, pos);
                    });
        }

        public static Type<Identifier> resourceLocation(String key) {
            return new Type<>(key,
                    (data, k) -> Identifier.parse(data.getString(k)),
                    (data, k, value) -> data.putString(k, value.toString()));
        }

        public static <T> Type<List<T>> list(String key, int tagType, Function<Tag, T> extractFunc, Function<T, Tag> packFunc) {
            return new Type<>(key,
                    (data, k) -> data.getList(k, tagType).stream()
                            .map(extractFunc)
                            .toList(),
                    (data, k, value) -> {
                        ListTag list = new ListTag();
                        list.addAll(value.stream()
                                .map(packFunc)
                                .toList());
                        data.put(k, list);
                    });
        }

        public static <T> Type<List<T>> stringBasedList(String key, Function<String, T> extractFunc, Function<T, String> packFunc) {
            return list(key, Tag.TAG_STRING, tag -> extractFunc.apply(tag.asString().orElse("")), value -> StringTag.valueOf(packFunc.apply(value)));
        }
    }
}
