package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class EnumClass extends NativeClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(
            "ordinal", DataMethodContainer.of(new NativeMethodImpl(List.of(), VarTypeManager.INTEGER, false, false) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return environment.getVar(Token.createNative("ordinal"));
                }
            }),
            "name", DataMethodContainer.of(new NativeMethodImpl(List.of(), VarTypeManager.STRING.get(), false, false) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return environment.getVar(Token.createNative("name"));
                }
            })
    );

    public EnumClass() {
        super("Enum", "scripted.lang", Map.of(), Map.of(), METHODS, DataMethodContainer.of(), null, false, false, false);
    }
}