package io.github.mortuusars.exposure.util.cycles;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Cycles {
    private final Queue<Task<?>> queuedTasks = new LinkedList<>();
    private final List<Task<?>> parallelTasks = new ArrayList<>();

    public void enqueueTask(Task<?> task) {
        Preconditions.checkState(!isInQueue(task), "This task is already in queue.");
        queuedTasks.add(task);
    }

    public void addParallelTask(Task<?> task) {
        Preconditions.checkState(!isInParallelTaskList(task), "This task is already added.");
        parallelTasks.add(task);
    }

    public boolean isInQueue(Task<?> task) {
        return queuedTasks.contains(task);
    }

    public boolean isInParallelTaskList(Task<?> task) {
        return parallelTasks.contains(task);
    }

    public void tick() {
        tickQueuedTasks();
        tickParallelTasks();
    }

    private void tickQueuedTasks() {
        @Nullable Task<?> task = queuedTasks.peek();
        if (task == null) return;

        if (!task.isStarted()) {
            task.execute();
        }

        task.tick();

        if (task.isDone()) {
            queuedTasks.remove(task);
        }
    }

    private void tickParallelTasks() {
        parallelTasks.removeIf(task -> {
            if (!task.isStarted()) {
                task.execute();
            }

            task.tick();

            return task.isDone();
        });
    }
}
