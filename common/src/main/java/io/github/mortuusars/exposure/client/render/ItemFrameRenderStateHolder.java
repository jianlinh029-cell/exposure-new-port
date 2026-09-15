package io.github.mortuusars.exposure.client.render;

import net.minecraft.world.item.ItemStack;

/**
 * Duck-interface implemented by the ItemFrameRenderState mixin so other mixins can reach the
 * frame's ItemStack without casting to a mixin class (which fails Mixin's type resolution
 * on 1.21.11).
 */
public interface ItemFrameRenderStateHolder {
    ItemStack exposure$getStack();
    void exposure$setStack(ItemStack stack);
}
