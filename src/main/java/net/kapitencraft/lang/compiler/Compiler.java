package net.kapitencraft.lang.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.JavaCompileSource;
import net.kapitencraft.lang.compiler.exe.pipeline.CompilePipeline;
import net.kapitencraft.lang.compiler.exe.pipeline.JavaCompilePipeline;
import net.kapitencraft.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.lang.exe.load.ClassLoader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.tool.Util;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Compiler {
    public static final File ROOT = new File("./run/src");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final LocationAnalyser LOCATION_ANALYSER = new LocationAnalyser();

    public static final List<CompilePipeline<?>> PIPELINES = List.of(
            JavaCompilePipeline.INSTANCE
    );

    public static boolean optimize = false;
    public static File source;
    private static SourceTree compileData;
    private static CompileStage activeStage;

    public static void dispatch(CompileSource holder) {
        for (int i = CompileStage.CREATE_SKELETON.ordinal(); i <= activeStage.ordinal(); i++) {
            holder.process(CompileStage.values()[i], compileData);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 && "-o".equals(args[0])) {
            optimize = true;
        }

        File cache = ClassLoader.cacheLoc;

        System.out.println("Compiling...");

        compile(true, true, ROOT, cache);
    }

    public static SourceTree compile(boolean logInfo, boolean failFast, File root, @Nullable File cache) {
        compileData = SourceTree.load(root);

        source = root;

        if (compileData.isEmpty()) {
            if (logInfo)
                System.out.println("no source found. returning");
            return compileData;
        }
        ExecutorService executor = Executors.newFixedThreadPool(10, new CompilerThreadFactory());
        ErrorStorage.overallErrorCount = 0;
        try {
            for (CompileStage stage : CompileStage.values()) {
                if (stage == CompileStage.CACHING && cache == null) {
                    if (logInfo)
                        System.out.println("Skipping step CACHING as there is no cache root provided");
                    continue;
                }

                activeStage = stage;
                if (logInfo)
                    System.out.printf("executing step %s\n", stage);

                if (stage == CompileStage.CACHING && cache.exists())
                    Util.delete(cache);

                compileData.execute(stage, executor, logInfo);

                if (failFast) {
                    if (ErrorStorage.overallErrorCount > 0) {
                        compileData.forEach(CompileSource::printErrors);

                        if (logInfo) {
                            if (ErrorStorage.overallErrorCount > 100) {
                                System.err.println("only showing the first 100 errors out of " + ErrorStorage.overallErrorCount + " total");
                            } else System.err.println(ErrorStorage.overallErrorCount + " errors");
                        }
                        System.exit(65);
                    }
                }
            }
        } finally {
            executor.shutdownNow();
        }
        return compileData;
    }

    public static SourceTree getCompileData() {
        return compileData;
    }

    /**
     * thread factory for more reasonable names
     */
    private static class CompilerThreadFactory implements ThreadFactory {
        AtomicInteger poolNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "CompilerThread#" + poolNumber.getAndIncrement());
        }
    }

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, CompileCallable>[] methods();

        ClassReference[] interfaces();

        void analyse();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cache(File cacheBase, CacheBuilder builder, String path, CacheableClass target, String name) throws IOException {
        JsonObject object = builder.cacheClass(target);
        File cacheTarget = new File(cacheBase, path + "/" + name + ".scrc");
        if (!cacheTarget.exists()) {
            cacheTarget.getParentFile().mkdirs();
            cacheTarget.createNewFile();
        }
        FileWriter writer = new FileWriter(cacheTarget);
        writer.write(GSON.toJson(object));
        writer.close();
    }

    public static void error(int lineIndex, int lineStartIndex, String msg, String fileId, String line) {
        report(System.err, lineIndex, msg, fileId, lineStartIndex, line);
    }

    public static void warn(int lineIndex, int lineStartIndex, String msg, String filedId, String line) {
        System.out.print("\u001B[33m"); //set output color to yellow
        report(System.out, lineIndex, msg, filedId, lineStartIndex, line);
        System.out.print("\u001B[0m"); //reset output color
    }

    public static void report(PrintStream target, int lineIndex, String message, String fileId, int startIndex, String line) {
        target.print(fileId);
        target.print(":");
        target.print(lineIndex);
        target.print(": ");
        target.println(message);

        target.println(line);
        target.println(" ".repeat(startIndex) + "^");
    }
}