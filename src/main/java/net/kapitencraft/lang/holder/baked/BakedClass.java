package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.*;

public record BakedClass(
        Compiler.ErrorLogger logger,
        Holder.Generics generics,
        ClassReference target,
        Pair<Token, CompileCallable>[] methods,
        Pair<Token, CompileCallable>[] constructors,
        Map<Token, CompileField> fields,
        ClassReference superclass, Token name, String pck,
        ClassReference[] interfaces,
        short modifiers,
        CompileAnnotationClassInstance[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            methods.get(method.left().lexeme()).addMethod(logger, method.right(), method.left());
        }

        List<Token> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            //if (field.isFinal() && !field.hasInit()) {
            //    finalFields.add(name);
            //}
        });

        for (Pair<Token, CompileCallable> method : this.constructors()) {
            methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
            methods.get("<init>").addMethod(logger, method.right(), method.left());
        }

        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                create(this.fields()),
                this.superclass(),
                this.pck(),
                this.name().lexeme(),
                this.interfaces(),
                this.modifiers(),
                this.annotations()
        );
    }

    public static Map<String, CompileField> create(Map<Token, CompileField> fields) {
        ImmutableMap.Builder<String, CompileField> builder = new ImmutableMap.Builder<>();
        fields.forEach((token, generatedField) -> builder.put(token.lexeme(), generatedField));
        return builder.build();
    }
}
