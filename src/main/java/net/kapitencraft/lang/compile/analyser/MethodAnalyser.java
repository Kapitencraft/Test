package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.lang.func.LoxCallable;

import java.util.List;

public class MethodAnalyser extends Leveled<String, LoxCallable> {

    public boolean add(String name, LoxCallable callable) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, callable);
        return false;
    }

    public Class<?> type(String lexeme) {
        return getValue(lexeme).type();
    }

    public List<? extends Class<?>> args(String lexeme) {
        return getValue(lexeme).argTypes();
    }

    public LoxCallable callable(String lexeme) {
        return getValue(lexeme);
    }
}
