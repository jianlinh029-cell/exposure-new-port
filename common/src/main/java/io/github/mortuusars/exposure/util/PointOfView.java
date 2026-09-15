package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Some functionality related to camera (player's view) implemented for server side, as client classes are not available.
 *
 * @param pos position of the camera.
 * @param dir look direction of the camera.
 */
public record PointOfView(Vec3 pos, Vec3 dir) {
    public static PointOfView of(CameraHolder holder) {
        return of(holder.asHolderEntity());
    }

    public static PointOfView of(Entity entity) {
        return new PointOfView(
                entity.position().add(0, entity.getEyeHeight(), 0),
                // Not allowing xRot to reach 90deg because it'll cause problems with rotations down the line.
                // Precision is not that important here.
                Vec3.directionFromRotation(Mth.clamp(entity.getXRot(), -89, 89), entity.getYRot()));
    }

    // --

    public PointOfView rotateX(double degrees) {
        Vec3 upVector = new Vec3(0, 1, 0);
        Vec3 rightVector = dir.cross(upVector).normalize();
        if (Mth.equal(rightVector.length(), 0)) {
            rightVector = new Vec3(1, 0, 0).cross(dir).normalize();
        }
        Vec3 rotated = Vec3Util.rotateVector(dir(), rightVector, Math.toRadians(degrees));
        return new PointOfView(pos, rotated);
    }

    public PointOfView rotateY(double degrees) {
        return new PointOfView(pos, dir.yRot((float) Math.toRadians(degrees)));
    }

    public PointOfView move(double x, double y, double z) {
        return new PointOfView(pos.add(x, y, z), dir);
    }

    public PointOfView reverseDirection() {
        return new PointOfView(pos, dir.reverse());
    }

    public PointOfView limitMaxDistance(CameraHolder holder, double distance) {
        return limitMaxDistance(holder.asHolderEntity(), distance);
    }

    public PointOfView limitMaxDistance(Entity entity, double distance) {
        float maxDistance = getMaxCameraDistance(entity, pos, dir, (float) distance);
        return new PointOfView(pos.add(dir.normalize().scale(-maxDistance)), dir);
    }

    /**
     * This method is the same as {@link net.minecraft.client.Camera#getMaxZoom(float)}
     * We need it to calculate proper camera position if camera is in third-person mode, so it does not clip into blocks.
     */
    private float getMaxCameraDistance(Entity entity, Vec3 position, Vec3 direction, float maxDistance) {
        for (int i = 0; i < 8; i++) {
            float xOff = (float) ((i & 1) * 2 - 1);
            float yOff = (float) ((i >> 1 & 1) * 2 - 1);
            float zOff = (float) ((i >> 2 & 1) * 2 - 1);
            Vec3 pos = position.add(xOff * 0.1F, yOff * 0.1F, zOff * 0.1F);
            Vec3 endPos = pos.add(direction.scale(-maxDistance));
            HitResult hitResult = entity.level().clip(
                    new ClipContext(pos, endPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.of(entity)));
            if (hitResult.getType() != HitResult.Type.MISS) {
                float distanceSqr = (float) hitResult.getLocation().distanceToSqr(position);
                if (distanceSqr < Mth.square(maxDistance)) {
                    maxDistance = Mth.sqrt(distanceSqr);
                }
            }
        }

        return maxDistance;
    }
}