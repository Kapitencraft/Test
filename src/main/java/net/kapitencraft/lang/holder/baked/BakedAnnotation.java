package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedAnnotation;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BakedAnnotation(
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Pair<Token, GeneratedCallable>[] methods,
        Pair<Token, GeneratedCallable>[] staticMethods,
        Stmt.VarDecl[] staticFields,
        Token name, String pck,
        Compiler.ClassBuilder[] enclosed
) implements Compiler.ClassBuilder {

    @Override
    public GeneratedAnnotation build() {
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

        GeneratedAnnotation loxClass = new GeneratedAnnotation(
                DataMethodContainer.bakeBuilders(methods), DataMethodContainer.bakeBuilders(staticMethods),
                Compiler.ClassBuilder.generateFields(this.staticFields()),
                enclosed.build(),
                this.name().lexeme(),
                this.pck()
        );
        this.target().apply(loxClass);
        return loxClass;
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass[] interfaces() {
        return new LoxClass[0];
    }
}
