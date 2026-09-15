package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public abstract class LecternBlockEntityMixin {
    @Shadow public abstract ItemStack getBook();

    @Inject(method = "hasBook", at = @At("HEAD"), cancellable = true)
    private void onHasBook(CallbackInfoReturnable<Boolean> cir) {
        if (getBook().getItem() instanceof AlbumItem || getBook().getItem() instanceof SignedAlbumItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private static void onGetPageCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof AlbumItem albumItem) {
            cir.setReturnValue(albumItem.getContent(stack).pages().size());
        }
        else if (stack.getItem() instanceof SignedAlbumItem signedAlbumItem) {
            cir.setReturnValue(signedAlbumItem.getContent(stack).pages().size());
        }
    }
}
