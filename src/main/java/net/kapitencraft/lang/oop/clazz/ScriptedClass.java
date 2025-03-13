package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
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
        checkInit();
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

    default void checkInit() {
        if (!hasInit() && !Interpreter.suppressClassLoad) clInit();
    }

    default Object assignStaticField(String name, Object val) {
        checkInit();
        staticFieldData.put(name, val);
        return getStaticField(name);
    }

    boolean hasInit();

    void setInit();

    default void startClInit() {
        setInit();
        Interpreter.INSTANCE.pushCallIndex(-1);
        Interpreter.INSTANCE.pushCall(this.absoluteName(), "<clinit>", this.name());
    }

    default void endClInit() {
        Interpreter.INSTANCE.popCall();
    }

    default void clInit() {

        this.staticFields().forEach((s, loxField) -> {
            assignStaticField(s, loxField.initialize(null, Interpreter.INSTANCE));
        });

        for (ClassReference loxClass : this.enclosed()) {
            loxClass.get().clInit();
        }
    }

    ClassReference[] enclosed();

    Map<String, ? extends ScriptedField> staticFields();

    default Object staticSpecialAssign(String name, Token assignType) {
        checkInit();
        Object val = getStaticField(name);
        if (val instanceof Integer) {
            return this.assignStaticField(name, (int)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else if (val instanceof Float) {
            return this.assignStaticField(name, (float) val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else {
            return this.assignStaticField(name, (double)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        }
    }

    default Object assignStaticFieldWithOperator(String name, Object val, Token type, ScriptedClass executor, Operand operand) {
        checkInit();
        Object newVal = Interpreter.INSTANCE.visitAlgebra(getStaticField(name), val, executor, type, operand);
        return this.assignStaticField(name, newVal);
    }

    @Contract(pure = true)
    String name();

    @Contract(pure = true)
    String pck(); //stupid keyword :agony:

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
        return staticFields().get(name).getType();
    }

    default boolean hasStaticField(String name) {
        return superclass() != null && superclass().get().hasStaticField(name);
    }

    default ScriptedCallable getStaticMethod(String name, ClassReference[] args) {
        return getStaticMethodByOrdinal(name, getStaticMethodOrdinal(name, args));
    }

    ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal);

    int getStaticMethodOrdinal(String name, ClassReference[] args);

    boolean hasStaticMethod(String name);

    default ScriptedCallable getMethod(String name, ClassReference[] args) {
        return getMethodByOrdinal(name, getMethodOrdinal(name, args));
    }

    default boolean hasMethod(String name) {
        return superclass() != null && superclass().get().hasMethod(name);
    }

    default DynamicClassInstance createInst(List<Expr> params, int ordinal, Interpreter interpreter) {
        return createNativeInst(interpreter.visitArgs(params), ordinal, interpreter);
    }

    default DynamicClassInstance createNativeInst(List<Object> params, int ordinal, Interpreter interpreter) {
        DynamicClassInstance instance = new DynamicClassInstance(this, interpreter);
        instance.construct(params, ordinal, interpreter);
        return instance;
    }

    default Map<String, ? extends ScriptedField> getFields() {
        return superclass() != null ? superclass().get().getFields() : Map.of();
    }

    MethodContainer getConstructor();

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
    int getMethodOrdinal(String name, ClassReference[] types);

    boolean hasEnclosing(String name);

    ClassReference getEnclosing(String name);

    AbstractMethodMap getMethods();

    ClassType getClassType();

    AnnotationClassInstance[] annotations();

    // ----- MODIFIERS START -----

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

    // ----- MODIFIERS END -----
}