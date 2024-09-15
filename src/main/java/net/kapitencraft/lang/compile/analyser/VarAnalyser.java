package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Pair;

import java.util.Objects;

public class VarAnalyser extends Leveled<String, Pair<Boolean, Class<?>>> {

    public boolean add(String name, String type, boolean value) {
        if (this.getLast().containsKey(name)) return true;

        Class<?> clazz = VarTypeManager.getClassForName(type);

        this.getLast().put(name, Pair.of(value, clazz)); //TODO implement final
        return false;
    }

    public Class<?> getType(String name) {
        return getLast().get(name).right();
    }

    public boolean hasVar(String name) {
        return this.getLast().containsKey(name);
    }

    public void setHasValue(String name) {
        this.getLast().compute(name, (k, pair) -> new Pair<>(true, Objects.requireNonNull(pair).right()));
    }

    public boolean hasValue(String name) {
        return this.getLast().get(name).left();
    }
}
