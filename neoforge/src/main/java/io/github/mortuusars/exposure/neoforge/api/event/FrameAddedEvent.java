package io.github.mortuusars.exposure.neoforge.api.event;

import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.List;

/**
 * Fired at the very end of a shot, when frame is added to the film.
 * Fired server side on NeoForge.EVENT_BUS.
 */
public class FrameAddedEvent extends Event {

    private final CameraHolder cameraHolder;
    private final ItemStack camera;
    private final Frame frame;
    private final List<BlockPos> positionsInFrame;
    private final List<LivingEntity> entitiesInFrame;

    public FrameAddedEvent(CameraHolder cameraHolder, ItemStack camera, Frame frame,
                           List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {

        this.cameraHolder = cameraHolder;
        this.camera = camera;
        this.frame = frame;
        this.positionsInFrame = positionsInFrame;
        this.entitiesInFrame = entitiesInFrame;
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

    public Frame getFrame() {
        return frame;
    }

    public List<BlockPos> getPositionsInFrame() {
        return positionsInFrame;
    }

    public List<LivingEntity> getEntitiesInFrame() {
        return entitiesInFrame;
    }
}
