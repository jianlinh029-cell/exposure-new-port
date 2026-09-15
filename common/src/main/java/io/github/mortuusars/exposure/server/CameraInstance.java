package io.github.mortuusars.exposure.server;

import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CameraInstance {
    protected final CameraId cameraId;

    private int deferredCooldownTicks = 0;
    private long projectionDeadline = -1;
    private ProjectionState projectionState = ProjectionState.IDLE;
    private Optional<TranslatableError> projectionError = Optional.empty();

    public CameraInstance(CameraId cameraId) {
        this.cameraId = cameraId;
    }

    // --

    public int getDeferredCooldown() {
        return deferredCooldownTicks;
    }

    public void setDeferredCooldown(int ticks) {
        deferredCooldownTicks = ticks;
    }

    // --

    public ProjectionState getProjectionState(Level level) {
        if (isWaitingForProjection() && isProjectionTimedOut(level)) {
            return ProjectionState.TIMED_OUT;
        }
        return projectionState;
    }

    public Optional<TranslatableError> getProjectionError(Level level) {
        if (isWaitingForProjection() && isProjectionTimedOut(level)) {
            return Optional.of(TranslatableError.TIMED_OUT);
        }
        return projectionError;
    }

    public long getProjectionDeadline() {
        return projectionDeadline;
    }

    public void waitForProjection(long deadline) {
        projectionDeadline = deadline;
    }

    public boolean isWaitingForProjection() {
        return projectionDeadline >= 0;
    }

    public boolean isProjectionTimedOut(Level level) {
        return level.getGameTime() > projectionDeadline;
    }

    public void setProjectionResult(Level level, boolean successful, Optional<TranslatableError> error) {
        if (isWaitingForProjection() && !isProjectionTimedOut(level)) {
            projectionState = successful ? ProjectionState.SUCCESSFUL : ProjectionState.FAILED;
            projectionDeadline = -1;
            projectionError = error;
        }
    }

    public void stopWaitingForProjection() {
        projectionState = ProjectionState.IDLE;
        projectionDeadline = -1;
        projectionError = Optional.empty();
    }

    // --

    public enum ProjectionState {
        IDLE,
        WAITING,
        SUCCESSFUL,
        FAILED,
        TIMED_OUT;
    }
}
