package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
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

public record AnnotationDecl(
        VarTypeParser parser,
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Token name, String pck,
        SkeletonParser.MethodDecl[] methods,
        SkeletonParser.FieldDecl[] fields,
        SkeletonParser.ClassConstructor<?>[] enclosed
) implements SkeletonParser.ClassConstructor<BakedAnnotation> {

    @Override
    public BakedAnnotation construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), parser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            staticFields.add(fieldDecl);
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
                stmtParser.applyMethod(method.params(), target(), VarTypeManager.ANNOTATION.get(), method.type());
                body = stmtParser.parse();
                stmtParser.popMethod();
            }
            GeneratedCallable methodDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(Pair.of(method.name(), methodDecl));
            else methods.add(Pair.of(method.name(), methodDecl));
        }

        return new BakedAnnotation(
                this.logger(),
                this.target(),
                methods.toArray(new Pair[0]),
                staticMethods.toArray(new Pair[0]),
                staticFields.toArray(new Stmt.VarDecl[0]),
                this.name(),
                this.pck(),
                enclosed.toArray(new Compiler.ClassBuilder[0])
        );
    }

    @Override
    public LoxClass createSkeleton() {

        //fields
        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        for (SkeletonParser.FieldDecl field : this.fields()) {
            SkeletonField skeletonField = new SkeletonField(field.type(), field.isFinal());
            staticFields.put(field.name().lexeme(), skeletonField);

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

        return new SkeletonAnnotation(
                this.name().lexeme(),
                this.pck(),
                staticFields.build(),
                enclosed.build(),
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods)
        );
    }
}
