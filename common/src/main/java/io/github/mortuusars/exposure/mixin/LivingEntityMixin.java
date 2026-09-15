package io.github.mortuusars.exposure.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getMaxHeadRotationRelativeToBody", at = @At("RETURN"))
    private float onGetMaxHeadRotationRelativeToBody(float original) {
        if (this instanceof CameraOperator operator && operator.getActiveExposureCamera() != null) {
            return Math.min(original, 20);
        }
        return original;
    }
}
