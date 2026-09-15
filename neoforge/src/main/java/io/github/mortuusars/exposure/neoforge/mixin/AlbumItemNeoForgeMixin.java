package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AlbumItem.class, remap = false)
public abstract class AlbumItemNeoForgeMixin implements IItemExtension {
    @Shadow
    abstract boolean shouldPlayEquipAnimation(ItemStack oldStack, ItemStack newStack);

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return shouldPlayEquipAnimation(oldStack, newStack);
    }
}
