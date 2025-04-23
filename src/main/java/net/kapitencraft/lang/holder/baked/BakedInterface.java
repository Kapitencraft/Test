package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
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
import java.util.List;
import java.util.Map;

public record BakedInterface(Compiler.ErrorLogger logger, ClassReference target, Pair<Token, CompileCallable>[] methods, Pair<Token, CompileCallable>[] staticMethods, Map<String, CompileField> staticFields, ClassReference[] interfaces, Token name, String pck, Compiler.ClassBuilder[] enclosed, CompileAnnotationClassInstance[] annotations) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {
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


        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                List.of(),
                Map.of(),
                staticFields(),
                VarTypeManager.OBJECT,
                name().lexeme(),
                pck(),
                enclosed,
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