package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;

public class CameraStandEntityRenderer <T extends CameraStandEntity> extends EntityRenderer<T, CameraStandRenderState> {
    public static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/item/camera.png");
    public static final float MOUNT_SCALE = 0.9f;

    private final ItemModelResolver itemModelResolver;

    public CameraStandEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public CameraStandRenderState createRenderState() {
        return new CameraStandRenderState();
    }

    @Override
    public void extractRenderState(T entity, CameraStandRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.yRot = entity.getYRot(partialTick);
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        if (entity.getVehicle() != null) {
            state.vehicleYRot = Mth.lerp(partialTick, entity.getVehicle().yRotO, entity.getVehicle().getYRot());
            state.hasVehicle = true;
        }

        state.hurtTime = (float) entity.getHurtTime() - partialTick;
        state.damage = Math.max(0, entity.getDamage() - partialTick);
        state.hurtDir = entity.getHurtDir();
        state.isMalfunctioned = entity.isMalfunctioned();

        var camera = entity.getCamera();
        state.hasCamera = !camera.isEmpty();
        if (state.hasCamera) {
            state.cameraScaleOnStand = camera.getItem() instanceof CameraItem cameraItem ? cameraItem.getScaleOnStand() : MOUNT_SCALE;
            itemModelResolver.updateForNonLiving(state.camera, camera, ItemDisplayContext.NONE, entity);
        }
    }

    @Override
    public void submit(CameraStandRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);

        if (state.hurtTime > 0.0F) {
            float rotation = Mth.sin(state.hurtTime) * state.hurtTime * state.damage / 10.0F * state.hurtDir;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        }

        renderStand(state, poseStack, submitNodeCollector);
        renderMount(state, poseStack, submitNodeCollector);
        if (state.hasCamera) {
            renderCamera(state, poseStack, submitNodeCollector);
        }
    }

    private void renderStand(CameraStandRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();

        if (state.hasVehicle) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.vehicleYRot + 45));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);

        BlockStateModel model = PlatformHelperClient.getModel(ExposureClient.Models.CAMERA_STAND);
        submitNodeCollector.submitBlockModel(poseStack, RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS),
                model, 1.0f, 1.0f, 1.0f, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void renderMount(CameraStandRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0, 1.125, 0);
        float scale = MOUNT_SCALE;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-10));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);
        BlockStateModel mountModel = PlatformHelperClient.getModel(ExposureClient.Models.CAMERA_STAND_MOUNT);
        submitNodeCollector.submitBlockModel(poseStack, RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS),
                mountModel, 1.0f, 1.0f, 1.0f, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void renderCamera(CameraStandRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();

        poseStack.translate(0, 1.125, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.translate(0, 0.125 * MOUNT_SCALE, 0);

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-15));
        }

        poseStack.scale(state.cameraScaleOnStand, state.cameraScaleOnStand, state.cameraScaleOnStand);
        poseStack.translate(0, 0.5, 0);

        state.camera.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
