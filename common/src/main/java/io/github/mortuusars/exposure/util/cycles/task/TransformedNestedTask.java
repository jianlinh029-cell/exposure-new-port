package io.github.mortuusars.exposure.util.cycles.task;

public abstract class TransformedNestedTask<T, R> extends Task<R> {
    private final Task<T> task;

    public TransformedNestedTask(Task<T> task) {
        this.task = task;
    }

    public Task<T> getTask() {
        return task;
    }

    @Override
    public void tick() {
        task.tick();
    }

    @Override
    public boolean isStarted() {
        return task.isStarted();
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }
}
