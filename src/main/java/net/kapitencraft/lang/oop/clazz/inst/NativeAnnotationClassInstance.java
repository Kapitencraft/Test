package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.List;
import java.util.Map;

public class NativeAnnotationClassInstance implements ClassInstance {
    public final Map<String, Object> properties;
    private final ScriptedClass type;

    public NativeAnnotationClassInstance(ScriptedClass type, Map<String, Object> properties) {
        this.properties = properties;
        this.type = type;
    }

    public static NativeAnnotationClassInstance createSingle(ClassReference reference, Object value) {
        return new NativeAnnotationClassInstance(reference.get(), Map.of("value", value));
    }


    @Override
    public Object assignField(String name, Object val) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    public Object assignFieldWithOperator(String name, Object val, Token type, ScriptedClass executor, Operand operand) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    public Object specialAssign(String name, Token assignType) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    public Object getField(String name) {
        throw new IllegalAccessError("can not get field of annotation");
    }

    @Override
    public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        throw new IllegalAccessError("can not construct annotation");
    }

    @Override
    public Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
        return ordinal == 0 ? properties.containsKey(name) ? type.getMethodByOrdinal(name, 0).call(null, interpreter, List.of()) :  properties.get(name) : null;
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }
}
