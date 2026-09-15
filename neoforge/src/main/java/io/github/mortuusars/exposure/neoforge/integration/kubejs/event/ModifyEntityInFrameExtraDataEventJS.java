package io.github.mortuusars.exposure.neoforge.integration.kubejs.event;

import dev.latvian.mods.kubejs.entity.KubeEntityEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyEntityInFrameDataEvent;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Can be used to add custom entity in frame NBT data that is stored in a frame.
 */
public class ModifyEntityInFrameExtraDataEventJS extends ModifyEntityInFrameDataEvent implements KubeEntityEvent {
    public ModifyEntityInFrameExtraDataEventJS(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
        super(cameraHolder, camera, entityInFrame, data);
    }

    @Override
    public Entity getEntity() {
        return getCameraHolderEntity();
    }
}
