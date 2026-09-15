package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AlbumPage(ItemStack photograph, String note) {
    public static final Codec<AlbumPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(AlbumPage::photograph),
            Codec.string(0, 512).optionalFieldOf("note", "").forGetter(AlbumPage::note)
    ).apply(instance, AlbumPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlbumPage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, AlbumPage::photograph,
            ByteBufCodecs.STRING_UTF8, AlbumPage::note,
            AlbumPage::new
    );

    public static final AlbumPage EMPTY = new AlbumPage(ItemStack.EMPTY, "");

    public boolean isEmpty() {
        return photograph().isEmpty() && note().isEmpty();
    }

    public AlbumPage setPhotograph(ItemStack stack) {
        return new AlbumPage(stack, note);
    }

    public AlbumPage setNote(String note) {
        return new AlbumPage(photograph, note);
    }

    public SignedAlbumPage convertToSigned() {
        return new SignedAlbumPage(photograph, Component.literal(note));
    }
}
