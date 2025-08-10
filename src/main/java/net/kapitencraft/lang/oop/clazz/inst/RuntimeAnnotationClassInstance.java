package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.HashMap;
import java.util.Map;

public class RuntimeAnnotationClassInstance implements AbstractAnnotationClassInstance {
    public final Map<String, RuntimeExpr> properties = new HashMap<>();
    private final ScriptedClass type;

    public RuntimeAnnotationClassInstance(ScriptedClass type, Map<String, RuntimeExpr> appliedProperties) {
        this.type = type;
        properties.putAll(appliedProperties);
    }

    public static RuntimeAnnotationClassInstance fromPropertyMap(ScriptedClass type, Map<String, RuntimeExpr> properties) {
        return new RuntimeAnnotationClassInstance(type, properties);
    }

    public static RuntimeAnnotationClassInstance fromSingleProperty(ScriptedClass type, RuntimeExpr singleProperty) {
        return new RuntimeAnnotationClassInstance(type, Map.of("value", singleProperty));
    }

    public static RuntimeAnnotationClassInstance noAbstract(ScriptedClass type) {
        return new RuntimeAnnotationClassInstance(type, Map.of());
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        return null; //TODO parse final
    }
}
