package io.github.mortuusars.exposure.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

public class Vec3Util {
    public static List<Vec3> getProbeVectors(Vec3 direction, float offsetDegrees) {
        // World up vector
        Vec3 upVector = new Vec3(0, 1, 0);

        // Calculate right vector (cross product of forward and up)
        Vec3 rightVector = direction.cross(upVector).normalize();
        if (Mth.equal(rightVector.length(), 0)) {
            // Handle edge case: looking straight up or down
            rightVector = new Vec3(1, 0, 0).cross(direction).normalize();
        }

        // Calculate adjusted up vector (orthogonal to forward and right)
        upVector = rightVector.cross(direction).normalize();

        float offsetRadians = (float) Math.toRadians(offsetDegrees);

        Vec3 topLeft = calculateOffsetDirection(direction, upVector, rightVector, offsetRadians, offsetRadians);
        Vec3 topRight = calculateOffsetDirection(direction, upVector, rightVector, -offsetRadians, offsetRadians);
        Vec3 bottomLeft = calculateOffsetDirection(direction, upVector, rightVector, offsetRadians, -offsetRadians);
        Vec3 bottomRight = calculateOffsetDirection(direction, upVector, rightVector, -offsetRadians, -offsetRadians);

        return Stream.of(direction, topLeft, topRight, bottomLeft, bottomRight).toList();
    }

    public static Vec3 calculateOffsetDirection(Vec3 forward, Vec3 up, Vec3 right, double horizontalOffset, double verticalOffset) {
        Vec3 horizontalRotated = rotateVector(forward, up, horizontalOffset);
        return rotateVector(horizontalRotated, right, verticalOffset).normalize();
    }

    public static Vec3 rotateVector(Vec3 vec, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vec.dot(axis);
        return vec.scale(cos)
                .add(axis.cross(vec).scale(sin))
                .add(axis.scale(dot * (1 - cos)));
    }
}
