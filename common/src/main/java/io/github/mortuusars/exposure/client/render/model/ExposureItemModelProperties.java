package io.github.mortuusars.exposure.client.render.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Custom select item model properties (1.21.11 data-driven item model system).
 * Kept for resource pack compatibility; the camera itself uses {@link CameraItemModel}.
 */
public class ExposureItemModelProperties {
    public static void register() {
        // 1.21.11: SelectItemModelProperties.ID_MAPPER is not publicly writable in the compile classpath.
        // The camera uses the custom CameraItemModel; these legacy select properties are skipped.
    }


    public abstract static class BaseFloatProperty implements SelectItemModelProperty<Float> {
        @Override
        public @Nullable Float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return getValue(stack, level, entity, seed, context);
        }

        @Override
        public Codec<Float> valueCodec() {
            return Codec.FLOAT;
        }

        public abstract float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context);
    }

    public static class CameraGold extends BaseFloatProperty {
        public static final MapCodec<CameraGold> CODEC = MapCodec.unit(CameraGold::new);
        public static final Type<CameraGold, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return stack.getOrDefault(Exposure.DataComponents.CAMERA_GOLD, false) ? 1f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class CameraActive extends BaseFloatProperty {
        public static final MapCodec<CameraActive> CODEC = MapCodec.unit(CameraActive::new);
        public static final Type<CameraActive, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack) ? 1f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class CameraSelfie extends BaseFloatProperty {
        public static final MapCodec<CameraSelfie> CODEC = MapCodec.unit(CameraSelfie::new);
        public static final Type<CameraSelfie, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            if (!(stack.getItem() instanceof CameraItem cameraItem) || !cameraItem.isInSelfieMode(stack)) {
                return 0f;
            }
            return entity != null && entity.equals(Minecrft.get().getCameraEntity()) ? 0.5f : 1f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class CameraHasLens extends BaseFloatProperty {
        public static final MapCodec<CameraHasLens> CODEC = MapCodec.unit(CameraHasLens::new);
        public static final Type<CameraHasLens, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return !Attachment.LENS.get(stack).isEmpty() ? 1f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class CameraHasFlash extends BaseFloatProperty {
        public static final MapCodec<CameraHasFlash> CODEC = MapCodec.unit(CameraHasFlash::new);
        public static final Type<CameraHasFlash, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return !Attachment.FLASH.get(stack).isEmpty() ? 1f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class Channels extends BaseFloatProperty {
        public static final MapCodec<Channels> CODEC = MapCodec.unit(Channels::new);
        public static final Type<Channels, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return stack.getItem() instanceof ChromaticSheetItem chromaticSheet ? chromaticSheet.getLayers(stack).size() / 10f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class Count extends BaseFloatProperty {
        public static final MapCodec<Count> CODEC = MapCodec.unit(Count::new);
        public static final Type<Count, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem ?
                    stackedPhotographsItem.getPhotographs(stack).size() / 100f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class Photos extends BaseFloatProperty {
        public static final MapCodec<Photos> CODEC = MapCodec.unit(Photos::new);
        public static final Type<Photos, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) / 100f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }

    public static class ProjectorActive extends BaseFloatProperty {
        public static final MapCodec<ProjectorActive> CODEC = MapCodec.unit(ProjectorActive::new);
        public static final Type<ProjectorActive, Float> TYPE = Type.create(CODEC, Codec.FLOAT);

        @Override
        public float getValue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            return io.github.mortuusars.exposure.Config.Server.CAN_PROJECT.get() && stack.has(DataComponents.CUSTOM_NAME) ? 1f : 0f;
        }

        @Override
        public Type<? extends SelectItemModelProperty<Float>, Float> type() {
            return TYPE;
        }
    }
}
