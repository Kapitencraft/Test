package net.kapitencraft.lang.compiler.analyser;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.abst.Leveled;

public class VarAnalyser extends Leveled<String, VarAnalyser.Wrapper> {

    public boolean add(String name, ClassReference type, boolean value, boolean isFinal) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, new Wrapper(type, value, isFinal));
        return false;
    }

    public ClassReference getType(String name) {
        if (!this.has(name)) return VarTypeManager.VOID.reference();
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
        private final ClassReference type;
        private final boolean isFinal;

        private Wrapper(ClassReference type, boolean hasValue, boolean isFinal) {
            this.type = type;
            this.isFinal = isFinal;
            this.value = hasValue;
        }

        public void setHasValue() {
            this.value = true;
        }
    }
}
