package io.github.mortuusars.exposure.util.cycles.task;

import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Task<T> {
    protected boolean started;
    protected boolean done;

    public abstract CompletableFuture<T> execute();

    public void tick() {
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDone() {
        return done;
    }

    protected void setStarted() {
        this.started = true;
    }

    protected void setDone() {
        this.done = true;
    }

    public Task<T> onError(Consumer<TranslatableError> errorConsumer) {
        return new HandleErrorTask<>(this, errorConsumer);
    }

    public Task<T> withTimeout(int timeout, TimeUnit timeUnit) {
        return new TimeoutTask<>(this, timeout, timeUnit);
    }

    public Task<T> withTimeout(T valueIfTimedOut, int timeout, TimeUnit timeUnit) {
        return new TimeoutTask<>(this, timeout, timeUnit, valueIfTimedOut);
    }

    public <R> Task<R> then(Function<T, R> transformFunction) {
        return new ChainedTask<>(this, transformFunction, false);
    }

    public <R> Task<R> thenAsync(Function<T, R> transformFunction) {
        return new ChainedTask<>(this, transformFunction, true);
    }

    public Task<Void> accept(Consumer<T> acceptFunction) {
        return new AcceptTask<>(this, acceptFunction, false);
    }

    public Task<Void> acceptAsync(Consumer<T> acceptFunction) {
        return new AcceptTask<>(this, acceptFunction, true);
    }

    public Task<T> overridenBy(Task<T> override) {
        return new OverrideTask<>(this, override);
    }
}
