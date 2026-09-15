package io.github.mortuusars.exposure.util.cycles.task;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TimeoutTask<T> extends NestedTask<T> {
    private final int timeout;
    private final TimeUnit timeUnit;
    private final T valueIfTimedOut;

    public TimeoutTask(Task<T> task, int timeout, TimeUnit timeUnit, @Nullable T valueIfTimedOut) {
        super(task);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.valueIfTimedOut = valueIfTimedOut;
    }

    public TimeoutTask(Task<T> task, int timeout, TimeUnit timeUnit) {
        super(task);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.valueIfTimedOut = null;
    }

    @Override
    public CompletableFuture<T> execute() {
        return valueIfTimedOut != null
                ? getTask().execute().completeOnTimeout(valueIfTimedOut, timeout, timeUnit)
                : getTask().execute().orTimeout(timeout, timeUnit);
    }
}
