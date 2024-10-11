package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.method_builder.MethodContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.oop.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public interface LoxClass {

    String name();

    String packageRepresentation(); //stupid keyword :agony:

    default String absoluteName() {
        return packageRepresentation() + name();
    }

    LoxClass superclass();

    default LoxClass getFieldType(String name) {
        return superclass().getFieldType(name);
    }

    LoxClass getStaticFieldType(String name);

    default boolean hasField(String name) {
        return superclass().hasField(name);
    }

    LoxCallable getStaticMethod(String name, List<? extends LoxClass> args);

    default LoxCallable getMethod(String name, List<LoxClass> args) {
        return superclass().getMethod(name, args);
    }

    default Map<String, MethodContainer> getMethods() {
        return superclass().getMethods();
    }

    boolean hasStaticMethod(String name);

    default boolean hasMethod(String name) {
        return superclass().hasMethod(name);
    }

    default ClassInstance createInst(List<Expr> params, int ordinal, Interpreter interpreter) {
        return createNativeInst(interpreter.visitArgs(params), ordinal, interpreter);
    }

    default ClassInstance createNativeInst(List<Object> params, int ordinal, Interpreter interpreter) {
        ClassInstance instance = new ClassInstance(this, interpreter);
        instance.construct(params, ordinal, interpreter);
        return instance;
    }

    default Map<String, LoxField> getFields() {
        return superclass().getFields();
    }

    MethodContainer getConstructor();

    boolean isAbstract();

    boolean isFinal();

    default boolean is(LoxClass other) {
        return other == this;
    }

    default boolean isParentOf(LoxClass suspectedChild) {
        if (suspectedChild instanceof PreviewClass previewClass) suspectedChild = previewClass.getTarget();
        if (suspectedChild.is(this) || suspectedChild == VarTypeManager.OBJECT) return true;
        while (suspectedChild != null && suspectedChild != VarTypeManager.OBJECT  && !suspectedChild.is(this)) {
            suspectedChild = suspectedChild.superclass();
        }
        return suspectedChild != null && suspectedChild.is(this);
    }

    default boolean isChildOf(LoxClass suspectedParent) {
        return suspectedParent.isParentOf(this);
    }


    /**
     * @param name name of the method
     * @param ordinal ID of the given method name generated at step COMPILE
     * @return the method that corresponds to the given ID
     */
    LoxCallable getMethodByOrdinal(String name, int ordinal);

    /**
     * @param name name of the method
     * @param types argument types
     * @return the ID of the given method name and argument types; -1 if none could be found
     */
    int getMethodOrdinal(String name, List<LoxClass> types);

    boolean hasEnclosing(String lexeme);

    LoxClass getEnclosing(String lexeme);
}