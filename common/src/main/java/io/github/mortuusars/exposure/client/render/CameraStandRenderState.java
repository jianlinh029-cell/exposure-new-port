package io.github.mortuusars.exposure.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class CameraStandRenderState extends EntityRenderState {
    public float yRot;
    public float xRot;
    public float vehicleYRot;
    public boolean hasVehicle;
    public float hurtTime;
    public float damage;
    public int hurtDir;
    public boolean isMalfunctioned;
    public boolean hasCamera;
    public float cameraScaleOnStand = 1.0F;
    public final ItemStackRenderState camera = new ItemStackRenderState();
}
