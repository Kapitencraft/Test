package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public interface EnumClass extends LoxClass {


    Map<String, ? extends LoxField> enumConstants();

    @Override
    default Map<String, ? extends LoxField> staticFields() {
        return enumConstants();
    }

    @Override
    default ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        if ("values".equals(name) && args.isEmpty()) {
            //TODO add arrays
            return new NativeMethodImpl(List.of(), VarTypeManager.VOID, true, false) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return null;
                }
            };
        }
        return LoxClass.super.getStaticMethod(name, args);
    }
}
