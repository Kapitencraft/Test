package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.abst.DequeStack;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public class VarAnalyser extends Leveled<String, Pair<Boolean, Class<?>>> {

    public boolean add(String name, String type, boolean value) {
        if (this.getLast().containsKey(name)) return true;

        Class<?> clazz = VarTypeManager.getClassForName(type);

        this.getLast().put(name, Pair.of(value, clazz));
        return false;
    }

    public Class<?> getType(String name) {
        return this.getLast().get(name).right();
    }

    public boolean has(String name) {
        return this.getLast().containsKey(name);
    }

    public void setHasValue(String name) {
        this.getLast().put(name, Pair.of(true, getType(name)));
    }

    public boolean hasValue(String name) {
        return this.getLast().get(name).left();
    }
}
