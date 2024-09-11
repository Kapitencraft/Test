package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.env.abst.DequeStack;

import java.util.ArrayList;
import java.util.List;

public class MethodAnalyser extends DequeStack<List<String>> {

    public MethodAnalyser() {
        super(new ArrayList<>(), ArrayList::new);
    }

    public boolean add(String name) {
        if (this.getLast().contains(name)) return true;

        this.getLast().add(name);
        return false;
    }

    public boolean has(String name) {
        return this.getLast().contains(name);
    }
}
