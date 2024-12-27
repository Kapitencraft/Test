package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record AnnotationDecl(
        VarTypeParser parser,
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Token name, String pck,
        SkeletonParser.AnnotationMethodDecl[] annotationMethods,
        SkeletonParser.ClassConstructor<?>[] enclosed,
        SkeletonParser.AnnotationObj[] annotations
) implements SkeletonParser.ClassConstructor<BakedAnnotation> {

    @Override
    public BakedAnnotation construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Compiler.ClassBuilder> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassConstructor<?> classDecl : enclosed()) {
            enclosed.add(classDecl.construct(stmtParser, exprParser));
        }

        ImmutableMap.Builder<String, MethodWrapper> methods = new ImmutableMap.Builder<>();
        for (SkeletonParser.AnnotationMethodDecl method : annotationMethods()) {
            Expr val = null;
            if (method.defaulted()) {
                exprParser.apply(method.body(), this.parser);
                val = exprParser.literalOrReference();
            }
            methods.put(method.name().lexeme(), new MethodWrapper(val, method.target()));
        }

        return new BakedAnnotation(
                this.logger(),
                this.target(),
                this.name(),
                this.pck(),
                methods.build(),
                enclosed.toArray(new Compiler.ClassBuilder[0])
        );
    }

    public record MethodWrapper(@Nullable Expr val, LoxClass type) {

    }

    @Override
    public LoxClass createSkeleton() {
        //enclosed classes
        ImmutableMap.Builder<String, PreviewClass> enclosed = new ImmutableMap.Builder<>();
        for (SkeletonParser.ClassConstructor<?> enclosedDecl : this.enclosed()) {
            LoxClass generated = enclosedDecl.createSkeleton();
            enclosedDecl.target().apply(generated);
            enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
        }

        ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();
        for (SkeletonParser.AnnotationMethodDecl method : annotationMethods()) {
            methods.put(method.name().lexeme(), new SkeletonAnnotationMethod(method.target(), method.body().length > 0));
        }

        return new SkeletonAnnotation(
                this.name().lexeme(),
                this.pck(),
                enclosed.build(),
                methods.build()
        );
    }
}
