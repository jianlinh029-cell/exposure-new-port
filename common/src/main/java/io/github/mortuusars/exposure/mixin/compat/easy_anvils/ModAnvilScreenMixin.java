package io.github.mortuusars.exposure.mixin.compat.easy_anvils;

import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModAnvilScreen.class)
public abstract class ModAnvilScreenMixin extends AnvilScreen {
    public ModAnvilScreenMixin(AnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    /**
     * Same mixin as {@link io.github.mortuusars.exposure.mixin.client.AnvilScreenMixin#onSlotChanged(AbstractContainerMenu, int, ItemStack, CallbackInfo)}
     * needs to be applied, as Easy Anvils overwrote whole method.
     */
    @Inject(method = "slotChanged", at = @At("HEAD"))
    private void onSlotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack, CallbackInfo ci) {
        if (dataSlotIndex == 0 &&
              Config.Server.SPEC.isLoaded()
              && Config.Server.INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT.get()) {
            int maxLength = stack.getItem() instanceof InterplanarProjectorItem
                  || stack.getItem() instanceof BrokenInterplanarProjectorItem ? 150 : 50;
            this.name.setMaxLength(maxLength);
        }
    }
}
