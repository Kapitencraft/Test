package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.NativeClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class ObjectClass extends NativeClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(
            "toString", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(), VarTypeManager.STRING.get(), false, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return ((ClassInstance) environment.getThis()).getType().name();
                        }
            }),
            "equals", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.OBJECT.get()), VarTypeManager.BOOLEAN, false, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return environment.getThis() == arguments.get(0);
                        }
            })
    );

    public ObjectClass() {
        super(
                "Object", "scripted.lang",
                Map.of(), Map.of(),
                METHODS,
                DataMethodContainer.of(),
                null,
                false, false, false
        );
    }

    @Override
    public LoxClass[] interfaces() {
        return null;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Map.of();
    }
}
