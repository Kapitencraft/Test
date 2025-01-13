package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedEnum;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonEnum;
import net.kapitencraft.lang.oop.field.GeneratedEnumConstant;
import net.kapitencraft.lang.oop.field.SkeletonField;
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

public record EnumDecl(VarTypeParser parser, Compiler.ErrorLogger logger,
                       ClassReference target, Token name, String pck,
                       ClassReference[] interfaces,
                       SkeletonParser.EnumConstDecl[] constants,
                       SkeletonParser.MethodDecl[] constructors,
                       SkeletonParser.MethodDecl[] methods,
                       SkeletonParser.FieldDecl[] fields,
                       SkeletonParser.ClassConstructor<?>[] enclosed,
                       SkeletonParser.AnnotationObj[] annotations
) implements SkeletonParser.ClassConstructor<BakedEnum> {

    @Override
    public BakedEnum construct(StmtParser stmtParser, ExprParser exprParser) {
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
                stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, method.type());
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
            stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, ClassReference.of(VarTypeManager.VOID));
            List<Stmt> body = stmtParser.parse();
            GeneratedCallable constDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            stmtParser.popMethod();
            constructors.add(Pair.of(method.name(), constDecl));
        }


        ImmutableMap.Builder<String, GeneratedEnumConstant> enumConstants = new ImmutableMap.Builder<>();
        for (SkeletonParser.EnumConstDecl decl : constants) {
            List<Expr> args;
            if (decl.args().length == 0) {
                args = new ArrayList<>();
                exprParser.apply(new Token[0], parser);
            } else {
                exprParser.apply(decl.args(), parser);
                args = exprParser.args();
            }

            int ordinal = target.get().getConstructor().getMethodOrdinal(exprParser.argTypes(args));
            ScriptedCallable callable = target.get().getConstructor().getMethodByOrdinal(ordinal);

            exprParser.checkArguments(args, callable, decl.name());

            enumConstants.put(decl.name().lexeme(), new GeneratedEnumConstant(target.get(), decl.ordinal(), decl.name().lexeme(), ordinal, args));
        }


        return new BakedEnum(
                logger(),
                target(),
                constructors.toArray(Pair[]::new),
                methods.toArray(Pair[]::new),
                staticMethods.toArray(Pair[]::new),
                interfaces(),
                enumConstants.build(),
                fields.toArray(Stmt.VarDecl[]::new),
                staticFields.toArray(Stmt.VarDecl[]::new),
                name(),
                pck(),
                enclosed.toArray(new Compiler.ClassBuilder[0])
        );
    }

    @Override
    public ScriptedClass createSkeleton() {
        ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        List<String> finalFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : this.fields()) {
            SkeletonField skeletonField = new SkeletonField(field.type(), field.isFinal());
            if (field.isStatic()) staticFields.put(field.name().lexeme(), skeletonField);
            else {
                fields.put(field.name().lexeme(), skeletonField);
                if (skeletonField.isFinal() && field.body() == null) //add non-defaulted final fields to extra list to check constructors init
                    finalFields.add(field.name().lexeme());
            }
        }

        //enclosed classes
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (SkeletonParser.ClassConstructor<?> enclosedDecl : this.enclosed()) {
            ScriptedClass generated = enclosedDecl.createSkeleton();
            enclosedDecl.target().setTarget(generated);
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


        return new SkeletonEnum(
                name().lexeme(), pck(),
                staticFields.build(), fields.build(),
                enclosed.build(),
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                constructorBuilder
        );
    }
}