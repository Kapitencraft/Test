package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.HashMap;
import java.util.Map;

public record BakedInterface(Compiler.ErrorLogger logger, PreviewClass target, Pair<Token, GeneratedCallable>[] methods, Pair<Token, GeneratedCallable>[] staticMethods, Stmt.VarDecl[] staticFields, LoxClass[] parentInterfaces, Token name, String pck, Compiler.ClassBuilder[] enclosed) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {
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

        Map<String, GeneratedField> staticFields = Compiler.ClassBuilder.generateFields(this.staticFields);


        return new GeneratedInterface(
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                staticFields,
                parentInterfaces,
                enclosed.build(),
                name().lexeme(),
                pck()
        );
    }

    @Override
    public LoxClass superclass() {
        return null;
    }
}