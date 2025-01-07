package net.kapitencraft.lang.oop.clazz.wrapper;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.natives.scripted.lang.ObjectClass;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class NativeClass extends NativeUtilClass {
    private final GeneratedMethodMap methods;
    private final Map<String, ScriptedField> fields;
    private final DataMethodContainer constructor;
    private final ClassReference superclass;
    private final boolean isAbstract, isFinal, isInterface;

    public NativeClass(String name, String pck, Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields, Map<String, DataMethodContainer> methods, Map<String, ScriptedField> fields, DataMethodContainer constructor, ClassReference superclass, boolean isAbstract, boolean isFinal, boolean isInterface) {
        super(staticMethods, staticFields, name, pck);
        this.methods = new GeneratedMethodMap(methods);
        this.fields = fields;
        this.constructor = constructor;
        this.superclass = superclass == null && this.getClass() != ObjectClass.class ? VarTypeManager.OBJECT : superclass;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isInterface = isInterface;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return fields.containsKey(name) ? fields.get(name).getType() : null;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
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
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || superclass != null && super.hasMethod(name);
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
    public Map<String, ScriptedField> getFields() {
        return fields;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }
}
