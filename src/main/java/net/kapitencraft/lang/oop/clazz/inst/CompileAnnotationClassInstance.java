package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.HashMap;
import java.util.Map;

public class CompileAnnotationClassInstance implements AbstractAnnotationClassInstance {
    public final Map<String, CompileExpr> properties = new HashMap<>();
    private final ScriptedClass type;

    public CompileAnnotationClassInstance(ScriptedClass type, Map<String, CompileExpr> appliedProperties) {
        this.type = type;
        properties.putAll(appliedProperties);
    }

    public static CompileAnnotationClassInstance fromPropertyMap(ScriptedClass type, Map<String, CompileExpr> properties) {
        return new CompileAnnotationClassInstance(type, properties);
    }

    public static CompileAnnotationClassInstance fromSingleProperty(ScriptedClass type, CompileExpr singleProperty) {
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
