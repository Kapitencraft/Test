package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.oop.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public interface LoxClass {
    //TODO abstract as interface and save all data in each subtype

    String name();


    LoxClass superclass();

    default LoxClass getFieldType(String name) {
        return superclass().getFieldType(name);
    }

    LoxClass getStaticFieldType(String name);

    default boolean hasField(String name) {
        return superclass().hasField(name);
    } //TODO check for subtypes

    LoxClass getStaticMethodType(String name);

    default LoxClass getMethodType(String name) {
        return superclass().getMethodType(name);
    }

    LoxCallable getStaticMethod(String name);

    default LoxCallable getMethod(String name) {
        return superclass().getMethod(name);
    }

    boolean hasStaticMethod(String name);

    default boolean hasMethod(String name) {
        return superclass().hasMethod(name);
    }

    default ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        ClassInstance instance = new ClassInstance(this, interpreter);
        instance.construct(params, interpreter);
        return instance;
    }

    Map<String, LoxField> getFields();

    void callConstructor(Environment environment, Interpreter interpreter, List<Object> args);

    default boolean is(LoxClass other) {
        return other == this;
    }

    default boolean isParentOf(LoxClass suspectedChild) {
        if (suspectedChild instanceof PreviewClass previewClass) suspectedChild = previewClass.getTarget();
        if (suspectedChild.is(this) || suspectedChild == VarTypeManager.OBJECT) return true;
        while (suspectedChild != VarTypeManager.OBJECT && !suspectedChild.is(this)) {
            suspectedChild = suspectedChild.superclass();
        }
        return suspectedChild.is(this);
    }

    default boolean isChildOf(LoxClass suspectedParent) {
        return suspectedParent.isParentOf(this);
    }
}