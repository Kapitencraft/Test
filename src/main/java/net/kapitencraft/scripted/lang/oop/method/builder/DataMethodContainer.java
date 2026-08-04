package net.kapitencraft.scripted.lang.oop.method.builder;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.RuntimeCallable;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.scripted.lang.tool.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DataMethodContainer(ScriptedCallable[] methods) implements MethodContainer {

    public int size() {
        return methods.length;
    }

    public static DataMethodContainer of(ScriptedCallable... methods) {
        return new DataMethodContainer(methods);
    }

    public ScriptedCallable getMethod(ClassReference[] expectedArgs) {
        for (ScriptedCallable method : methods) {
            if (Util.matchArgs(method.argTypes(), expectedArgs)) return method;
        }
        //just going to make it easier for myself xD
        return methods[0];
    }

    public static class Builder {
        private final Token className;
        private final List<ScriptedCallable> methods = new ArrayList<>();

        public Builder(Token className) {
            this.className = className;
        }

        public void addMethod(ErrorStorage errorStorage, ScriptedCallable callable, Token methodName) {
            List<? extends ClassReference[]> appliedTypes = methods.stream().map(ScriptedCallable::argTypes).toList();
            ClassReference[] argTypes = callable.argTypes();
            for (ClassReference[] appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorStorage.errorF(methodName, "method %s(%s) is already defined in class %s", methodName.lexeme(), Util.getDescriptor(argTypes), className.lexeme());
                    return;
                }
            }
            methods.add(callable);
        }

        public DataMethodContainer create() {
            return new DataMethodContainer(methods.toArray(new ScriptedCallable[0]));
        }

        public boolean isEmpty() {
            return this.methods.isEmpty();
        }
    }

    public static Map<String, DataMethodContainer> bakeBuilders(Map<String, Builder> builders) {
        Map<String, DataMethodContainer> map = new HashMap<>();
        builders.forEach((name, builder) -> map.put(name, builder.create()));
        return ImmutableMap.copyOf(map);
    }
}
