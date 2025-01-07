package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedInterface;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.*;

public record InterfaceDecl(
        VarTypeParser parser, Compiler.ErrorLogger logger,
        ClassReference target, Token name, String pck, ClassReference[] parentInterfaces,
        SkeletonParser.MethodDecl[] methods, SkeletonParser.FieldDecl[] fields,
        SkeletonParser.ClassConstructor<?>[] enclosed,
        SkeletonParser.AnnotationObj[] annotations
) implements SkeletonParser.ClassConstructor<BakedInterface> {

    @Override
    public BakedInterface construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), parser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            if (field.isStatic()) staticFields.add(fieldDecl);
            else logger.error(fieldDecl, "fields on interfaces must be static");
        }

        List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
        List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : this.methods()) {
            List<Stmt> body = null;
            if (!method.isAbstract()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), null, method.type());
                body = stmtParser.parse();
                stmtParser.popMethod();
            }
            GeneratedCallable methodDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(Pair.of(method.name(), methodDecl));
            else methods.add(Pair.of(method.name(), methodDecl));
        }

        return new BakedInterface(
                logger, target,
                methods.toArray(new Pair[0]),
                staticMethods.toArray(new Pair[0]),
                staticFields.toArray(new Stmt.VarDecl[0]),
                parentInterfaces,
                name,
                pck,
                Arrays.stream(enclosed)
                        .map(classConstructor -> classConstructor.construct(stmtParser, exprParser))
                        .toArray(Compiler.ClassBuilder[]::new)
        );
    }

    @Override
    public LoxClass createSkeleton() {

        //fields
        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        for (SkeletonParser.FieldDecl field : this.fields()) {
            if (field.isStatic()) staticFields.put(field.name().lexeme(), new SkeletonField(field.type(), field.isFinal()));
            else {
                logger.error(field.name(), "fields inside Interfaces must always be static");
            }
        }

        //enclosed classes
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (SkeletonParser.ClassConstructor<?> enclosedDecl : this.enclosed()) {
            LoxClass generated = enclosedDecl.createSkeleton();
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

        return new SkeletonInterface(
                this.name().lexeme(),
                this.pck(),
                this.parentInterfaces(),
                staticFields.build(),
                enclosed.build(),
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods)
        );
    }
}
