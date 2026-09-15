package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.11 replacement for the old BakedModel-based camera model + ItemRendererMixin.
 * Selects and composites sub-models (base/gold/active/selfie/viewfinder/flash/lens/selfie stick)
 * depending on the camera state, appending them as layers of the {@link ItemStackRenderState}.
 *
 * <p>The sub-models are baked once by the Fabric {@code modifyItemModelBeforeBake} hook from the
 * {@code minecraft:composite} declared in {@code assets/exposure/items/camera.json}; their order is
 * fixed and must match the {@code PART_*} indices below. We cannot look them up through
 * {@code ModelManager#getItemModel} at runtime because that map is keyed by item id, not model id.
 */
public class CameraItemModel implements ItemModel {

    // Order MUST match the models array in assets/exposure/items/camera.json
    public static final int PART_DEFAULT = 0;
    public static final int PART_GUI = 1;
    public static final int PART_GOLD = 2;
    public static final int PART_GOLD_GUI = 3;
    public static final int PART_ACTIVE = 4;
    public static final int PART_GOLD_ACTIVE = 5;
    public static final int PART_SELFIE = 6;
    public static final int PART_GOLD_SELFIE = 7;
    public static final int PART_VIEWFINDER = 8;
    public static final int PART_FLASH = 9;
    public static final int PART_LENS = 10;
    public static final int PART_SELFIE_STICK_ATTACHMENT = 11;
    public static final int PART_SELFIE_STICK = 12;
    public static final int PART_FLASH_ACTIVE = 13;
    public static final int PART_LENS_ACTIVE = 14;
    public static final int PART_FLASH_SELFIE = 15;
    public static final int PART_LENS_SELFIE = 16;
    public static final int PART_FLASH_ACTIVE_GOLD = 17;
    public static final int PART_LENS_ACTIVE_GOLD = 18;
    public static final int PART_FLASH_SELFIE_GOLD = 19;
    public static final int PART_LENS_SELFIE_GOLD = 20;
    public static final int PART_COUNT = 21;

    private final List<ItemModel> parts;

    public CameraItemModel(List<ItemModel> parts) {
        if (parts.size() != PART_COUNT) {
            throw new IllegalArgumentException("Expected " + PART_COUNT + " baked camera sub-models, got " + parts.size());
        }
        this.parts = parts;
    }

    private ItemModel part(int index) {
        return parts.get(index);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner itemOwner, int seed) {
        if (!(stack.getItem() instanceof CameraItem camera)) {
            part(PART_DEFAULT).update(state, stack, resolver, displayContext, level, itemOwner, seed);
            return;
        }

        boolean gold = stack.getOrDefault(Exposure.DataComponents.CAMERA_GOLD, false);
        boolean inGui = displayContext == ItemDisplayContext.GUI;
        boolean hasFlash = !Attachment.FLASH.get(stack).isEmpty();
        boolean hasLens = !Attachment.LENS.get(stack).isEmpty();

        List<Integer> selected = new ArrayList<>(4);
        int base;

        if (inGui) {
            // GUI shows the flat camera icon only (official ItemRendererMixin GUI branch).
            base = gold ? PART_GOLD_GUI : PART_GUI;
        } else if (camera.isInSelfieMode(stack)) {
            boolean ownSelfieView = itemOwner instanceof LivingEntity entity
                    && entity.equals(Minecrft.get().getCameraEntity());
            if (ownSelfieView) {
                // Mirrors official 1.21.1 CameraModel: the player's own first-person selfie view
                // renders ONLY the selfie stick - no camera body and no flash/lens attachments.
                state.appendModelIdentityElement(this);
                part(PART_SELFIE_STICK).update(state, stack, resolver, displayContext, level, itemOwner, seed);
                return;
            }

            // Third-person / other players looking at the selfie camera: body + selfie stick part.
            base = gold ? PART_GOLD_SELFIE : PART_SELFIE;
            selected.add(PART_SELFIE_STICK_ATTACHMENT);

            // In 1.21.11 every appended child layer applies its OWN display transform (the old
            // CompositeModel exposed only the body's transform to the whole stack). Therefore each
            // attachment must use the dedicated selfie variant whose display transform is identical
            // to camera_selfie, so body / flash ring / lens / stick stay locked together instead of
            // the flash drifting or ending up inside the lens view.
            if (hasFlash) selected.add(gold ? PART_FLASH_SELFIE_GOLD : PART_FLASH_SELFIE);
            if (hasLens) selected.add(gold ? PART_LENS_SELFIE_GOLD : PART_LENS_SELFIE);
        } else if (camera.isActive(stack)) {
            base = gold ? PART_GOLD_ACTIVE : PART_ACTIVE;
            selected.add(PART_VIEWFINDER);
            if (hasFlash) selected.add(gold ? PART_FLASH_ACTIVE_GOLD : PART_FLASH_ACTIVE);
            if (hasLens) selected.add(gold ? PART_LENS_ACTIVE_GOLD : PART_LENS_ACTIVE);
        } else {
            base = gold ? PART_GOLD : PART_DEFAULT;
            if (hasFlash) selected.add(PART_FLASH);
            if (hasLens) selected.add(PART_LENS);
        }

        state.appendModelIdentityElement(this);
        part(base).update(state, stack, resolver, displayContext, level, itemOwner, seed);
        for (int index : selected) {
            part(index).update(state, stack, resolver, displayContext, level, itemOwner, seed);
        }
    }
}
