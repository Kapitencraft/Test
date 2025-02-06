package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ClassReference implements Supplier<ScriptedClass> {
    protected ScriptedClass target;
    private final String name;
    private final String pck;
    private final Map<String, ClassReference> enclosed = new HashMap<>();

    public ClassReference(String name, String pck) {
        this.name = name;
        this.pck = pck;
    }

    public void registerEnclosed(Token name, ClassReference reference, Compiler.ErrorLogger logger) {
        if (enclosed.containsKey(name.lexeme())) {
            logger.errorF(name, "duplicate inner class with name %s", name.lexeme());
        } else enclosed.put(name.lexeme(), reference);
    }

    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    public ClassReference getEnclosedUnsave(String name) {
        return enclosed.get(name);
    }

    public ClassReference getEnclosedOrError(Token name, Compiler.ErrorLogger logger) {
        if (!enclosed.containsKey(name.lexeme())) {
            logger.errorF(name, "no inner class '%s' in %s", name.lexeme(), this.absoluteName());
        }
        return enclosed.get(name.lexeme());
    }

    public ScriptedClass get() {
        return Objects.requireNonNull(target, "ScriptedClass not present: " + this.name);
    }

    public String absoluteName() {
        return pck + "." + name;
    }

    public String pck() {
        return pck;
    }

    public static ClassReference of(ScriptedClass target) {
        ClassReference reference = new ClassReference(target.name(), target.pck());
        reference.setTarget(target);
        return reference;
    }

    public String name() {
        return name;
    }

    public void setTarget(ScriptedClass target) {
        this.target = target;
    }

    public boolean is(ClassReference other) {
        return this.get().is(other.get());
    }

    public boolean is(ScriptedClass scriptedClass) {
        return this.get().is(scriptedClass);
    }

    public ClassReference array() {
        return new ClassWrapper(this.name + "[]", this.pck, ScriptedClass::array, this);
    }

    @Override
    public String toString() {
        return "ClassReference@" + this.name + (exists() ? ", applied: " + target : "");
    }

    protected boolean exists() {
        return target != null;
    }
}
