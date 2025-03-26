package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.method.annotation.GeneratedAnnotationCallable;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.List;
import java.util.Map;

public record BakedAnnotation(
        ClassReference target,
        Token name, String pck,
        Map<String, Holder.Class.MethodWrapper> methodWrappers,
        Compiler.ClassBuilder[] enclosed,
        AnnotationClassInstance[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public GeneratedClass build() {
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (Compiler.ClassBuilder builder : enclosed()) {
            ClassReference loxClass = builder.build().reference();
            enclosed.put(loxClass.name(), loxClass);
        }

        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        methodWrappers.forEach((string, wrapper) -> builder.put(string, new DataMethodContainer(new ScriptedCallable[]{new GeneratedAnnotationCallable(wrapper.type(), wrapper.val(), wrapper.annotations())})));

        GeneratedClass loxClass = new GeneratedClass(
                builder.build(), Map.of(), List.of(), Map.of(), Map.of(), VarTypeManager.OBJECT,
                this.name().lexeme(),
                this.pck(), enclosed.build(), new ClassReference[0], Modifiers.ANNOTATION, this.annotations());
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
