package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
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
        ClassReference target,
        Pair<Token, CompileCallable>[] methods,
        Pair<Token, CompileCallable>[] staticMethods,
        Pair<Token, CompileCallable>[] constructors,
        Map<Token, CompileField> fields,
        Map<String, CompileField> staticFields,
        ClassReference superclass, Token name, String pck,
        ClassReference[] interfaces,
        Compiler.ClassBuilder[] enclosed,
        short modifiers,
        CompileAnnotationClassInstance[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {
        CacheableClass[] enclosed = Arrays.stream(enclosed()).map(Compiler.ClassBuilder::build).toArray(CacheableClass[]::new);

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(method.left().lexeme());
            builder.addMethod(logger, method.right(), method.left());
        }

        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.staticMethods()) {
            staticMethods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = staticMethods.get(method.left().lexeme());
            builder.addMethod(logger, method.right(), method.left());
        }

        List<Token> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            if (field.isFinal() && !field.hasInit()) {
                finalFields.add(name);
            }
        });

        DataMethodContainer.Builder container = new DataMethodContainer.Builder(this.name());
        for (Pair<Token, CompileCallable> method : this.constructors()) {
            container.addMethod(logger, method.right(), method.left());
        }


        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods), DataMethodContainer.bakeBuilders(staticMethods),
                container,
                create(this.fields()),
                this.staticFields(),
                enclosed,
                this.superclass(),
                this.interfaces(),
                this.name().lexeme(), this.pck(),
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
