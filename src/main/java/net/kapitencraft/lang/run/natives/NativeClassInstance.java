package net.kapitencraft.lang.run.natives;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.run.Interpreter;

public class NativeClassInstance extends ClassInstance {
    private final Object obj;

    public NativeClassInstance(ScriptedClass type, Object obj) {
        super(type, Interpreter.INSTANCE);
        this.obj = obj;
    }

    public Object getObject() {
        return obj;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    @Override
    public Object getField(String name) {
        //TODO implement (static) fields
        return super.getField(name);
    }
}
