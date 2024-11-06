package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethod;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class EnumClass implements LoxClass {
    private final Map<String, DataMethodContainer> methods = Map.of(
            "ordinal", DataMethodContainer.of(new NativeMethod(List.of(), VarTypeManager.INTEGER) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return environment.getVar(Token.createNative("ordinal"));
                }
            }),
            "name", DataMethodContainer.of(new NativeMethod(List.of(), VarTypeManager.STRING) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return environment.getVar(Token.createNative("name"));
                }
            })
    );
}
