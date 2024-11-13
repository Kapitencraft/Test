package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.SkeletonClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClassDecl(
        VarTypeParser parser,
        Compiler.ErrorLogger logger,
        boolean isAbstract, boolean isFinal,
        PreviewClass target,
        Token name, String pck, LoxClass superclass,
        LoxClass[] implemented,
        SkeletonParser.MethodDecl[] constructors,
        SkeletonParser.MethodDecl[] methods,
        SkeletonParser.FieldDecl[] fields,
        SkeletonParser.ClassConstructor<?>[] enclosed
) implements SkeletonParser.ClassConstructor<BakedClass> {

    @Override
    public BakedClass construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Stmt.VarDecl> fields = new ArrayList<>();
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), parser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            if (field.isStatic()) staticFields.add(fieldDecl);
            else fields.add(fieldDecl);
        }

        List<Compiler.ClassBuilder> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassConstructor<?> classDecl : enclosed()) {
            enclosed.add(classDecl.construct(stmtParser, exprParser));
        }

        List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
        List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : this.methods()) {
            List<Stmt> body = null;
            if (!method.isAbstract()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), superclass(), method.type());
                body = stmtParser.parse();
                stmtParser.popMethod();
            }
            GeneratedCallable methodDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(Pair.of(method.name(), methodDecl));
            else methods.add(Pair.of(method.name(), methodDecl));
        }

        List<Pair<Token, GeneratedCallable>> constructors = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : this.constructors()) {
            stmtParser.apply(method.body(), parser);
            stmtParser.applyMethod(method.params(), target(), superclass(), VarTypeManager.VOID);
            List<Stmt> body = stmtParser.parse();
            GeneratedCallable constDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            stmtParser.popMethod();
            constructors.add(Pair.of(method.name(), constDecl));
        }

        return new BakedClass(
                this.logger(),
                this.target(),
                methods.toArray(new Pair[0]),
                staticMethods.toArray(new Pair[0]),
                constructors.toArray(new Pair[0]),
                fields.toArray(new Stmt.VarDecl[0]),
                staticFields.toArray(new Stmt.VarDecl[0]),
                this.superclass(),
                this.name(),
                this.pck(),
                this.implemented(),
                enclosed.toArray(new Compiler.ClassBuilder[0]),
                this.isAbstract(),
                this.isFinal()
        );
    }

    @Override
    public LoxClass createSkeleton() {

        //fields
        ImmutableMap.Builder<String, LoxClass> fields = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<String, LoxClass> staticFields = new ImmutableMap.Builder<>();
        List<String> finalFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : this.fields()) {
            if (field.isStatic()) staticFields.put(field.name().lexeme(), field.type());
            else {
                fields.put(field.name().lexeme(), field.type());
                if (field.isFinal() && field.body() == null) //add non-defaulted final fields to extra list to check constructors init
                    finalFields.add(field.name().lexeme());
            }
        }

        //enclosed classes
        ImmutableMap.Builder<String, PreviewClass> enclosed = new ImmutableMap.Builder<>();
        for (SkeletonParser.ClassConstructor<?> enclosedDecl : this.enclosed()) {
            LoxClass generated = enclosedDecl.createSkeleton();
            enclosedDecl.target().apply(generated);
            enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
        }

        //methods
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (SkeletonParser.MethodDecl method : this.methods()) {
            if (method.isStatic()) {
                staticMethods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = staticMethods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            } else {
                methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            }
        }

        //constructors
        ConstructorContainer.Builder constructorBuilder = new ConstructorContainer.Builder(finalFields, this.name());
        for (SkeletonParser.MethodDecl constructor : this.constructors()) {
            constructorBuilder.addMethod(
                    logger,
                    SkeletonMethod.create(constructor),
                    constructor.name()
            );
        }

        return new SkeletonClass(
                this.name().lexeme(),
                this.pck(), this.superclass(),
                staticFields.build(),
                fields.build(),
                enclosed.build(),
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                constructorBuilder,
                this.isAbstract(),
                this.isFinal()
        );
    }
}
