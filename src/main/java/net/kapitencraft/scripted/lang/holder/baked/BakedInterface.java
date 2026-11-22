package net.kapitencraft.scripted.lang.holder.baked;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BakedInterface(ErrorStorage logger, Generics generics, ClassReference target,
                             List<Pair<Token, CompileCallable>> methods,
                             Map<String, CompileField> staticFields, ClassReference[] interfaces,
                             Token name, String pck,
                             Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.first().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(method.first().lexeme());
            builder.addMethod(logger, method.second(), method.first());
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

    @Override
    public void analyse() {
        SemanticAnalyser analyser = new SemanticAnalyser(logger, this.methods::add);

        for (Pair<Token, CompileCallable> method : this.methods) {
            method.second().analyseSemantics(analyser, this.target, method.first());
        }
        for (CompileField value : staticFields.values()) {
            value.analyseSemantics(analyser);
        }
    }
}