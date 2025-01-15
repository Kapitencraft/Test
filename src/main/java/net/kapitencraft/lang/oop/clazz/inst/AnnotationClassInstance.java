package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationClassInstance implements AbstractClassInstance {
    private final Map<String, Expr> properties = new HashMap<>();
    private final AbstractAnnotationClass type;

    public AnnotationClassInstance(AbstractAnnotationClass type, Map<String, Expr> appliedProperties) {
        this.type = type;
        properties.putAll(appliedProperties);
    }

    public static AnnotationClassInstance fromPropertyMap(AbstractAnnotationClass type, Map<String, Expr> properties) {
        return new AnnotationClassInstance(type, properties);
    }

    public static AnnotationClassInstance fromSingleProperty(AbstractAnnotationClass type, Expr singleProperty) {
        return new AnnotationClassInstance(type, Map.of("value", singleProperty));
    }

    public static AnnotationClassInstance noAbstract(AbstractAnnotationClass type) {
        return new AnnotationClassInstance(type, Map.of());
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
        return ordinal == 0 ? properties.containsKey(name) ? type.getMethodByOrdinal(name, 0).call(null, interpreter, List.of()) :  interpreter.evaluate(properties.get(name)) : null;
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }
}
