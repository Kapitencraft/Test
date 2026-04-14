package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.annotation.CompileAnnotationCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.Map;

public record BakedAnnotation(
        ClassReference target,
        Token name, String pck,
        Map<String, Holder.Class.MethodWrapper> methodWrappers,
        Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {

        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        methodWrappers.forEach((string, wrapper) -> builder.put(string, new DataMethodContainer(new ScriptedCallable[]{new CompileAnnotationCallable(wrapper.retType(), wrapper.val(), wrapper.annotations())})));

        return new CompileClass(
                builder.build(), Map.of(), VarTypeManager.OBJECT,
                this.name().lexeme(),
                this.pck(), new ClassReference[0], Modifiers.ANNOTATION, this.annotations());
    }

    @Override
    public ClassReference superclass() {
        return null;
    }

    @Override
    public Pair<Token, CompileCallable>[] methods() {
        return new Pair[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }
}
