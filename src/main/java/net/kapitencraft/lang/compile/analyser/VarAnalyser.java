package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.lang.oop.LoxClass;
import net.kapitencraft.tool.Pair;

import java.util.Objects;

public class VarAnalyser extends Leveled<String, VarAnalyser.Wrapper> {

    public boolean add(String name, String type, boolean value, boolean isFinal) {
        if (this.getLast().containsKey(name)) return true;

        LoxClass clazz = VarTypeManager.getClassForName(type);

        this.getLast().put(name, new Wrapper(clazz, value, isFinal)); //TODO implement final
        return false;
    }

    public LoxClass getType(String name) {
        return getValue(name).type;
    }

    public void setHasValue(String name) {
        this.getValue(name).setHasValue();
    }

    public boolean hasValue(String name) {
        return this.getValue(name).value;
    }

    public boolean isFinal(String name) {
        return this.getValue(name).isFinal;
    }

    public static class Wrapper {
        private boolean value;
        private final LoxClass type;
        private final boolean isFinal;

        private Wrapper(LoxClass type, boolean hasValue, boolean isFinal) {
            this.type = type;
            this.isFinal = isFinal;
            this.value = hasValue;
        }

        public void setHasValue() {
            this.value = true;
        }
    }
}
