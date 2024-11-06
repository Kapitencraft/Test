package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.lang.func.ScriptedCallable;

public class MethodEnv extends Leveled<String, ScriptedCallable> {

    MethodEnv() {
    }

    public void define(String name, ScriptedCallable func) {
        this.addValue(name, func);
    }

    public ScriptedCallable get(String name) {
        return this.getValue(name);
    }
}
