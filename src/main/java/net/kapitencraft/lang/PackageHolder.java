package net.kapitencraft.lang;

import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.ClassLoaderHolder;
import net.kapitencraft.lang.exe.load.CompileSource;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PackageHolder<T extends ClassLoaderHolder<T>> {
    private final Map<String, PackageHolder<T>> packages = new HashMap<>();
    private final Map<String, T> classes = new HashMap<>();

    public void add(String pck, String name, T val) {
        String[] packages = pck.split("\\.");
        PackageHolder<T> holder = this.packages.get(packages[0]);
        for (int i = 1; i < packages.length; i++) {
            holder = holder.getOrCreate(packages[i]);
        }
        holder.classes.put(name, val);
    }

    public PackageHolder<T> getOrCreate(String name) {
        return packages.computeIfAbsent(name, n -> new PackageHolder<>());
    }

    public void forEach(Consumer<T> sink) {
        classes.values().forEach(sink);
        packages.values().forEach(h -> h.forEach(sink));
    }

    public int size() {
        int size = this.classes.size();
        for (PackageHolder<T> value : this.packages.values()) {
            size += value.size();
        }
        return size;
    }

    public @Nullable T getEntry(String target) {
        PackageHolder<T> holder = this;
        String[] split = target.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            String s = split[i];
            holder = holder.packages.get(s);
            if (holder == null) return null;
        }
        return holder.classes.get(split[split.length - 1]);
    }

    public boolean isEmpty() {
        return this.packages.isEmpty() && this.classes.isEmpty();
    }

    public void useClasses(BiConsumer<Map<String, T>, Package> consumer, Package pck) {
        consumer.accept(this.classes, pck);
        this.packages.forEach((name, holder1) ->
                holder1.useClasses(consumer, pck.getOrCreatePackage(name))
        );
    }

    public void useHolders(Consumer<CompileSource> consumer) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<Pair<PackageHolder<CompileSource>, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        AtomicInteger completed = new AtomicInteger(0);
        int total = root.size();
        while (!packageData.isEmpty()) {
            Pair<PackageHolder<CompileSource>, Package> data = packageData.getFirst();
            PackageHolder<CompileSource> holder = data.getFirst();
            Package pck = data.getSecond();
            holder.classes.forEach((n, o) -> {
                if (!o.getErrorInfo().hadError())
                    futures.add(CompletableFuture.runAsync(() -> consumer.accept(o), executor)
                            .whenComplete((v, ex) -> {
                                if (ex != null) {
                                    System.err.printf("error in thread: %s: %s\n", o, ex.getMessage());
                                    System.exit(1);
                                }
                                int done = completed.incrementAndGet();
                                if (logInfo)
                                    printProgress(done, total);
                            }));
            });
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.removeFirst();
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        if (logInfo) {
            printProgress(total, total);
            System.out.println();
        }
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

    public static <T extends ClassLoaderHolder<T>> PackageHolder<T> load(File fileLoc, String end, Function<File, T> constructor) {
        PackageHolder<T> root = new PackageHolder<>();
        loadRecursive(fileLoc, root, end, constructor);
        return root;
    }

    private static <T extends ClassLoaderHolder<T>> void loadRecursive(File root, PackageHolder<T> holder, String extension, Function<File, T> constructor) {
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file1 : files) {
            if (file1.isDirectory()) {
                PackageHolder<T> child = new PackageHolder<>();
                holder.packages.put(file1.getName(), child);
                loadRecursive(file1, child, extension, constructor);
            } else {
                String name = file1.getName().replace(extension, "");
                holder.classes.put(name, constructor.apply(file1));
            }
        }
    }
}
