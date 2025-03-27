package net.kapitencraft.lang.run.natives;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.natives.impl.NativeClassImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class NativeClassInstance implements ClassInstance {
    private final Object obj;
    private final NativeClassImpl type;
    private final Environment environment = new Environment();

    public NativeClassInstance(NativeClassImpl type, Object obj) {
        this.obj = obj;
        this.type = type;
        this.environment.defineVar("this", this);
    }

    public Object getObject() {
        return obj;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    @Override
    public Object assignField(String name, Object val) {
        type.getFields().get(name).set(obj, NativeClassLoader.extractNative(val));
        return val;
    }

    @Override
    public Object assignFieldWithOperator(String name, Object val, TokenType type, int line, ScriptedClass executor, Operand operand) {
        Object newVal = Interpreter.INSTANCE.visitAlgebra(getField(name), val, executor, type, line, operand);
        return assignField(name, newVal);
    }

    @Override
    public Object specialAssign(String name, TokenType assignType) {
        return null;
    }

    @Override
    public Object getField(String name) {
        return type.getFields().get(name).get(obj);
    }

    @Override
    public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        type.getConstructor().getMethodByOrdinal(ordinal).call(this.environment, interpreter, params);
    }

    @Override
    public Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
        return type.getMethodByOrdinal(name, ordinal).call(this.environment, interpreter, arguments);
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }
}
