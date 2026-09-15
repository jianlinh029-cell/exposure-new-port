package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SignedAlbumPage(ItemStack photograph, Component note) {
    public static final Codec<SignedAlbumPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(SignedAlbumPage::photograph),
            ComponentSerialization.CODEC.optionalFieldOf("note", Component.empty()).forGetter(SignedAlbumPage::note)
    ).apply(instance, SignedAlbumPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignedAlbumPage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, SignedAlbumPage::photograph,
            ComponentSerialization.STREAM_CODEC, SignedAlbumPage::note,
            SignedAlbumPage::new
    );

    public static final SignedAlbumPage EMPTY = new SignedAlbumPage(ItemStack.EMPTY, Component.empty());

    public boolean isEmpty() {
        return this.equals(EMPTY) || (photograph().isEmpty() && note().getString().isEmpty());
    }
}
