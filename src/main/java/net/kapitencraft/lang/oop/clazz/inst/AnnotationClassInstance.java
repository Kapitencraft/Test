package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.HashMap;
import java.util.Map;

public class AnnotationClassInstance implements AbstractAnnotationClassInstance {
    public final Map<String, Expr> properties = new HashMap<>();
    private final ScriptedClass type;

    public AnnotationClassInstance(ScriptedClass type, Map<String, Expr> appliedProperties) {
        this.type = type;
        properties.putAll(appliedProperties);
    }

    public static AnnotationClassInstance fromPropertyMap(ScriptedClass type, Map<String, Expr> properties) {
        return new AnnotationClassInstance(type, properties);
    }

    public static AnnotationClassInstance fromSingleProperty(ScriptedClass type, Expr singleProperty) {
        return new AnnotationClassInstance(type, Map.of("value", singleProperty));
    }

    public static AnnotationClassInstance noAbstract(ScriptedClass type) {
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
