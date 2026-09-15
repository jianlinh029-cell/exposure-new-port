package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.render.ItemFrameRenderStateHolder;
import io.github.mortuusars.exposure.client.render.RenderQuadSink;
import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void exposure$onExtract(ItemFrame itemFrame, ItemFrameRenderState state, float partialTick, CallbackInfo ci) {
        ((ItemFrameRenderStateHolder) (Object) state).exposure$setStack(itemFrame.getItem());
    }

    @Redirect(method = "submit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void exposure$onItemSubmit(ItemStackRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                       int packedLight, int overlay, int outline, ItemFrameRenderState frameState) {
        ItemStack stack = ((ItemFrameRenderStateHolder) (Object) frameState).exposure$getStack();
        if (stack != null && !stack.isEmpty()
                && ClientEvents.renderItemFrameItem(stack, frameState.isGlowFrame, frameState.rotation,
                poseStack, RenderQuadSink.of(submitNodeCollector), packedLight)) {
            return; // Photograph rendered by exposure.
        }
        renderState.submit(poseStack, submitNodeCollector, packedLight, overlay, outline);
    }
}
