
package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record SignedAlbumContent(String title, String author, List<SignedAlbumPage> pages) {
    public static final Codec<SignedAlbumContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").forGetter(SignedAlbumContent::title),
            Codec.STRING.fieldOf("author").forGetter(SignedAlbumContent::author),
            SignedAlbumPage.CODEC.sizeLimitedListOf(AlbumContent.MAX_PAGES).fieldOf("pages").forGetter(SignedAlbumContent::pages)
    ).apply(instance, SignedAlbumContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignedAlbumContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SignedAlbumContent::title,
            ByteBufCodecs.STRING_UTF8, SignedAlbumContent::author,
            SignedAlbumPage.STREAM_CODEC.apply(ByteBufCodecs.list(AlbumContent.MAX_PAGES)), SignedAlbumContent::pages,
            SignedAlbumContent::new
    );

    public static final SignedAlbumContent EMPTY = new SignedAlbumContent("", "", Collections.emptyList());

    public SignedAlbumContent {
        Preconditions.checkArgument(pages.size() <= AlbumContent.MAX_PAGES,
                "Too many pages for signed album. Max is " + AlbumContent.MAX_PAGES);
    }
}
