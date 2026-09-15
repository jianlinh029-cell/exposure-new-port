package io.github.mortuusars.exposure.core.cycles;

import io.github.mortuusars.exposure.util.cycles.Cycles;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class CyclesTests {
    public static class TestTask extends Task<Boolean> {
        private int ticks;
        private final CompletableFuture<Boolean> future = new CompletableFuture<>();

        public TestTask(int ticks) {
            this.ticks = ticks;
        }

        @Override
        public CompletableFuture<Boolean> execute() {
            setStarted();
            if (ticks <= 0) {
                setDone();
                future.complete(true);
            }
            return future;
        }

        @Override
        public void tick() {
            ticks--;

            if (!isDone() && ticks <= 0) {
                setDone();
                future.complete(true);
            }
        }
    }

    @Test
    void enqueuedTaskExecutesAndFinishesCorrectly() {
        Cycles cycles = new Cycles();

        TestTask task = new TestTask(0);
        cycles.enqueueTask(task);

        cycles.tick();

        assertTrue(task.isDone());
        assertFalse(cycles.isInQueue(task));
    }

    @Test
    void enqueuedTickingTaskExecutesAndFinishesCorrectly() {
        Cycles cycles = new Cycles();
        int ticks = 5;

        TestTask task = new TestTask(ticks);
        cycles.enqueueTask(task);

        for (int i = 0; i < ticks; i++) {
            cycles.tick();
        }

        assertTrue(task.isDone());
        assertFalse(cycles.isInQueue(task));
    }

    @Test
    void parallelTasksExecuteAndFinishProperly() {
        Cycles cycles = new Cycles();

        TestTask task1 = new TestTask(1);
        TestTask task2 = new TestTask(5);
        TestTask task3 = new TestTask(10);

        cycles.addParallelTask(task1);
        cycles.addParallelTask(task2);
        cycles.addParallelTask(task3);

        for (int i = 0; i < 5; i++) {
            cycles.tick();
        }

        assertTrue(task1.isDone());
        assertFalse(cycles.isInParallelTaskList(task1));

        assertTrue(task2.isDone());
        assertFalse(cycles.isInParallelTaskList(task2));

        assertFalse(task3.isDone());
        assertTrue(cycles.isInParallelTaskList(task3));
    }
}
