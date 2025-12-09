package net.kapitencraft.lang.compiler;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.load.CompilerLoaderHolder;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Compiler {
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

    public static void queueRegister(Holder.Class aClass, ErrorLogger errorLogger, VarTypeParser parser, @Nullable String namePrefix) {
        String name = aClass.name().lexeme();
        ClassRegister e = ClassRegister.create(aClass, errorLogger, parser, name);
        registers.add(e);
        Compiler.dispatch(e.holder);
    }

    private record ClassRegister(CompilerLoaderHolder holder, String pck, @Nullable String name) {
        public static ClassRegister create(Holder.Class entry, ErrorLogger logger, VarTypeParser parser, @Nullable String name) {
            return new ClassRegister(new CompilerLoaderHolder(entry, logger, parser), entry.pck(), name);
        }

        private void register() {
            Compiler.register(holder, pck, name);
        }
    }

    public static void main(String[] args) {
        File root = new File("./run/src");
        File cache = ClassLoader.cacheLoc;

        System.out.println("Compiling...");

        compileData = ClassLoader.load(root, ".scr", CompilerLoaderHolder::new);

        Executor executor = Executors.newFixedThreadPool(10);
        for (Stage stage : Stage.values()) {
            registers.forEach(ClassRegister::register);
            registers.clear();
            activeStage = stage;
            System.out.printf("executing step %s\n", stage);

            ClassLoader.useHolders(compileData, stage.action, executor);

            if (errorCount > 0) {
                if (errorCount > 100) {
                    System.err.println("only showing the first 100 errors out of " + errorCount + " total");
                } else System.err.println(errorCount + " errors");
                System.exit(65);
            }
        }

        if (cache.exists()) Util.delete(cache);

        System.out.println("executing step CACHING");

        List<CompletableFuture<?>> futures = new ArrayList<>();
        ClassLoader.useClasses(compileData, (stringClassHolderMap, aPackage) ->
                stringClassHolderMap.values().forEach(classHolder -> futures.add(CompletableFuture.runAsync(classHolder::cache)))
        );
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        if (errorCount > 0) System.exit(65);
    }

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, CompileCallable>[] methods();

        ClassReference[] interfaces();
    }

    public static void cache(File cacheBase, CacheBuilder builder, String path, CacheableClass target, String name) throws IOException {
        JsonObject object = builder.cacheClass(target);
        File cacheTarget = new File(cacheBase, path + "/" + name + ".scrc");
        if (!cacheTarget.exists()) {
            cacheTarget.getParentFile().mkdirs();
            cacheTarget.createNewFile();
        }
        FileWriter writer = new FileWriter(cacheTarget);
        writer.write(GsonHelper.GSON.toJson(object));
        writer.close();
    }

    public static class ErrorLogger {
        private final String[] lines;
        private final String fileLoc;
        private final LocationAnalyser finder;

        public ErrorLogger(String[] lines, String fileLoc) {
            this.lines = lines;
            this.fileLoc = fileLoc;
            finder = new LocationAnalyser();
        }

        public void error(Token loc, String msg) {
            if (errorCount++ < 100) {
                Compiler.error(loc, msg, fileLoc, lines[loc.line() - 1]);
            }
        }

        public void errorF(Token loc, String format, Object... args) {
            error(loc, String.format(format, args));
        }

        public void error(int lineIndex, int lineStartIndex, String msg) {
            if (errorCount++ < 100) Compiler.error(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
        }

        public void error(Stmt loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void error(Expr loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void logError(String s) {
            System.err.println(s);
        }

        public void warn(int lineIndex, int lineStartIndex, String msg) {
            Compiler.warn(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
        }

        public void warn(Token loc, String msg) {
            Compiler.warn(loc, msg, fileLoc, lines[loc.line()]);
        }

        public void warn(Stmt loc, String msg) {
            warn(finder.find(loc), msg);
        }

        @Override
        public String toString() {
            return "ErrorLogger for '" + fileLoc + "' (errorCount: " + errorCount + ")";
        }

        public boolean hadError() {
            return errorCount > 0;
        }
    }
    public static void error(Token token, String message, String fileId, String line) {
        error(token.line(), token.lineStartIndex(), message, fileId, line);
    }

    public static void error(int lineIndex, int lineStartIndex, String msg, String fileId, String line) {
        report(System.err, lineIndex, msg, fileId, lineStartIndex, line);
    }

    public static void warn(Token token, String msg, String fileId, String line) {
        warn(token.line(), token.lineStartIndex(), msg, fileId, line);
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
        CONSTRUCT(CompilerLoaderHolder::construct),
        LOAD(CompilerLoaderHolder::loadClass);

        private final Consumer<CompilerLoaderHolder> action;

        Stage(Consumer<CompilerLoaderHolder> action) {
            this.action = action;
        }
    }
}