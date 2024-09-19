package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.func.LoxCallable;

import java.util.List;
import java.util.Map;

public class PrimitiveClass extends LoxClass {

    public PrimitiveClass(LoxClass superclass, String name) {
        super(Map.of(), Map.of(), null, List.of(), Map.of(), Map.of(), superclass, name);
    }

    public PrimitiveClass(String name) {
        super(Map.of(), Map.of(), null, List.of(), Map.of(), Map.of(), null, name);
    }
}
