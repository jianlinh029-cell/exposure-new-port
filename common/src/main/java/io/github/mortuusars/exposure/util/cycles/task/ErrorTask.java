package io.github.mortuusars.exposure.util.cycles.task;

import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;

public class ErrorTask<T> extends Task<Result<T>> {
    private final TranslatableError error;

    public ErrorTask(TranslatableError error) {
        this.error = error;
    }

    @Override
    public CompletableFuture<Result<T>> execute() {
        return CompletableFuture.completedFuture(Result.error(error));
    }
}
