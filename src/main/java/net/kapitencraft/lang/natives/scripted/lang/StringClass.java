package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.NativeClass;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.Map;

public class StringClass extends NativeClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(

    );

    public StringClass(Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields, Map<String, DataMethodContainer> methods, DataMethodContainer constructor, LoxClass superclass, boolean isAbstract, boolean isFinal, boolean isInterface) {
        super("String", "scripted.lang", staticMethods, staticFields, methods, constructor, superclass, isAbstract, isFinal, isInterface);
    }
}
