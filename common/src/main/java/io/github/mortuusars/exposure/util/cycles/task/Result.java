package io.github.mortuusars.exposure.util.cycles.task;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.TranslatableError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> {
    private final @Nullable T value;
    private final @Nullable TranslatableError error;

    private Result(@Nullable("If error is provided") T value, @Nullable("If value is provided") TranslatableError error) {
        Preconditions.checkState(value != null || error != null,
                "Either value or error must be provided. Both cannot be null.");
        this.value = value;
        this.error = error;
    }

    public boolean isSuccessful() {
        return value != null;
    }

    public boolean isError() {
        return error != null;
    }

    public Result<T> ifSuccessful(Consumer<T> valueConsumer) {
        if (isSuccessful()) {
            valueConsumer.accept(getValue());
        }
        return this;
    }

    public Result<T> ifError(Consumer<TranslatableError> errorConsumer) {
        if (isError()) {
            errorConsumer.accept(getError());
        }
        return this;
    }

    public <R> R map(Function<T, R> ifSuccessful, Function<TranslatableError, R> ifError) {
        return isSuccessful() ? ifSuccessful.apply(getValue()) : ifError.apply(getError());
    }

    public @NotNull T getValue() {
        Preconditions.checkState(value != null, "Called getValue on an error result. Should check with isSuccessful first.");
        return value;
    }

    public @NotNull TranslatableError getError() {
        Preconditions.checkState(error != null, "Called getError on a successful result. Should check with isError first.");
        return error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> error(TranslatableError error) {
        return new Result<>(null, error);
    }

    public T unwrap(Consumer<TranslatableError> errorConsumer) throws IllegalStateException {
        this.ifError(errorConsumer);
        return getValue();
    }

    public T unwrap() throws IllegalStateException {
        return getValue();
    }

    public <R> Result<R> remapError() {
        return error(getError());
    }
}
