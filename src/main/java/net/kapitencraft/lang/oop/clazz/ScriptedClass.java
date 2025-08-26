package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface ScriptedClass {
    Map<String, Object> staticFieldData = new HashMap<>();

    default boolean isArray() {
        return false;
    }

    default ScriptedClass array() {
        return new ArrayClass(this);
    }

    default ClassReference reference() {
        return ClassReference.of(this);
    }

    default Object getStaticField(String name) {
        return staticFieldData.get(name);
    }

    //TODO move to reference?
    default ScriptedClass getComponentType() {
        return null;
    }

    /**
     * @param type the operation
     * @param other the other type
     * @return the resulting type or {@link VarTypeManager#VOID}, if this operation is not possible (must return {@link VarTypeManager#BOOLEAN} or {@link VarTypeManager#VOID} for comparators)
     * <br><br>API note: it's recommended to call {@code super.checkOperation(...)} due to the given equality check
     */
    default ScriptedClass checkOperation(OperationType type, Operand operand, ClassReference other) {
        return type == OperationType.ADDITION && "scripted.lang.String".equals(this.absoluteName()) ?
                VarTypeManager.STRING.get() :
                type.is(TokenTypeCategory.EQUALITY) && other.is(this) ?
                        VarTypeManager.BOOLEAN :
                        VarTypeManager.VOID;
    }

    default Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        String selfS = Interpreter.stringify(self);
        String otherS = Interpreter.stringify(other);
        return type == OperationType.ADDITION && "scripted.lang.String".equals(this.absoluteName()) ?
                operand == Operand.LEFT ? selfS + otherS  : otherS + selfS : null;
    }

    default Object assignStaticField(String name, Object val) {
        staticFieldData.put(name, val);
        return getStaticField(name);
    }

    default void clInit() {

        this.staticFields().forEach((s, loxField) -> {
            assignStaticField(s, loxField.initialize(null, null));
        });

        for (ClassReference loxClass : this.enclosed()) {
            loxClass.get().clInit();
        }
    }

    ClassReference[] enclosed();

    Map<String, ? extends ScriptedField> staticFields();

    default Object staticSpecialAssign(String name, TokenType assignType) {
        Object val = getStaticField(name);
        if (val instanceof Integer) {
            return this.assignStaticField(name, (int)val + (assignType == TokenType.GROW ? 1 : -1));
        } else if (val instanceof Float) {
            return this.assignStaticField(name, (float) val + (assignType == TokenType.GROW ? 1 : -1));
        } else {
            return this.assignStaticField(name, (double)val + (assignType == TokenType.GROW ? 1 : -1));
        }
    }

    @Contract(pure = true)
    String name();

    @Contract(pure = true)
    String pck(); //stupid keyword :agony:

    @Contract(pure = true)
    default String absoluteName() {
        return pck() + "." + name();
    }

    @Contract(pure = true)
    @Nullable ClassReference superclass();

    default ClassReference[] interfaces() {
        return superclass() != null ? superclass().get().interfaces() : new ClassReference[0];
    }

    default ClassReference getFieldType(String name) {
        return superclass() != null ? superclass().get().getFieldType(name) : null;
    }

    default boolean hasField(String name) {
        return superclass() != null && superclass().get().hasField(name);
    }

    default ClassReference getStaticFieldType(String name) {
        return staticFields().get(name).type();
    }

    default boolean hasStaticField(String name) {
        return superclass() != null && superclass().get().hasStaticField(name);
    }

    default DynamicClassInstance createNativeInst(Object[] params, int ordinal) {
        DynamicClassInstance instance = null;// new DynamicClassInstance(this, interpreter);
        return instance;
    }

    default Map<String, ? extends ScriptedField> getFields() {
        return superclass() != null ? superclass().get().getFields() : Map.of();
    }

    default boolean is(ScriptedClass other) {
        return other == this;
    }

    default boolean isParentOf(ScriptedClass suspectedChild) {
        if (suspectedChild.is(this) || (!suspectedChild.isInterface() && VarTypeManager.OBJECT.get().is(this))) return true;
        while (suspectedChild != null && suspectedChild.superclass() != null && suspectedChild != VarTypeManager.OBJECT.get() && !suspectedChild.is(this)) {
            suspectedChild = suspectedChild.superclass().get();
        }
        return suspectedChild != null && suspectedChild.is(this);
    }

    default boolean isChildOf(ScriptedClass suspectedParent) {
        return suspectedParent.isInterface() ?
                Arrays.stream(this.interfaces()).map(ClassReference::get).anyMatch(scriptedClass -> scriptedClass.isParentOf(suspectedParent)) :
                suspectedParent.isParentOf(this);
    }

    //region method

    /**
     * @param signature the signature of the method, without declaring class or return type
     * @return the method for the signature or null if it couldn't be found
     */
    ScriptedCallable getMethod(String signature);

    default boolean hasMethod(String name) {
        return superclass() != null && superclass().get().hasMethod(name);
    }

    AbstractMethodMap getMethods();

    //endregion

    boolean hasEnclosing(String name);

    ClassReference getEnclosing(String name);

    RuntimeAnnotationClassInstance[] annotations();

    //region MODIFIERS

    @Contract(pure = true)
    short getModifiers();

    default boolean isInterface() {
        return (getModifiers() & Modifiers.INTERFACE) != 0;
    }

    default boolean isAbstract() {
        return Modifiers.isAbstract(getModifiers());
    }

    default boolean isFinal() {
        return Modifiers.isFinal(getModifiers());
    }

    default boolean isAnnotation() {
        return (getModifiers() & Modifiers.ANNOTATION) != 0;
    }

    default @Nullable Holder.Generics getGenerics() {
        return null;
    }

    //endregion
}