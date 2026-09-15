package io.github.mortuusars.exposure.neoforge.integration.kubejs.event;

import dev.latvian.mods.kubejs.entity.KubeEntityEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyFrameExtraDataEvent;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModifyFrameExtraDataEventJS extends ModifyFrameExtraDataEvent implements KubeEntityEvent {
    public ModifyFrameExtraDataEventJS(CameraHolder cameraHolder, ItemStack stack, CaptureParameters captureParameters,
                                       List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        super(cameraHolder, stack, captureParameters, positionsInFrame, entitiesInFrame, data);
    }

    @Override
    public Entity getEntity() {
        return getCameraHolderEntity();
    }
}
