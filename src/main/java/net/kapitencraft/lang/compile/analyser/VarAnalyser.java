package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.ast.Var;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Pair;

import java.util.Objects;

public class VarAnalyser extends Leveled<String, Pair<Boolean, Var<?>>> {

    public boolean add(String name, String type, boolean value) {
        if (this.getLast().containsKey(name)) return true;

        Class<?> clazz = VarTypeManager.getClassForName(type);

        this.getLast().put(name, Pair.of(value, new Var<>(false, clazz))); //TODO implement final
        return false;
    }

    public Class<?> getType(String name) {
        return getVarRef(name).getValueClass();
    }

    public boolean hasVar(String name) {
        return this.getLast().containsKey(name);
    }

    public Var<?> getVarRef(String name) {
        return this.getLast().get(name).right();
    }

    public void setHasValue(String name) {
        this.getLast().compute(name, (k, pair) -> new Pair<>(true, Objects.requireNonNull(pair).right()));
    }

    public boolean hasValue(String name) {
        return this.getLast().get(name).left();
    }
}
