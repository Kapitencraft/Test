package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.oop.clazz.NativeClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.Map;

public class StringClass extends NativeClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(

    );

    public StringClass() {
        super("String", "scripted.lang", Map.of(), Map.of(), METHODS, DataMethodContainer.of(), VarTypeManager.OBJECT.get(), false, false, false);
    }
}
