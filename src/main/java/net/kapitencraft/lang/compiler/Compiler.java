package net.kapitencraft.lang.compiler;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.visitor.LocationFinder;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.load.CompilerLoaderHolder;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;

import java.io.*;

public class Compiler {
    static boolean hadError = false;

    public static class ErrorLogger {
        private boolean hadError = false;
        private final String[] lines;
        private final String fileLoc;
        private final LocationFinder finder;

        public ErrorLogger(String[] lines, String fileLoc) {
            this.lines = lines;
            this.fileLoc = fileLoc;
            finder = new LocationFinder();
        }

        public void error(Token loc, String msg) {
            hadError = true;
            Compiler.error(loc, msg, fileLoc, lines[loc.line() - 1]);
        }

        public void errorF(Token loc, String format, Object... args) {
            hadError = true;
            error(loc, String.format(format, args));
        }

        public void error(int lineIndex, int lineStartIndex, String msg) {
            hadError = true;
            Compiler.error(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
        }

        public void error(CompileStmt loc, String msg) {
            hadError = true;
            error(finder.find(loc), msg);
        }

        public void error(CompileExpr loc, String msg) {
            hadError = true;
            error(finder.find(loc), msg);
        }

        public void logError(String s) {
            hadError = true;
            System.err.println(s);
        }

        public void warn(int lineIndex, int lineStartIndex, String msg) {
            Compiler.warn(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
        }

        public void warn(Token loc, String msg) {
            Compiler.warn(loc, msg, fileLoc, lines[loc.line()]);
        }

        public void warn(CompileStmt loc, String msg) {
            warn(finder.find(loc), msg);
        }

        @Override
        public String toString() {
            return "ErrorLogger for '" + fileLoc + "' (hadError: " + hadError + ")";
        }

        public boolean hadError() {
            return hadError;
        }
    }

    public static void main(String[] args) {
        File root = new File("./run/src");
        File cache = ClassLoader.cacheLoc;

        System.out.println("Compiling...");

        ClassLoader.PackageHolder<CompilerLoaderHolder> holder = ClassLoader.load(root, ".scr", CompilerLoaderHolder::new);

        ClassLoader.useHolders(holder, (s, classHolder) -> classHolder.applyConstructor());

        ClassLoader.generateSkeletons(holder);

        ClassLoader.useHolders(holder, (s, compilerLoaderHolder) -> compilerLoaderHolder.validate());

        ClassLoader.useHolders(holder, (s, classHolder) -> classHolder.construct());

        ClassLoader.generateClasses(holder);

        if (hadError) System.exit(65);

        if (cache.exists()) Util.delete(cache);

        System.out.println("Caching...");

        CacheBuilder builder = new CacheBuilder();
        ClassLoader.useClasses(holder, (stringClassHolderMap, aPackage) ->
                stringClassHolderMap.values().forEach(classHolder -> classHolder.cache(builder))
        );

        if (hadError) System.exit(65);
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
        for (ClassReference loxClass : target.enclosed()) {
            cache(cacheBase, builder, path, (CacheableClass) loxClass.get(), name + "$" + loxClass.get().name());
        }
    }

    public static void error(Token token, String message, String fileId, String line) {
        error(token.line(), token.lineStartIndex(), message, fileId, line);
    }

    public static void error(int lineIndex, int lineStartIndex, String msg, String fileId, String line) {
        report(System.err, lineIndex, msg, fileId, lineStartIndex, line, true);
    }

    public static void warn(Token token, String msg, String fileId, String line) {
        warn(token.line(), token.lineStartIndex(), msg, fileId, line);
    }

    public static void warn(int lineIndex, int lineStartIndex, String msg, String filedId, String line) {
        System.out.print("\u001B[33m"); //set output color to yellow
        report(System.out, lineIndex, msg, filedId, lineStartIndex, line, false);
        System.out.print("\u001B[0m"); //reset output color
    }

    public static void report(PrintStream target, int lineIndex, String message, String fileId, int startIndex, String line, boolean setHasError) {
        target.print(fileId);
        target.print(":");
        target.print(lineIndex);
        target.print(": ");
        target.println(message);

        target.println(line);
        for (int i = 0; i < startIndex; i++) {
            target.print(" ");
        }
        target.println("^");

        hadError |= setHasError;
    }

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, GeneratedCallable>[] methods();

        ClassReference[] interfaces();
    }
}