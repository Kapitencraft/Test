package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.natives.impl.NativeMethodImpl;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public interface EnumClass extends ScriptedClass {

    Map<String, ? extends ScriptedField> enumConstants();

    void setConstantValues(Map<String, DynamicClassInstance> constants);

    DynamicClassInstance[] getConstants();

    @Override
    default void clInit() {
        startClInit();
        ScriptedClass.super.clInit();
        ImmutableMap.Builder<String, DynamicClassInstance> constants = new ImmutableMap.Builder<>();
        this.enumConstants().forEach((s, loxField) -> {
            constants.put(s, (DynamicClassInstance) loxField.initialize(null, Interpreter.INSTANCE));
        });
        setConstantValues(constants.build());
        endClInit();
    }

    @Override
    default Map<String, ? extends ScriptedField> staticFields() {
        return enumConstants();
    }

    @Override
    default ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        if ("values".equals(name) && ordinal == 0) {
            return new NativeMethodImpl(new ClassReference[0], this.array().reference(), true, false) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return getConstants();
                }
            };
        }
        return null;
    }

    @Override
    default int getStaticMethodOrdinal(String name, ClassReference[] args) {
        if ("values".equals(name) && args.length == 0) return 0;
        return -1;
    }
}
