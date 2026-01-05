package net.kapitencraft.lang.run.natives.scripted.lang;

import net.kapitencraft.lang.run.natives.NativeClass;

@NativeClass(pck = "scripted.lang")
public class Enum {
    private final String name;
    private final int ordinal;

    public Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }
}
