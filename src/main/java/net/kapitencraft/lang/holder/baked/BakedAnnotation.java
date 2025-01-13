package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.decl.AnnotationDecl;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedAnnotation;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.GeneratedAnnotationCallable;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.tool.Pair;

import java.util.Map;

public record BakedAnnotation(
        Compiler.ErrorLogger logger,
        ClassReference target,
        Token name, String pck,
        Map<String, AnnotationDecl.MethodWrapper> methodWrappers,
        Compiler.ClassBuilder[] enclosed
) implements Compiler.ClassBuilder {

    @Override
    public GeneratedAnnotation build() {
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (Compiler.ClassBuilder builder : enclosed()) {
            ClassReference loxClass = builder.build().reference();
            enclosed.put(loxClass.name(), loxClass);
        }

        ImmutableMap.Builder<String, AnnotationCallable> builder = new ImmutableMap.Builder<>();
        methodWrappers.forEach((string, wrapper) -> builder.put(string, new GeneratedAnnotationCallable(wrapper.type(), wrapper.val())));

        GeneratedAnnotation loxClass = new GeneratedAnnotation(
                enclosed.build(), methodWrappers,
                this.name().lexeme(),
                this.pck());
        this.target().setTarget(loxClass);
        return loxClass;
    }

    @Override
    public ClassReference superclass() {
        return null;
    }

    @Override
    public Pair<Token, GeneratedCallable>[] methods() {
        return new Pair[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }
}
