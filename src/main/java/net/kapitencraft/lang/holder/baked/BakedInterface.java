package net.kapitencraft.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedInterface;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;

import java.util.HashMap;
import java.util.Map;

public record BakedInterface(Compiler.ErrorLogger logger, ClassReference target, Pair<Token, GeneratedCallable>[] methods, Pair<Token, GeneratedCallable>[] staticMethods, Map<String, GeneratedField> staticFields, ClassReference[] interfaces, Token name, String pck, Compiler.ClassBuilder[] enclosed, AnnotationClassInstance[] annotations) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {
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


        return new GeneratedInterface(
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                staticFields(),
                interfaces(),
                enclosed.build(),
                name().lexeme(),
                pck(),
                annotations()
        );
    }

    @Override
    public ClassReference superclass() {
        return null;
    }
}