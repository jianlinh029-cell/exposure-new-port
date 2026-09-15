package io.github.mortuusars.exposure.client.render.model;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.21.11 replacement for the old 1.21.1 ItemOverride list on item/album.json (custom
 * {@code exposure:photos} property = photograph count / 100, thresholds 0.01/0.02/0.03).
 *
 * <p>1.21.11 does NOT let mods register a custom {@code range_dispatch}/{@code select} property
 * through any public API: {@code RangeSelectItemModelProperties}' id mapper is private and Fabric's
 * model-loading-api exposes no property registry. So we mirror vanilla {@code RangeSelectItemModel}
 * directly: an {@link ItemModel} that holds the four cover models and lets only the one matching
 * the photograph count write its layer.
 *
 * <p>The four child models are declared explicitly here (not parsed from a {@code composite}), each
 * with its own model id, and each is marked as a dependency in {@link #UNBAKED} so its texture is
 * guaranteed to enter the item atlas. The Fabric BeforeBakeItem hook replaces the album item model
 * wholesale with {@link #UNBAKED}; the on-disk items/album.json is only a fallback.
 */
public class AlbumItemModel implements ItemModel {

    // Cover model ids in fixed order: 0 photos -> album, 1 -> one, 2 -> two, 3+ -> three.
    public static final Identifier[] COVER_IDS = new Identifier[] {
            Exposure.resource("item/album"),
            Exposure.resource("item/album_one"),
            Exposure.resource("item/album_two"),
            Exposure.resource("item/album_three")
    };
    public static final int PART_ALBUM = 0;
    public static final int PART_ONE = 1;
    public static final int PART_TWO = 2;
    public static final int PART_THREE = 3;
    public static final int PART_COUNT = 4;

    private final ItemModel[] parts;
    // [AlbumDebug] per-part geometry description (quads/atlas/texture), computed at bake time via
    // compile-time public API so it survives remapping; printed for the selected part in update().
    private final String[] geoInfo;

    public AlbumItemModel(ItemModel[] parts, String[] geoInfo) {
        if (parts.length != PART_COUNT) {
            throw new IllegalArgumentException("Expected " + PART_COUNT + " album cover models, got " + parts.length);
        }
        this.parts = parts;
        this.geoInfo = geoInfo;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner itemOwner, int seed) {
        int count = stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) : 0;
        // [AlbumDebug] UNCONDITIONAL enter log: no throttle, no context filter. Proves whether the
        // baked AlbumItemModel is the one Minecraft calls when the album is rendered.
        Exposure.LOGGER.info("[AlbumDebug] UPDATE_ENTER ctx={} stackItem={} photographCount={}",
                displayContext, BuiltInRegistries.ITEM.getKey(stack.getItem()), count);
        int index = Math.max(PART_ALBUM, Math.min(count, PART_THREE));
        ItemModel selected = parts[index];
        Exposure.LOGGER.info("[AlbumDebug] UPDATE_SELECT index={} coverId={} selectedClass={} geometry=[{}]",
                index, COVER_IDS[index], selected.getClass().getName(),
                geoInfo != null && index < geoInfo.length ? geoInfo[index] : "n/a");

        // Identical to vanilla RangeSelectItemModel.update: identity element, then the one child.
        state.appendModelIdentityElement(this);
        selected.update(state, stack, resolver, displayContext, level, itemOwner, seed);
        Exposure.LOGGER.info("[AlbumDebug] UPDATE_AFTER_CHILD index={}", index);
    }

    /**
     * Explicit unbaked source: builds the four {@link BlockModelWrapper} children from the fixed
     * cover ids and marks every one as a dependency so all four textures are loaded.
     */
    public static final ItemModel.Unbaked UNBAKED = new ItemModel.Unbaked() {
        private final BlockModelWrapper.Unbaked[] children = new BlockModelWrapper.Unbaked[PART_COUNT];
        {
            for (int i = 0; i < PART_COUNT; i++) {
                children[i] = new BlockModelWrapper.Unbaked(COVER_IDS[i], List.<ItemTintSource>of());
            }
        }

        @Override
        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
            for (BlockModelWrapper.Unbaked child : children) {
                child.resolveDependencies(resolver);
            }
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            ItemModel[] baked = new ItemModel[PART_COUNT];
            String[] geo = new String[PART_COUNT];
            ModelBaker baker = bakingContext.blockModelBaker();
            for (int i = 0; i < PART_COUNT; i++) {
                baked[i] = children[i].bake(bakingContext);
                // Re-derive the generated item geometry through the same public API BlockModelWrapper
                // uses, so we can see the real quad count / atlas / texture.
                geo[i] = describeGeometry(baker, COVER_IDS[i]);
                Exposure.LOGGER.info("[AlbumDebug] cover[{}] id={} baked={} geometry={}",
                        i, COVER_IDS[i], baked[i].getClass().getSimpleName(), geo[i]);
            }
            return new AlbumItemModel(baked, geo);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            // Never re-serialized; the codec id is irrelevant after the BeforeBakeItem replacement.
            return BlockModelWrapper.Unbaked.MAP_CODEC;
        }
    };

    // [AlbumDebug] Re-bake the top geometry through the same public API BlockModelWrapper uses and
    // report quad count / atlas / texture. Compile-time types only, so it survives remapping.
    private static String describeGeometry(ModelBaker baker, Identifier modelId) {
        try {
            ResolvedModel resolved = baker.getModel(modelId);
            if (resolved == null) return "RESOLVED_MODEL_NULL";
            TextureSlots slots = resolved.getTopTextureSlots();
            List<BakedQuad> quads = resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY).getAll();
            if (quads.isEmpty()) return "QUADS=0 (generated geometry missing!)";
            TextureAtlasSprite sprite = quads.get(0).sprite();
            return "quads=" + quads.size()
                    + " atlas=" + sprite.atlasLocation()
                    + " tex=" + sprite.contents().name();
        } catch (Throwable t) {
            return "geometry-error: " + t;
        }
    }
}
