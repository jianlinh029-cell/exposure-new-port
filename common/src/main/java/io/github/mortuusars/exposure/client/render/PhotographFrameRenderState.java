package io.github.mortuusars.exposure.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PhotographFrameRenderState extends EntityRenderState {
    public Direction direction = Direction.NORTH;
    public float yRot;
    public float xRot;
    public int size;
    public float itemRotation;
    public boolean isFrameInvisible;
    public boolean isGlowing;
    public int photographBrightness;
    public final ItemStackRenderState item = new ItemStackRenderState();
    public @Nullable ItemStack stack;
}
