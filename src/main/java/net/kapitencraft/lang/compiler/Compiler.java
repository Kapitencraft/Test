package net.kapitencraft.lang.compiler;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.visitor.LocationFinder;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.load.CompilerHolder;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler {
    static boolean hadError = false;

    public static class ErrorLogger {
        private final String[] lines;
        private final String fileLoc;
        private final LocationFinder finder;

        public ErrorLogger(String[] lines, String fileLoc) {
            this.lines = lines;
            this.fileLoc = fileLoc;
            finder = new LocationFinder();
        }

        public void error(Token loc, String msg) {
            Compiler.error(loc, msg, fileLoc, lines[loc.line() - 1]);
        }

        public void errorF(Token loc, String format, Object... args) {
            error(loc, String.format(format, args));
        }

        public void error(int lineIndex, int lineStartIndex, String msg) {
            Compiler.error(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
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

        public void warn(Token loc, String msg) {
            Compiler.warn(loc, msg, fileLoc, lines[loc.line()]);
        }

        public void warn(Stmt loc, String msg) {
            warn(finder.find(loc), msg);
        }

        @Override
        public String toString() {
            return "ErrorLogger for '" + fileLoc + "'";
        }
    }

    public static void main(String[] args) {
        File root = new File("./run/src");
        File cache = ClassLoader.cacheLoc;

        System.out.println("Compiling...");

        ClassLoader.PackageHolder holder = ClassLoader.load(root, ".scr", CompilerHolder::new);
        ClassLoader.applyPreviews(holder);

        ClassLoader.useHolders(holder, (s, classHolder) -> ((CompilerHolder) classHolder).applyConstructor());

        ClassLoader.generateSkeletons(holder);

        ClassLoader.useHolders(holder, (s, classHolder) -> ((CompilerHolder) classHolder).construct());

        ClassLoader.generateClasses(holder);

        if (hadError) System.exit(65);

        if (cache.exists()) Util.delete(cache);

        CacheBuilder builder = new CacheBuilder();
        ClassLoader.useClasses(holder, (stringClassHolderMap, aPackage) ->
                stringClassHolderMap.values().forEach(classHolder -> ((CompilerHolder) classHolder).cache(builder))
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
        for (CacheableClass loxClass : target.enclosed()) {
            cache(cacheBase, builder, path, loxClass, name + "$" + loxClass.name());
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
        ErrorLogger logger();

        CacheableClass build();

        static Map<String, GeneratedField> generateFields(Stmt.VarDecl[] declarations) {
            return Arrays.stream(declarations).collect(Collectors.toMap(dec -> dec.name.lexeme(), decl -> new GeneratedField(decl.type, decl.initializer, decl.isFinal)));
        }

        LoxClass superclass();

        Token name();

        Pair<Token, GeneratedCallable>[] methods();

        LoxClass[] interfaces();
    }
}