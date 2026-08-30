package net.kapitencraft.lang.compiler.exe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CompileEnvironment {
    private final EnumMap<CompilerStage, CompletableFuture<?>> stageCompletions;
    private final AtomicInteger completed = new AtomicInteger(0);
    private int total;
    private final List<CompletableFuture<?>> processors = new ArrayList<>();

    public CompileEnvironment() {
        this.stageCompletions = new EnumMap<>(CompilerStage.class);
        for (CompilerStage value : CompilerStage.values()) {
            stageCompletions.put(value, new CompletableFuture<>());
        }
    }

    public void stageCompleted(CompilerStage stage) {
        printProgress(total, total);
        stageCompletions.get(stage).complete(null);
        completed.set(0);
        total = processors.size();
    }

    public <T> Function<T, CompletableFuture<T>> waitFor(CompilerStage stage) {
        return t -> {
            printProgress(completed.incrementAndGet(), total);
            if (completed.get() == total) {
                stageCompleted(stage);
            }
            return stageCompletions.get(stage).thenCombine(CompletableFuture.completedFuture(t), (o, t1) -> t1);
        };
    }

    static void printProgress(int done, int total) {
        int width = 40; // bar width
        int progress = (int) ((done / (double) total) * width);

        String bar = "[" +
                "=".repeat(progress) +
                " ".repeat(width - progress) +
                "]";

        int percent = (int) ((done / (double) total) * 100);

        System.out.print("\r" + bar + " " + percent + "% (" + done + "/" + total + ")");
    }

    public void addProcessor(CompletableFuture<?> processor) {
        this.processors.add(processor);
    }
}
