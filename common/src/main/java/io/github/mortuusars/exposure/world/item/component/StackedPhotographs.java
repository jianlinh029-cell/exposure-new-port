package io.github.mortuusars.exposure.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public record StackedPhotographs(List<ItemStack> photographs) {
    public static final StackedPhotographs EMPTY = new StackedPhotographs(List.of());

    public static final Codec<StackedPhotographs> CODEC = ItemStack.CODEC.listOf()
          .xmap(StackedPhotographs::new, StackedPhotographs::photographs);
    public static final StreamCodec<RegistryFriendlyByteBuf, StackedPhotographs> STREAM_CODEC = ItemStack.STREAM_CODEC
          .apply(ByteBufCodecs.list())
          .map(StackedPhotographs::new, StackedPhotographs::photographs);

    // --

    public ItemStack getItemUnsafe(int index) {
        return photographs.get(index);
    }

    public List<ItemStack> photographsCopy() {
        return Lists.transform(photographs, ItemStack::copy);
    }

    public Stream<ItemStack> photographsCopyStream() {
        return photographs.stream().map(ItemStack::copy);
    }

    public List<ItemAndStack<PhotographItem>> photographsItemAndStacks() {
        return photographs.stream()
              .filter(stack -> stack.getItem() instanceof PhotographItem)
              .map(stack -> new ItemAndStack<PhotographItem>(stack))
              .toList();
    }

    public int size() {
        return photographs.size();
    }

    public boolean isEmpty() {
        return photographs.isEmpty();
    }

    // --

    public StackedPhotographs add(int index, ItemStack photograph) {
        ArrayList<ItemStack> list = new ArrayList<>(photographs);
        list.add(index, photograph);
        return new StackedPhotographs(list);
    }

    public StackedPhotographs remove(int index) {
        ArrayList<ItemStack> list = new ArrayList<>(photographs);
        list.remove(index);
        return new StackedPhotographs(list);
    }

    // --

    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof StackedPhotographs stackedPhotographs
              && ItemStack.listMatches(this.photographs, stackedPhotographs.photographs);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(photographs);
    }

    @Override
    public @NotNull String toString() {
        return "StackedPhotographs" + photographs;
    }
}
