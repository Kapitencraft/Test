package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public interface EnumClass extends LoxClass {

    Map<String, ? extends LoxField> enumConstants();

    void setConstantValues(Map<String, ClassInstance> constants);

    Map<String, ClassInstance> getConstantValues();

    ClassInstance[] getConstants();

    @Override
    default void clInit() {
        startClInit();
        LoxClass.super.clInit();
        ImmutableMap.Builder<String, ClassInstance> constants = new ImmutableMap.Builder<>();
        this.enumConstants().forEach((s, loxField) -> {
            constants.put(s, (ClassInstance) loxField.initialize(null, Interpreter.INSTANCE));
        });
        setConstantValues(constants.build());
        endClInit();
    }

    @Override
    default Map<String, ? extends LoxField> staticFields() {
        return enumConstants();
    }

    @Override
    default ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        if ("values".equals(name) && ordinal == 0) {
            return new NativeMethodImpl(List.of(), this.array(), true, false) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return getConstants();
                }
            };
        }
        return null;
    }

    @Override
    default int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        if ("values".equals(name) && args.isEmpty()) return 0;
        return -1;
    }
}
