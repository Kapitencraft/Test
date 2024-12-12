package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.*;

public record BakedClass(
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Pair<Token, GeneratedCallable>[] methods,
        Pair<Token, GeneratedCallable>[] staticMethods,
        Pair<Token, GeneratedCallable>[] constructors,
        Stmt.VarDecl[] fields,
        Stmt.VarDecl[] staticFields,
        LoxClass superclass, Token name, String pck,
        LoxClass[] interfaces,
        Compiler.ClassBuilder[] enclosed,
        boolean isAbstract, boolean isFinal
) implements Compiler.ClassBuilder {

    @Override
    public GeneratedClass build() {
        ImmutableMap.Builder<String, LoxClass> enclosed = new ImmutableMap.Builder<>();
        for (int i = 0; i < this.enclosed().length; i++) {
            LoxClass loxClass = this.enclosed()[i].build();
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

        Map<String, GeneratedField> fields = Compiler.ClassBuilder.generateFields(this.fields());

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
                fields,
                Compiler.ClassBuilder.generateFields(this.staticFields()),
                enclosed.build(),
                this.superclass(),
                this.interfaces(),
                this.name().lexeme(),
                this.pck(),
                this.isAbstract(),
                this.isFinal()
        );
        this.target().apply(loxClass);
        return loxClass;
    }
}
