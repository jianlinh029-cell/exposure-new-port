package io.github.mortuusars.exposure.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.mortuusars.exposure.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PotionContents.class)
public abstract class PotionContentsMixin {
    @Shadow public abstract Optional<Holder<Potion>> potion();

    @ModifyReturnValue(method = "getColor()I", at = @At("RETURN"))
    private int onGetColor(int original) {
        if (!Config.Common.DIFFERENT_DEVELOPING_POTION_COLORS.get() || original != 0xFF385DC6) { // Default color
            return original;
        }

        if (potion().isPresent()) {
            if (potion().get().value().equals(Potions.MUNDANE.value())) {
                return 0xFF424D8F;
            }

            if (potion().get().value().equals(Potions.AWKWARD.value())) {
                return 0xFF653594;
            }

            if (potion().get().value().equals(Potions.THICK.value())) {
                return 0xFF3E7782;
            }
        }

        return original;
    }
}
