package io.github.mortuusars.exposure.neoforge.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Can be used to add/modify custom entity in frame NBT data that is stored in a frame.
 * Fired server side on NeoForge.EVENT_BUS.
 */
public class ModifyEntityInFrameDataEvent extends Event {
    private final CameraHolder cameraHolder;
    private final ItemStack camera;
    private final LivingEntity entityInFrame;
    private final ExtraData data;

    public ModifyEntityInFrameDataEvent(CameraHolder cameraHolder,
                                        ItemStack camera,
                                        LivingEntity entityInFrame,
                                        ExtraData data) {
        this.cameraHolder = cameraHolder;
        this.camera = camera;
        this.entityInFrame = entityInFrame;
        this.data = data;
    }

    public CameraHolder getCameraHolder() {
        return cameraHolder;
    }

    public Entity getCameraHolderEntity() {
        return cameraHolder.asHolderEntity();
    }

    public ItemStack getCamera() {
        return camera;
    }

    public LivingEntity getEntityInFrame() {
        return entityInFrame;
    }

    public ExtraData getData() {
        return data;
    }
}