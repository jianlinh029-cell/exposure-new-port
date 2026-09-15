package io.github.mortuusars.exposure.neoforge.mixin.create;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import io.github.mortuusars.exposure.world.item.FilmItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles developing Films with spouts.
 * Since it changes only FillingBySpout, other types of steps in sequenced recipe won't work, and remove data from film as usual.
 * But it's enough for my current needs. Chances that someone will want to change it to have pressing or sawing are slim anyway.
 */
@Mixin(FillingBySpout.class)
public class FillingBySpoutMixin {
    /**
     * Transfers components from input stack to results, but only if input and result are both {@link FilmItem}.
     */
    @ModifyVariable(method = "fillItem", at = @At(value = "INVOKE_ASSIGN",
            target = "Lcom/simibubi/create/content/fluids/transfer/FillingRecipe;rollResults(Lnet/minecraft/util/RandomSource;)Ljava/util/List;"), index = 7)
    private static List<ItemStack> onFillItem(List<ItemStack> results, Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        if (!(stack.getItem() instanceof FilmItem)) {
            return results;
        }

        results = new ArrayList<>(results);
        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (result.getItem() instanceof FilmItem) {
                // Store and reapply assembly stage, because it'll get lost when applyComponents is called:
                SequencedAssemblyRecipe.SequencedAssembly sequencedAssemblyComponent = result.get(AllDataComponents.SEQUENCED_ASSEMBLY);
                result.applyComponents(stack.getComponents());
                result.set(AllDataComponents.SEQUENCED_ASSEMBLY, sequencedAssemblyComponent);
                results.set(i, result);
                break;
            }
        }

        return results;
    }
}
