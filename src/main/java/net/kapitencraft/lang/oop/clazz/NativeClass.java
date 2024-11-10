package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.natives.scripted.lang.ObjectClass;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

public class NativeClass extends NativeUtilClass {
    private final MethodMap methods;
    private final DataMethodContainer constructor;
    private final LoxClass superclass;
    private final boolean isAbstract, isFinal, isInterface;

    public NativeClass(String name, String pck, Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields, Map<String, DataMethodContainer> methods, DataMethodContainer constructor, LoxClass superclass, boolean isAbstract, boolean isFinal, boolean isInterface) {
        super(staticMethods, staticFields, name, pck);
        this.methods = new MethodMap(methods);
        this.constructor = constructor;
        this.superclass = superclass == null && this.getClass() != ObjectClass.class ? VarTypeManager.OBJECT : superclass;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isInterface = isInterface;
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    @Override
    public MethodMap getMethods() {
        return methods;
    }
}
