package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class FilmDevelopingRecipe extends ComponentTransferringRecipe {
    public FilmDevelopingRecipe(CraftingBookCategory category, Ingredient filmIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(category, filmIngredient, ingredients, result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return (RecipeSerializer<? extends CustomRecipe>) Exposure.RecipeSerializers.FILM_DEVELOPING.get();
    }

    /**
     * Developing changes the item from a FilmRollItem into a DevelopedFilmItem.
     * Only the persistent film data is meaningful on the developed roll; copying
     * every component would also copy the high-sensitivity FilmStyle (and any
     * display/name components) from the source stack.
     */
    @Override
    public @NotNull ItemStack transferComponents(ItemStack source, ItemStack result) {
        copyComponent(source, result, Exposure.DataComponents.FILM_FRAME_COUNT);
        copyComponent(source, result, Exposure.DataComponents.FILM_FRAME_SIZE);
        copyComponent(source, result, Exposure.DataComponents.FILM_COLOR_PALETTE);
        copyComponent(source, result, Exposure.DataComponents.FILM_DITHER_MODE);
        copyComponent(source, result, Exposure.DataComponents.FILM_FRAMES);
        return result;
    }

    private static <T> void copyComponent(ItemStack source, ItemStack result, net.minecraft.core.component.DataComponentType<T> type) {
        T value = source.get(type);
        if (value != null) {
            result.set(type, value);
        }
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

        for (int i = 0; i < input.size(); ++i) {
            ItemStack item = input.getItem(i);
            if (item.getItem() instanceof PotionItem && remainingItems.get(i).isEmpty()) {
                remainingItems.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return remainingItems;
    }
}
