package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentTransferringShapelessExtension implements ICraftingCategoryExtension<ComponentTransferringRecipe> {
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<ComponentTransferringRecipe> recipeHolder) {
        ComponentTransferringRecipe recipe = recipeHolder.value();
        List<SlotDisplay> displays = recipe.placementInfo().ingredients().stream()
                .map(ingredient -> (SlotDisplay) new SlotDisplay.Composite(ingredient.items().map(itemHolder -> (SlotDisplay) new ItemSlotDisplay(itemHolder)).toList()))
                .collect(Collectors.toList());
        displays.add(new SlotDisplay.Composite(recipe.getSourceIngredient().items().map(itemHolder -> (SlotDisplay) new ItemSlotDisplay(itemHolder)).toList()));
        return displays;
    }

    @Override
    public void setRecipe(RecipeHolder<ComponentTransferringRecipe> recipeHolder, IRecipeLayoutBuilder builder,
                          ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ComponentTransferringRecipe recipe = recipeHolder.value();
        ItemStack resultItem = recipe.getResult();
        int width = getWidth(recipeHolder);
        int height = getHeight(recipeHolder);
        craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
        craftingGridHelper.createAndSetInputs(builder, List.of(List.of(resultItem)), width, height);
    }
}