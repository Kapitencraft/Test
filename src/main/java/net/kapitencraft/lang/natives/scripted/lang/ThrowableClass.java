package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.natives.impl.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public class ThrowableClass extends NativeClass {

    public ThrowableClass(String name, String pck) {
        super(name, pck, Map.of(), Map.of(), Map.of(), Map.of("message", new ScriptedField() {

            @Override
            public Object initialize(Environment environment, Interpreter interpreter) {
                return null;
            }

            @Override
            public ClassReference getType() {
                return VarTypeManager.STRING;
            }

            @Override
            public boolean isFinal() {
                return true;
            }
        }), new ConstructorContainer(new ScriptedCallable[]{
                new NativeMethodImpl(List.of(VarTypeManager.STRING), VarTypeManager.VOID.reference(), false, false) {

                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        ((ClassInstance) environment.getThis()).assignField("message", arguments.get(0));
                        return null;
                    }

                    @Override
                    public boolean isAbstract() {
                        return false;
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }
                }
        }), VarTypeManager.OBJECT, false, false, false);
    }
}
