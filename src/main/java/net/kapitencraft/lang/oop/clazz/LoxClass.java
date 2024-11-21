package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LoxClass {
    Map<String, Object> staticFieldData = new HashMap<>();

    default boolean isArray() {
        return false;
    }

    default LoxClass array() {
        return new ArrayClass(this);
    }

    default Object getStaticField(String name) {
        return staticFieldData.get(name);
    }

    default Object assignStaticField(String name, Object val) {
        staticFieldData.put(name, val);
        return getStaticField(name);
    }

    default void clInit() {
        this.staticFields().forEach((s, loxField) -> {
            assignStaticField(s, loxField.initialize(null, Interpreter.INSTANCE));
        });
    }

    Map<String, ? extends LoxField> staticFields();

    default Object staticSpecialAssign(String name, Token assignType) {
        Object val = getStaticField(name);
        if (val instanceof Integer) {
            return this.assignStaticField(name, (int)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else if (val instanceof Float) {
            return this.assignStaticField(name, (float) val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else {
            return this.assignStaticField(name, (double)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        }
    }

    default Object assignStaticFieldWithOperator(String name, Object val, Token type) {
        return this.assignStaticField(name, Math.merge(getStaticField(name), val, type));
    }

    String name();

    String packageRepresentation(); //stupid keyword :agony:

    default String absoluteName() {
        return packageRepresentation() + name();
    }

    LoxClass superclass();

    default LoxClass[] interfaces() {
        return superclass().interfaces();
    }

    default LoxClass getFieldType(String name) {
        return superclass().getFieldType(name);
    }

    default LoxClass getStaticFieldType(String name) {
        return staticFields().get(name).getType();
    }

    default boolean hasField(String name) {
        return superclass().hasField(name);
    }

    default ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return getStaticMethodByOrdinal(name, getStaticMethodOrdinal(name, args));
    }

    ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal);

    int getStaticMethodOrdinal(String name, List<? extends LoxClass> args);

    default ScriptedCallable getMethod(String name, List<LoxClass> args) {
        return superclass().getMethod(name, args);
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

    boolean isInterface();

    default boolean is(LoxClass other) {
        return other == this;
    }

    default boolean isParentOf(LoxClass suspectedChild) {
        if (suspectedChild instanceof PreviewClass previewClass) suspectedChild = previewClass.getTarget();
        if (suspectedChild.is(this) || !suspectedChild.isInterface() && VarTypeManager.OBJECT.get().is(this)) return true;
        while (suspectedChild != null && suspectedChild != VarTypeManager.OBJECT.get()  && !suspectedChild.is(this)) {
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
    ScriptedCallable getMethodByOrdinal(String name, int ordinal);

    /**
     * @param name name of the method
     * @param types argument types
     * @return the ID of the given method name and argument types; -1 if none could be found
     */
    int getMethodOrdinal(String name, List<LoxClass> types);

    boolean hasEnclosing(String name);

    LoxClass getEnclosing(String name);

    MethodMap getMethods();
}