package net.kapitencraft.lang.compiler.analyser;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class VarAnalyser extends Leveled<String, VarAnalyser.Wrapper> {

    public boolean add(String name, LoxClass type, boolean value, boolean isFinal) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, new Wrapper(type, value, isFinal));
        return false;
    }

    public LoxClass getType(String name) {
        if (!this.has(name)) return VarTypeManager.VOID;
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
