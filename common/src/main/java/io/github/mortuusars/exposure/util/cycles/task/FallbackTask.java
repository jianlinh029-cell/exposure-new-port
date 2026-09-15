package io.github.mortuusars.exposure.util.cycles.task;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;

/**
 * If first task is failed, second task will be executed, and it's result returned.
 */
public class FallbackTask<T> extends Task<Result<T>> {
    private final Task<Result<T>> main;
    private final Task<Result<T>> fallback;

    public FallbackTask(Task<Result<T>> main, Task<Result<T>> fallback) {
        this.main = main;
        this.fallback = fallback;
    }

    @Override
    public CompletableFuture<Result<T>> execute() {
        return main.execute()
                .handle((mainResult, mainException) -> {
                    if (mainResult != null && mainResult.isSuccessful()) {
                        return mainResult;
                    } else if (mainException != null) {
                        Exposure.LOGGER.error("Main CaptureTask is failed: ", mainException);
                    } else if (mainResult != null && mainResult.isError()) {
                        Exposure.LOGGER.error("Main CaptureTask is failed: {}", mainResult.getError().technical().getString());
                    }

                    return captureFallback();
                });
    }

    private Result<T> captureFallback() {
        return fallback.execute().handle((fallbackResult, fallbackException) -> {
            if (fallbackException != null) {
                Exposure.LOGGER.error("Both Main and Fallback tasks are failed! Fallback error: ", fallbackException);
                return Result.<T>error(TranslatableError.GENERIC);
            }
            return fallbackResult;
        }).join();
    }

    @Override
    public void tick() {
        main.tick();
        fallback.tick();
    }

    @Override
    public boolean isDone() {
        return main.isDone() && fallback.isDone();
    }

    @Override
    public boolean isStarted() {
        return main.isStarted() || fallback.isStarted();
    }
}