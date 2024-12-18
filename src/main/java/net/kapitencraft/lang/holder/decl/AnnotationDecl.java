package net.kapitencraft.lang.holder.decl;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;

import java.util.ArrayList;
import java.util.List;

public record AnnotationDecl(
        VarTypeParser parser,
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Token name, String pck,
        SkeletonParser.AnnotationMethodDecl[] annotationMethods,
        SkeletonParser.ClassConstructor<?>[] enclosed
) implements SkeletonParser.ClassConstructor<BakedAnnotation> {

    @Override
    public BakedAnnotation construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Compiler.ClassBuilder> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassConstructor<?> classDecl : enclosed()) {
            enclosed.add(classDecl.construct(stmtParser, exprParser));
        }
        //TODO compile methods

        return new BakedAnnotation(
                this.logger(),
                this.target(),
                this.name(),
                this.pck(),
                enclosed.toArray(new Compiler.ClassBuilder[0])
        );
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

        return new SkeletonAnnotation(
                this.name().lexeme(),
                this.pck(),
                enclosed.build()
        );
    }
}
