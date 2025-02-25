package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationClassInstance implements AbstractAnnotationClassInstance {
    public final Map<String, Expr> properties = new HashMap<>();
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
    public ScriptedClass getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        return Interpreter.INSTANCE.evaluate(properties.get(name));
    }
}
