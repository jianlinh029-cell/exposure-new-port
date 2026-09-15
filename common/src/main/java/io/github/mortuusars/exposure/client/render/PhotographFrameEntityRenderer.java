package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class PhotographFrameEntityRenderer<T extends PhotographFrameEntity> extends EntityRenderer<T, PhotographFrameRenderState> {
    private final ItemModelResolver itemModelResolver;

    public PhotographFrameEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public @NotNull PhotographFrameRenderState createRenderState() {
        return new PhotographFrameRenderState();
    }

    @Override
    public void extractRenderState(T entity, PhotographFrameRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
            Minecraft.getInstance().crosshairPickEntity = entity;
        }

        state.direction = entity.getDirection();
        state.yRot = entity.getYRot(partialTick);
        state.xRot = entity.getXRot();
        state.size = entity.getSize();
        state.itemRotation = entity.getItemRotation();
        state.isFrameInvisible = entity.isFrameInvisible();
        state.isGlowing = entity.isGlowing();
        state.photographBrightness = getPhotographBrightness(entity);
        state.stack = entity.getItem();

        if (!state.stack.isEmpty()) {
            itemModelResolver.updateForNonLiving(state.item, state.stack, ItemDisplayContext.FIXED, entity);
        }
    }

    @Override
    public void submit(PhotographFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);

        Direction direction = state.direction;

        poseStack.pushPose();

        // thickness of the frame is 1px (0.5 - (1/16 * 0.5)) (0.5 is because we are offsetting from the center)
        // stripped frame is thin, so 1/16 becomes 0.15/16 (thickness of the backplate)
        double hangOffset = 0.46875;
        poseStack.translate(direction.getStepX() * hangOffset, direction.getStepY() * hangOffset, direction.getStepZ() * hangOffset);

        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        ItemStack item = state.stack;
        if (!item.isEmpty()) {
            boolean photographRendered = renderPhotograph(state, poseStack, submitNodeCollector);

            if (!photographRendered) {
                poseStack.pushPose();
                float scale = 0.65f + state.size * 0.5f;
                poseStack.translate(0, 0, 0.46875);
                poseStack.scale(scale, scale, scale * 0.75f);
                poseStack.mulPose(Axis.ZP.rotationDegrees((state.itemRotation * 360.0F / 4.0F)));
                state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }

        if (!state.isFrameInvisible) {
            renderFrame(state, poseStack, submitNodeCollector);
        }

        poseStack.popPose();
    }

    protected void renderFrame(PhotographFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        Identifier modelLocation = getModelLocation(state.size);
        BlockStateModel model = PlatformHelperClient.getModel(modelLocation);
        submitNodeCollector.submitBlockModel(poseStack, getFrameRenderType(),
                model, 1.0f, 1.0f, 1.0f, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    protected net.minecraft.client.renderer.rendertype.RenderType getFrameRenderType() {
        return RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);
    }

    protected boolean renderPhotograph(PhotographFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        ItemStack item = state.stack;
        int size = state.size;
        int packedLight = state.isGlowing ? LightTexture.FULL_BRIGHT : state.lightCoords;

        poseStack.pushPose();

        boolean frameInvisible = state.isFrameInvisible;

        float frameBorderOffset = frameInvisible ? 0f : 0.125f; // (2px / 16px = 0.125)
        float offsetFromCenter = frameInvisible ? 0.497f : 0.48f;
        offsetFromCenter -= Config.Client.PHOTOGRAPH_FRAME_IMAGE_OFFSET.get();
        float desiredSize = size + 1 - frameBorderOffset * 2;

        poseStack.mulPose(Axis.ZP.rotationDegrees((state.itemRotation * 360.0F / 4.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(-0.5 * (size + 1) + frameBorderOffset, -0.5 * (size + 1) + frameBorderOffset, offsetFromCenter);
        poseStack.scale(desiredSize, desiredSize, 1);

        int brightness = state.isGlowing ? 255 : state.photographBrightness;

        boolean photographRendered = false;

        if (Config.Client.PIXEL_PERFECT_PHOTOGRAPH_FRAME.get()) {
            if (item.getItem() instanceof PhotographItem photographItem) {
                PhotographStyle style = PhotographStyle.of(item);
                Frame frame = photographItem.getFrame(item);

                RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));

                int pixels = 16 * (size + 1);
                if (!frameInvisible) {
                    pixels -= 4;
                }
                image = image.modifyWith(ImageEffect.Resize.to(pixels)::modify, "pixels-" + pixels);

                ExposureClient.imageRenderer().render(image, poseStack, RenderQuadSink.of(submitNodeCollector), RenderCoordinates.DEFAULT,
                        packedLight, brightness, brightness, brightness, 255);
                photographRendered = !image.isEmpty();
            }
        } else {
            photographRendered = ExposureClient.photographRenderer().render(item, false, false,
                    poseStack, RenderQuadSink.of(submitNodeCollector), packedLight, brightness, brightness, brightness, 255);
        }

        poseStack.popPose();

        return photographRendered;
    }

    public Identifier getModelLocation(int size) {
        return switch (size) {
            case 0 -> ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL;
            case 1 -> ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM;
            case 2 -> ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE;
            default -> throw new IllegalArgumentException("size " + size + " is not valid. Expected 0-2.");
        };
    }

    public int getPhotographBrightness(PhotographFrameEntity entity) {
        if (entity.getDirection() == Direction.UP)
            return 255;

        // Darken the photo same way as the block sides darken,
        // but not quite as much and allow light sources to brighten it:
        int lightLevel = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition());
        float shadeFactor = entity.level().getShade(entity.getDirection(), true);
        shadeFactor += (1f - shadeFactor) * 0.2f;

        int shadedBrightness = (int)(255 * shadeFactor);
        int missingLight = 255 - shadedBrightness;
        int lightUp = (int)(missingLight * (lightLevel / 15f * 0.5f));
        return Math.min(255, shadedBrightness + lightUp);
    }

    @Override
    protected boolean shouldShowName(T entity, double d) {
        if (Minecraft.renderNames() && (!entity.getItem().isEmpty() && entity.getItem().has(DataComponents.CUSTOM_NAME)
                && Minecraft.getInstance().crosshairPickEntity == entity)) {
            double distSqr = Minecraft.getInstance().crosshairPickEntity.distanceToSqr(entity);
            float showRangeSqr = entity.isDiscrete() ? 32.0f : 64.0f;
            return distSqr < (double) (showRangeSqr * showRangeSqr);
        }
        return false;
    }

    @Override
    protected @NotNull Component getNameTag(T entity) {
        return entity.getDisplayName();
    }
}
