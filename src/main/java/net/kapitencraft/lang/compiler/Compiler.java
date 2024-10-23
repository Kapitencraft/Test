package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.compiler.visitor.LocationFinder;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.clazz.GeneratedLoxClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.SkeletonClass;
import net.kapitencraft.lang.run.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
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
    }

    public static void main(String[] args) throws IOException {
        File root = new File("./run/src");
        File cache = new File("./run/cache");
        List<File> source = Util.listResources(root, ".scr");

        CacheBuilder builder = new CacheBuilder();
        System.out.println("Compiling...");

        ClassLoader.PackageHolder holder = ClassLoader.load(root, ".scr");
        ClassLoader.applyPreviews(holder);

        for (File file : source) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String val = reader.lines().collect(Collectors.joining("\n"));
            ErrorLogger logger = new ErrorLogger(val.split("\n", Integer.MAX_VALUE), file.getAbsolutePath().replace(".\\", ""));
            String path = file.getParentFile().getPath().substring(10).replace(".scr", "");
            String absoluteName = file.getName().replace(".scr", "");

            GeneratedLoxClass loxClass = compile(val, logger, path.replace('\\', '.'), absoluteName);
            cache(cache, builder, path.replace('\\', '/'), loxClass, absoluteName);
        }
    }

    private static void cache(File cache, CacheBuilder builder, String path, GeneratedLoxClass target, String name) throws IOException {
        JsonObject object = builder.cacheClass(target);
        File cacheTarget = new File(cache, path + "/" + name + ".scrc");
        if (!cacheTarget.exists()) {
            cacheTarget.getParentFile().mkdirs();
            cacheTarget.createNewFile();
        }
        FileWriter writer = new FileWriter(cacheTarget);
        writer.write(GsonHelper.GSON.toJson(object));
        writer.close();
        for (GeneratedLoxClass loxClass : target.enclosing()) {
            cache(cache, builder, path, loxClass, name + "$" + loxClass.name());
        }
    }

    public static GeneratedLoxClass compile(String source, ErrorLogger logger, String pck, String fileName) {
        Lexer lexer = new Lexer(source, logger);
        List<Token> tokens = lexer.scanTokens();

        SkeletonParser parser = new SkeletonParser(logger, fileName);
        VarTypeParser varTypeParser = new VarTypeParser();

        parser.apply(tokens.toArray(new Token[0]), varTypeParser);

        SkeletonParser.ClassDecl decl = parser.parse();

        if (!Objects.equals(decl.pck(), pck)) {
            logger.error(
                    tokens.get(0),
                    "package path '" + decl.pck() + "' does not match file path '" + pck + "'");
        }

        BakedClass baked = compileClass(logger, decl, varTypeParser, decl.pck() + ".");

        // Stop if there was a syntax error.
        if (hadError) System.exit(65);

        return generateClass(baked, logger);
    }

    private static BakedClass compileClass(ErrorLogger logger, SkeletonParser.ClassDecl decl, VarTypeParser varTypeParser, String pck) {
        StmtParser stmtParser = new StmtParser(logger);
        ExprParser exprParser = new ExprParser(logger);

        SkeletonClass skeletonClass = SkeletonClass.create(logger, decl);
        decl.target().apply(skeletonClass);

        List<Stmt.VarDecl> fields = new ArrayList<>();
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : decl.fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), varTypeParser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            if (field.isStatic()) staticFields.add(fieldDecl);
            else fields.add(fieldDecl);
        }

        List<BakedClass> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassDecl classDecl : decl.enclosed()) {
            BakedClass loxClass = compileClass(logger, classDecl, varTypeParser, pck + decl.name().lexeme() + "$");
            enclosed.add(loxClass);
        }

        List<Stmt.FuncDecl> methods = new ArrayList<>();
        List<Stmt.FuncDecl> staticMethods = new ArrayList<>();
        List<Stmt.FuncDecl> abstracts = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : decl.methods()) {
            List<Stmt> body = null;
            if (!method.isAbstract()) {
                stmtParser.apply(method.body(), varTypeParser);
                stmtParser.applyMethod(method.params(), skeletonClass, method.type());
                body = stmtParser.parse();
                stmtParser.popMethod();
            }
            Stmt.FuncDecl methodDecl = new Stmt.FuncDecl(method.type(), method.name(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(methodDecl);
            else if (method.isAbstract()) abstracts.add(methodDecl);
            else methods.add(methodDecl);
        }

        List<Stmt.FuncDecl> constructors = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : decl.constructors()) {
            stmtParser.apply(method.body(), varTypeParser);
            stmtParser.applyMethod(method.params(), skeletonClass, VarTypeManager.VOID);
            List<Stmt> body = stmtParser.parse();
            Stmt.FuncDecl constDecl = new Stmt.FuncDecl(method.type(), method.name(), method.params(), body, method.isFinal(), method.isAbstract());
            stmtParser.popMethod();
            constructors.add(constDecl);
        }

        return new BakedClass(
                decl.target(),
                abstracts.toArray(new Stmt.FuncDecl[0]),
                methods.toArray(new Stmt.FuncDecl[0]),
                staticMethods.toArray(new Stmt.FuncDecl[0]),
                constructors.toArray(new Stmt.FuncDecl[0]),
                fields.toArray(new Stmt.VarDecl[0]),
                staticFields.toArray(new Stmt.VarDecl[0]),
                decl.superclass(),
                decl.name(),
                pck,
                enclosed.toArray(new BakedClass[0]),
                decl.isAbstract(),
                decl.isFinal()
        );
    }

    private static GeneratedLoxClass generateClass(BakedClass baked, ErrorLogger logger) {
        ImmutableMap.Builder<String, GeneratedLoxClass> enclosed = new ImmutableMap.Builder<>();
        for (int i = 0; i < baked.enclosed().length; i++) {
            GeneratedLoxClass loxClass = generateClass(baked.enclosed()[i], logger);
            enclosed.put(loxClass.name(), loxClass);
        }

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Stmt.FuncDecl method : baked.methods()) {
            methods.putIfAbsent(method.name.lexeme(), new DataMethodContainer.Builder(baked.name()));
            DataMethodContainer.Builder builder = methods.get(method.name.lexeme());
            builder.addMethod(logger, new GeneratedCallable(method), method.name);
        }

        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (Stmt.FuncDecl method : baked.staticMethods()) {
            staticMethods.putIfAbsent(method.name.lexeme(), new DataMethodContainer.Builder(baked.name()));
            DataMethodContainer.Builder builder = staticMethods.get(method.name.lexeme());
            builder.addMethod(logger, new GeneratedCallable(method), method.name);
        }

        Map<String, GeneratedField> fields = generateFields(baked.fields());

        List<String> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            if (field.isFinal() && !field.hasInit()) {
                finalFields.add(name);
            }
        });

        ConstructorContainer.Builder container = new ConstructorContainer.Builder(finalFields, baked.name());
        for (Stmt.FuncDecl method : baked.constructors()) {
            container.addMethod(logger, new GeneratedCallable(method), method.name);
        }


        GeneratedLoxClass loxClass = new GeneratedLoxClass(
                DataMethodContainer.bakeBuilders(methods), DataMethodContainer.bakeBuilders(staticMethods),
                container,
                fields, generateFields(baked.staticFields()),
                baked.superclass(),
                baked.name().lexeme(),
                enclosed.build(),
                baked.pck(),
                baked.isAbstract(),
                baked.isFinal()
        );
        baked.target().apply(loxClass);
        return loxClass;
    }

    private static Map<String, GeneratedField> generateFields(Stmt.VarDecl[] declarations) {
        return Arrays.stream(declarations).collect(Collectors.toMap(dec -> dec.name.lexeme(), GeneratedField::new));
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

    public record BakedClass(PreviewClass target, Stmt.FuncDecl[] abstracts, Stmt.FuncDecl[] methods, Stmt.FuncDecl[] staticMethods, Stmt.FuncDecl[] constructors, Stmt.VarDecl[] fields, Stmt.VarDecl[] staticFields, LoxClass superclass, Token name, String pck, BakedClass[] enclosed, boolean isAbstract, boolean isFinal) implements ClassBuilder<BakedClass> {

        @Override
        public LoxClass build(BakedClass in, ErrorLogger logger) {
            return null;
        }
    }

    public record BakedInterface(PreviewClass target, Stmt.FuncDecl[] abstracts, Stmt.FuncDecl[] methods, Stmt.FuncDecl[] staticMethods, Stmt.VarDecl[] staticFields, LoxClass[] parentInterfaces, Token name, String pck) implements ClassBuilder<BakedInterface> {

        @Override
        public LoxClass build(BakedInterface in, ErrorLogger logger) {
            return null;
        }
    }

    public record BakedEnum(PreviewClass target, Stmt.FuncDecl[] abstracts, Stmt.FuncDecl[] methods, Stmt.FuncDecl[] staticMethods, Stmt.VarDecl[] fields, Stmt.VarDecl[] staticFields, LoxClass[] interfaces, Token name, String pck, BakedClass[] enclosed) implements ClassBuilder<BakedEnum> {

        @Override
        public LoxClass build(BakedEnum in, ErrorLogger logger) {
            return null;
        }
    }

    public interface ClassBuilder<T extends ClassBuilder<T>> {
        LoxClass build(T in, ErrorLogger logger);
    }
}