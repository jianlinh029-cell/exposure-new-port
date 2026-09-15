package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.render.ItemFrameRenderStateHolder;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFrameRenderState.class)
public abstract class ItemFrameRenderStateMixin implements ItemFrameRenderStateHolder {
    @Unique
    private ItemStack exposure$stack = ItemStack.EMPTY;

    @Override
    public ItemStack exposure$getStack() {
        return this.exposure$stack;
    }

    @Override
    public void exposure$setStack(ItemStack stack) {
        this.exposure$stack = stack;
    }
}
