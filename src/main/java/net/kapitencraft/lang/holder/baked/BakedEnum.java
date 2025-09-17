package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.CompileEnum;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.CompileEnumConstant;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BakedEnum(
        Compiler.ErrorLogger logger,
        ClassReference target,
        Pair<Token, CompileCallable>[] constructors,
        Pair<Token, CompileCallable>[] methods,
        Pair<Token, CompileCallable>[] staticMethods,
        ClassReference[] interfaces,
        Map<String, CompileEnumConstant> constants,
        Map<Token, CompileField> fields, Map<String, CompileField> staticFields,
        Token name, String pck, Compiler.ClassBuilder[] enclosed,
        CompileAnnotationClassInstance[] annotations
        ) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (int i = 0; i < this.enclosed().length; i++) {
            ClassReference loxClass = this.enclosed()[i].build().reference();
            enclosed.put(loxClass.name(), loxClass);
        }

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


        return new CompileEnum(
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                container,
                BakedClass.create(fields),
                constants(),
                this.staticFields(),
                enclosed.build(),
                interfaces(),
                name().lexeme(),
                pck(), annotations());
    }

    @Override
    public ClassReference superclass() {
        return VarTypeManager.ENUM;
    }
}

