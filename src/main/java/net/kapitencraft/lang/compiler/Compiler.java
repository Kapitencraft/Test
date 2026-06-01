package net.kapitencraft.lang.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.parser.VarTypeContainer;
import net.kapitencraft.lang.exe.load.ClassLoader;
import net.kapitencraft.lang.exe.load.CompilerLoaderHolder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Compiler {
    public static final File ROOT = new File("./run/src");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final LocationAnalyser LOCATION_ANALYSER = new LocationAnalyser();

    public static boolean optimize = false;
    public static File source;
    static int errorCount = 0;
    private static ClassLoader.PackageHolder<CompilerLoaderHolder> compileData;
    private static final List<ClassRegister> registers = new ArrayList<>();
    private static Stage activeStage;

    public static void register(CompilerLoaderHolder holder, String pck, @Nullable String name) {
        compileData.add(pck, name, holder);
    }

    public static void dispatch(CompilerLoaderHolder holder) {
        for (int i = 1; i <= activeStage.ordinal(); i++) {
            Stage.values()[i].action.accept(holder);
        }
    }

    public static void queueRegister(ClassConstructor aClass, ErrorStorage errorStorage, VarTypeContainer parser, @Nullable String namePrefix) {
        String name = aClass.name().lexeme();
        ClassRegister e = ClassRegister.create(aClass, errorStorage, parser, name);
        registers.add(e);
        Compiler.dispatch(e.holder);
    }

    private record ClassRegister(CompilerLoaderHolder holder, String pck, @Nullable String name) {
        public static ClassRegister create(ClassConstructor entry, ErrorStorage logger, VarTypeContainer parser, @Nullable String name) {
            return new ClassRegister(new CompilerLoaderHolder(entry, logger, parser), entry.pck(), name);
        }

        private void register() {
            Compiler.register(holder, pck, name);
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

    public static ClassLoader.PackageHolder<CompilerLoaderHolder> compile(boolean logInfo, boolean failFast, File root, @Nullable File cache) {
        compileData = ClassLoader.load(root, ".scr", CompilerLoaderHolder::new);

        source = root;

        if (compileData.isEmpty()) {
            if (logInfo)
                System.out.println("no source found. returning");
            return compileData;
        }
        ExecutorService executor = Executors.newFixedThreadPool(10, new CompilerThreadFactory());
        try {
            for (Stage stage : Stage.values()) {
                if (stage == Stage.CACHING && cache == null) {
                    if (logInfo)
                        System.out.println("Skipping step CACHING as there is no cache root provided");
                    continue;
                }

                registers.forEach(ClassRegister::register);
                registers.clear();
                activeStage = stage;
                if (logInfo)
                    System.out.printf("executing step %s\n", stage);

                if (stage == Stage.CACHING && cache.exists())
                    Util.delete(cache);

                ClassLoader.useHolders(logInfo, compileData, stage.action, executor);

                if (failFast) {
                    if (errorCount > 0) {
                        printErrors(compileData);

                        if (logInfo) {
                            if (errorCount > 100) {
                                System.err.println("only showing the first 100 errors out of " + errorCount + " total");
                            } else System.err.println(errorCount + " errors");
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

    public static ClassLoader.PackageHolder<CompilerLoaderHolder> getCompileData() {
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

    private static void printErrors(ClassLoader.PackageHolder<CompilerLoaderHolder> compileData) {
        compileData.forEach(CompilerLoaderHolder::printErrors);
    }

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, CompileCallable>[] methods();

        ClassReference[] interfaces();

        void analyse();
    }

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

    public enum Stage {
        PARSE_SOURCE(CompilerLoaderHolder::parseSource),
        CREATE_SKELETON(CompilerLoaderHolder::applySkeleton),
        VALIDATE(CompilerLoaderHolder::validate),
        SYNTAX_ANALYSIS(CompilerLoaderHolder::construct),
        SEMANTIC_ANALYSIS(CompilerLoaderHolder::analyse),
        FINALIZE_LOAD(CompilerLoaderHolder::finalizeLoad),
        CACHING(CompilerLoaderHolder::cache);

        private final Consumer<CompilerLoaderHolder> action;

        Stage(Consumer<CompilerLoaderHolder> action) {
            this.action = action;
        }
    }
}