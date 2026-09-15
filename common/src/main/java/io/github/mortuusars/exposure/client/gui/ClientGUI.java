package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumViewScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ClientGUI {
    public static void openPhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        Minecrft.get().setScreen(new PhotographScreen(photographs));
    }

    public static void openPhotographsScreenFromItem(int item) {
        Minecrft.get().setScreen(new PhotographScreen(PhotographScreen.PhotographProvider.fromPhotographItem(item)));
    }

    public static void openAlbumViewScreen(ItemStack albumStack) {
        Minecrft.get().setScreen(new AlbumViewScreen(AlbumViewScreen.AlbumAccess.fromItem(albumStack)));
    }

    public static void addFilmRollDevelopingTooltip(ItemStack filmStack, Item.TooltipContext tooltipContext,
                                                    @NotNull Consumer<Component> components, @NotNull TooltipFlag isAdvanced) {
        // 1.21.11: the client no longer exposes per-type recipe iteration (RecipeManager#getAllRecipesFor is
        // replaced by RecipeAccess). The developing ingredients are therefore supplied from Exposure's own
        // built-in developing recipes, matched by the same film-roll tags, and rendered exactly like 1.21.1.
        addRecipeTooltip(components, getFilmDevelopingIngredients(filmStack),
                "item.exposure.film_roll.tooltip.details.develop");
    }

    public static void addPhotographCopyingTooltip(ItemStack photographStack, Item.TooltipContext tooltipContext,
                                                   @NotNull Consumer<Component> components, @NotNull TooltipFlag isAdvanced) {
        // Client recipe iteration is unavailable in 1.21.11; photograph copying keeps its hint-only tooltip.
        addRecipeTooltip(components, List.of(), "item.exposure.photograph.tooltip.details.copy");
    }

    private static List<ItemStack> getFilmDevelopingIngredients(ItemStack filmStack) {
        // The black-and-white tag contains both the regular and high-sensitivity rolls; both develop with water.
        if (filmStack.is(Exposure.Tags.Items.BLACK_AND_WHITE_FILM_ROLLS)) {
            return List.of(PotionContents.createItemStack(Items.POTION, Potions.WATER));
        } else if (filmStack.is(Exposure.Tags.Items.COLOR_FILM_ROLLS)) {
            return List.of(
                    PotionContents.createItemStack(Items.POTION, Potions.AWKWARD),
                    PotionContents.createItemStack(Items.POTION, Potions.THICK),
                    PotionContents.createItemStack(Items.POTION, Potions.MUNDANE));
        }
        return List.of();
    }

    private static void addRecipeTooltip(@NotNull Consumer<Component> components,
                                         @NotNull List<ItemStack> ingredients, String detailsKey) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        components.accept(Component.translatable("tooltip.exposure.hold_for_details"));
        if (!Minecrft.hasShiftDown()) {
            return;
        }

        components.accept(Component.empty());

        Style orange = Style.EMPTY.withColor(0xc7954b);
        Style yellow = Style.EMPTY.withColor(0xeeda78);

        components.accept(Component.translatable(detailsKey).withStyle(orange));

        for (ItemStack ingredient : ingredients) {
            components.accept(Component.literal("  ").append(ingredient.getHoverName().copy().withStyle(yellow)));
        }
    }
}
