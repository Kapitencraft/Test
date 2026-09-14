package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BakedClass(
        ErrorStorage logger,
        Generics generics,
        ClassReference target,
        List<Pair<Token, CompileCallable>> methods,
        List<Pair<Token, CompileCallable>> constructors,
        Map<Token, CompileField> fields,
        ClassReference superclass, Token name, String pck,
        ClassReference[] interfaces,
        int modifiers,
        Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.first().lexeme(), new DataMethodContainer.Builder(this.name()));
            methods.get(method.first().lexeme()).addMethod(logger, method.second(), method.first());
        }

        List<Token> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            //if (field.isFinal() && !field.hasInit()) {
            //    finalFields.add(name);
            //}
        });

        for (Pair<Token, CompileCallable> method : this.constructors()) {
            methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
            methods.get("<init>").addMethod(logger, method.second(), method.first());
        }

        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                create(this.fields()),
                this.superclass(),
                this.name().lexeme(),
                this.pck(),
                this.interfaces(),
                this.modifiers(),
                this.annotations()
        );
    }

    @Override
    public void analyse() {
        SemanticAnalyser analyser = new SemanticAnalyser(this.logger, this.methods::add);

        List<Pair<Token, CompileCallable>> pairs = this.methods;
        //must be iterator loop due to the nature of the analyser being able to append synthetic (lambda) methods
        for (int i = 0; i < pairs.size(); i++) {
            Pair<Token, CompileCallable> method = pairs.get(i);
            method.second().analyseSemantics(analyser, this.target);
        }
        for (Pair<Token, CompileCallable> constructor : this.constructors) {
            constructor.second().analyseSemantics(analyser, this.target);
        }
        for (CompileField value : this.fields.values()) {
            value.analyseSemantics(analyser);
        }
    }

    public static Map<String, CompileField> create(Map<Token, CompileField> fields) {
        ImmutableMap.Builder<String, CompileField> builder = new ImmutableMap.Builder<>();
        fields.forEach((token, generatedField) -> builder.put(token.lexeme(), generatedField));
        return builder.build();
    }
}
