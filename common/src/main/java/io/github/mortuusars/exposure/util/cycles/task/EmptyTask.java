package io.github.mortuusars.exposure.util.cycles.task;

import java.util.concurrent.CompletableFuture;

public class EmptyTask<T> extends Task<T> {
    @Override
    public CompletableFuture<T> execute() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isDone() {
        return true;
    }
}
