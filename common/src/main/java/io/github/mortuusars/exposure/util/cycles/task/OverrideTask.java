package io.github.mortuusars.exposure.util.cycles.task;

import java.util.concurrent.CompletableFuture;

/**
 * Tasks are executed in order. Second task will be returned if it is successful.
 */
public class OverrideTask<T> extends Task<T> {
    private final Task<T> base;
    private final Task<T> override;

    public OverrideTask(Task<T> base, Task<T> override) {
        this.base = base;
        this.override = override;
    }

    @Override
    public CompletableFuture<T> execute() {
        return base.execute()
                .thenCompose(baseResult -> override.execute()
                        .exceptionally(exception -> baseResult)
                        .thenApply(overrideResult -> {
                            if (overrideResult instanceof Result<?> result && result.isError()) {
                                return isResultSuccessful(baseResult) ? baseResult : overrideResult;
                            }
                            return overrideResult;
                        }));

    }

    private boolean isResultSuccessful(T resultValue) {
        return resultValue != null && (!(resultValue instanceof Result<?> result) || result.isSuccessful());
    }

    @Override
    public void tick() {
        base.tick();
        override.tick();
    }

    @Override
    public boolean isDone() {
        return base.isDone() && override.isDone();
    }

    @Override
    public boolean isStarted() {
        return base.isStarted() || override.isStarted();
    }
}