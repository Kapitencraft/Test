package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.env.abst.DequeStack;
import net.kapitencraft.lang.env.abst.Leveled;

import java.util.ArrayList;
import java.util.List;

public class VarAnalyser extends Leveled<String, Boolean> {

    public boolean add(String name, boolean value) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, value);
        return false;
    }

    public boolean has(String name) {
        return this.getLast().containsKey(name);
    }

    public void setHasValue(String name) {
        this.getLast().put(name, true);
    }

    public boolean hasValue(String name) {
        return this.getLast().get(name);
    }
}
