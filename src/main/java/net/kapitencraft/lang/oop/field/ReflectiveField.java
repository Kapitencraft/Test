package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectiveField extends LoxField {
    private final Field field;
    private final LoxClass type;

    public ReflectiveField(Field field) {
        this.field = field;
        this.type = VarTypeManager.lookupClass(field.getType());
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        return null; //done by the JVM (yay!)
    }

    @Override
    public LoxClass getType() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.field.getModifiers());
    }
}
