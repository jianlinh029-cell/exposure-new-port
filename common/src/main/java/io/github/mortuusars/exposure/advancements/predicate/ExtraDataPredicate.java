package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * We cannot use {@link net.minecraft.advancements.criterion.NbtPredicate} as it checks against CompoundTag.class.
 * This is basically a copy of its functionality to make ExtraData work.
 */
public record ExtraDataPredicate(CompoundTag data) {
    public static final Codec<ExtraDataPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(
            ExtraDataPredicate::new,
            predicate -> predicate.data);
    public static final StreamCodec<ByteBuf, ExtraDataPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
            ExtraDataPredicate::new,
            predicate -> predicate.data);

    public boolean matches(@Nullable Tag tag) {
        return tag != null && compareNbt(tag);
    }

    private boolean compareNbt(@Nullable Tag other) {
        if (data == other) {
            return true;
        } else if (data == null) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof CompoundTag compoundTag2) {
            if (compoundTag2.size() < data.size()) {
                return false;
            } else {
                for (String key : data.keySet()) {
                    Tag tag2 = data.get(key);
                    if (!NbtUtils.compareNbt(tag2, compoundTag2.get(key), true)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return data.equals(other);
        }
    }
}
