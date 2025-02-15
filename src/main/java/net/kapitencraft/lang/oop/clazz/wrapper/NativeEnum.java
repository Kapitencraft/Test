package net.kapitencraft.lang.oop.clazz.wrapper;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.EnumClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class NativeEnum implements EnumClass {
    private final String name, pck;

    private final GeneratedMethodMap methods;
    private final GeneratedMethodMap staticMethods;

    private final DataMethodContainer constructors;

    private final Map<String, ClassInstance> constants;
    ClassInstance[] constantData;
    private final AnnotationClassInstance[] annotations;

    public NativeEnum(String name, String pck, ConstructorContainer.NativeBuilder constructors, GeneratedMethodMap methods, GeneratedMethodMap staticMethods, Map<String, List<Object>> enumConstants, AnnotationClassInstance... annotations) {
        this.name = name;
        this.pck = pck;
        this.methods = methods;
        this.constructors = constructors.build(this);
        this.staticMethods = staticMethods;
        this.annotations = annotations;
        ImmutableMap.Builder<String, ClassInstance> constantBuilder = new ImmutableMap.Builder<>();
        enumConstants.forEach((string, objects) -> {
            ClassInstance instance = new ClassInstance(this, Interpreter.INSTANCE);
            instance.construct(objects, this.constructors.getMethodOrdinal(VarTypeManager.getArgsFromObjects(objects)), Interpreter.INSTANCE);
            constantBuilder.put(string, instance);
        });
        this.constants = constantBuilder.build();
        this.constantData = this.constants.values().toArray(new ClassInstance[0]);
    }

    @Override
    public Map<String, ? extends ScriptedField> enumConstants() {
        return null;
    }

    @Override
    public void setConstantValues(Map<String, ClassInstance> constants) {
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
    public ClassReference getStaticFieldType(String name) {
        return constants.containsKey(name) ? this.reference() : null;
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
        return constructors;
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
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return methods.getMethodOrdinal(name, types);
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
        return methods;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return annotations;
    }
}
