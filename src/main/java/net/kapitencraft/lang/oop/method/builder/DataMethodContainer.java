package net.kapitencraft.lang.oop.method.builder;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import net.kapitencraft.lang.compile.CacheBuilder;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.tool.Util;

import java.util.*;

public class DataMethodContainer implements MethodContainer {
    private final LoxCallable[] methods;

    public DataMethodContainer(LoxCallable[] methods) {
        this.methods = methods;
    }

    public static DataMethodContainer of(LoxCallable... methods) {
        return new DataMethodContainer(methods);
    }

    public LoxCallable getMethod(List<? extends LoxClass> expectedArgs) {
        for (LoxCallable method : methods) {
            if (Util.matchArgs(method.argTypes(), expectedArgs)) return method;
        }
        //just going to make it easier for myself xD
        return methods[0];
    }

    //expecting Container methods to be Functions (!)
    public JsonArray cache(CacheBuilder builder) {
        JsonArray array = new JsonArray(methods.length);
        for (LoxCallable method : methods) {
            if (method instanceof GeneratedCallable function) {
                array.add(function.save(builder));
            }
        }
        return array;
    }

    public LoxCallable getMethodByOrdinal(int ordinal) {
        if (ordinal == -1) return methods[0]; //default
        return methods[ordinal];
    }

    public int getMethodOrdinal(List<? extends LoxClass> types) {
        for (int i = 0; i < methods.length; i++) {
            if (Util.matchArgs(methods[i].argTypes(), types)) return i;
        }
        return -1;
    }

    public static class Builder {
        private final Token className;
        private final List<LoxCallable> methods = new ArrayList<>();

        public Builder(Token className) {
            this.className = className;
        }

        public void addMethod(Compiler.ErrorLogger errorLogger, LoxCallable callable, Token methodName) {
            List<? extends List<? extends LoxClass>> appliedTypes = methods.stream().map(LoxCallable::argTypes).toList();
            List<? extends LoxClass> argTypes = callable.argTypes();
            for (List<? extends LoxClass> appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorLogger.error(methodName, String.format("method %s(%s) is already defined in class %s", methodName.lexeme(), Util.getDescriptor(argTypes), className.lexeme()));
                    return;
                }
            }
            methods.add(callable);
        }

        public DataMethodContainer create() {
            return new DataMethodContainer(methods.toArray(new LoxCallable[0]));
        }
    }

    public static Map<String, DataMethodContainer> bakeBuilders(Map<String, DataMethodContainer.Builder> builders) {
        Map<String, DataMethodContainer> map = new HashMap<>();
        builders.forEach((name, builder) -> map.put(name, builder.create()));
        return ImmutableMap.copyOf(map);
    }
}
