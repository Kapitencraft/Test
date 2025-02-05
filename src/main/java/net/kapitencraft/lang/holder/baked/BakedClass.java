package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.*;

public record BakedClass(
        Compiler.ErrorLogger logger,
        ClassReference target,
        Pair<Token, GeneratedCallable>[] methods,
        Pair<Token, GeneratedCallable>[] staticMethods,
        Pair<Token, GeneratedCallable>[] constructors,
        Map<String, GeneratedField> fields,
        Map<String, GeneratedField> staticFields,
        ClassReference superclass, Token name, String pck,
        ClassReference[] interfaces,
        Compiler.ClassBuilder[] enclosed,
        short modifiers,
        AnnotationClassInstance[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public GeneratedClass build() {
        ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
        for (int i = 0; i < this.enclosed().length; i++) {
            ClassReference loxClass = this.enclosed()[i].build().reference();
            enclosed.put(loxClass.name(), loxClass);
        }

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, GeneratedCallable> method : this.methods()) {
            methods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(method.left().lexeme());
            builder.addMethod(logger, method.right(), method.left());
        }

        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (Pair<Token, GeneratedCallable> method : this.staticMethods()) {
            staticMethods.putIfAbsent(method.left().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = staticMethods.get(method.left().lexeme());
            builder.addMethod(logger, method.right(), method.left());
        }

        List<String> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            if (field.isFinal() && !field.hasInit()) {
                finalFields.add(name);
            }
        });

        ConstructorContainer.Builder container = new ConstructorContainer.Builder(finalFields, this.name());
        for (Pair<Token, GeneratedCallable> method : this.constructors()) {
            container.addMethod(logger, method.right(), method.left());
        }


        GeneratedClass loxClass = new GeneratedClass(
                DataMethodContainer.bakeBuilders(methods), DataMethodContainer.bakeBuilders(staticMethods),
                container,
                fields(),
                staticFields(),
                enclosed.build(),
                this.superclass(),
                this.interfaces(),
                this.name().lexeme(),
                this.pck(),
                this.modifiers(),
                this.annotations()
        );
        this.target().setTarget(loxClass);
        return loxClass;
    }
}
