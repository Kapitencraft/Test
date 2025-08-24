package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.natives.impl.NativeMethodImpl;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.Interpreter;

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
            constants.put(s, (DynamicClassInstance) loxField.initialize(null, null));
        });
        setConstantValues(constants.build());
        endClInit();
    }

    @Override
    default Map<String, ? extends ScriptedField> staticFields() {
        return enumConstants();
    }

    @Override
    default ScriptedCallable getMethod(String signature) {
        if ("values()".equals(signature)) {
            return new NativeMethodImpl(new ClassReference[0], this.array().reference(), true, false) {
                @Override
                public Object call(Object[] arguments) {
                    return getConstants();
                }

                @Override
                public boolean isStatic() {
                    return true;
                }
            };
        }
        return null;
    }

    @Override
    default boolean hasMethod(String name) {
        return "values".equals(name) || ScriptedClass.super.hasMethod(name);
    }
}
