package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.env.abst.DequeStack;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public class MethodAnalyser extends Leveled<String, Class<?>> {

    public boolean add(String name, Class<?> retType) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, retType);
        return false;
    }

    public boolean has(String name) {
        return this.getLast().containsKey(name);
    }

    public Class<?> type(String lexeme) {
        return getValue(lexeme);
    }
}
