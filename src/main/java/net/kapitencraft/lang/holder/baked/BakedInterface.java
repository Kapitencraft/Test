package net.kapitencraft.lang.holder.baked;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public record BakedInterface(Compiler.ErrorLogger logger, Holder.Generics generics, ClassReference target,
                             Pair<Token, CompileCallable>[] methods,
                             Map<String, CompileField> staticFields, ClassReference[] interfaces,
                             Token name, String pck,
                             CompileAnnotationClassInstance[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(method.left().lexeme());
            builder.addMethod(logger, method.right(), method.left());
        }

        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                staticFields(),
                VarTypeManager.OBJECT,
                name().lexeme(),
                pck(),
                interfaces(),
                Modifiers.INTERFACE,
                annotations()
        );
    }

    @Override
    public ClassReference superclass() {
        return null;
    }
}