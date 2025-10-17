package ecs.utils;

import ecs.Entity;

import java.util.*;
import java.util.concurrent.*;

/**
 * Splits entity iteration into parallel chunks.
 */
public class ParallelECSExecutor {
    private final ExecutorService executor;

    public ParallelECSExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void forEachParallel(Collection<Entity> entities, int chunkSize, java.util.function.Consumer<Entity> task) {
        if (entities.isEmpty()) return;

        List<Entity> entityList = new ArrayList<>(entities);
        List<Callable<Void>> jobs = new ArrayList<>();

        for (int i = 0; i < entityList.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, entityList.size());
            jobs.add(() -> {
                for (int j = start; j < end; j++) {
                    task.accept(entityList.get(j));
                }
                return null;
            });
        }

        try {
            executor.invokeAll(jobs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
