package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.HashMap;
import java.util.Map;

public class CompileAnnotationClassInstance implements AbstractAnnotationClassInstance {
    public final Map<String, Expr> properties = new HashMap<>();
    private final ScriptedClass type;

    public CompileAnnotationClassInstance(ScriptedClass type, Map<String, Expr> appliedProperties) {
        this.type = type;
        properties.putAll(appliedProperties);
    }

    public static CompileAnnotationClassInstance fromPropertyMap(ScriptedClass type, Map<String, Expr> properties) {
        return new CompileAnnotationClassInstance(type, properties);
    }

    public static CompileAnnotationClassInstance fromSingleProperty(ScriptedClass type, Expr singleProperty) {
        return new CompileAnnotationClassInstance(type, Map.of("value", singleProperty));
    }

    public static CompileAnnotationClassInstance noAbstract(ScriptedClass type) {
        return new CompileAnnotationClassInstance(type, Map.of());
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        throw new IllegalAccessError("can not get property for compile class instance");
    }
}
