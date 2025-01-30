package net.kapitencraft.lang.oop.clazz.wrapper;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.EnumClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class NativeEnum implements EnumClass {
    private final String name, pck;

    private final GeneratedMethodMap methods;
    private final GeneratedMethodMap staticMethods;

    private Map<String, ClassInstance> constants;
    ClassInstance[] constantData;

    private final Map<String, ? extends ScriptedField> enumConstants;

    public NativeEnum(String name, String pck, GeneratedMethodMap methods, GeneratedMethodMap staticMethods, Map<String, ? extends ScriptedField> enumConstants) {
        this.name = name;
        this.pck = pck;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.enumConstants = enumConstants;
    }

    @Override
    public Map<String, ? extends ScriptedField> enumConstants() {
        return enumConstants;
    }

    @Override
    public void setConstantValues(Map<String, ClassInstance> constants) {
        this.constants = constants;
        constantData = this.constants.values().toArray(new ClassInstance[0]);
    }

    @Override
    public Map<String, ClassInstance> getConstantValues() {
        return Map.of();
    }

    @Override
    public ClassInstance[] getConstants() {
        return new ClassInstance[0];
    }

    @Override
    public boolean hasInit() {
        return true;
    }

    @Override
    public void setInit() {

    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return pck;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.ENUM;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return 0;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return null;
    }

    @Override
    public AbstractMethodMap getMethods() {
        return null;
    }
}
