package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.func.NativeMethod;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public class ThrowableClass implements LoxClass {
    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        return null;
    }

    @Override
    public String name() {
        return "Throwable";
    }

    @Override
    public String packageRepresentation() {
        return "scripted.lang.";
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.OBJECT;
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Map.of(
                "message", new LoxField() {

                    @Override
                    public Object initialize(Environment environment, Interpreter interpreter) {
                        return null;
                    }

                    @Override
                    public LoxClass getType() {
                        return VarTypeManager.STRING;
                    }

                    @Override
                    public boolean isFinal() {
                        return true;
                    }
                }
        );
    }

    @Override
    public LoxClass getFieldType(String name) {
        return "message".equals(name) ? VarTypeManager.STRING : null;
    }

    @Override
    public boolean hasField(String name) {
        return "message".equals(name);
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return 0;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public MethodContainer getConstructor() {
        return ConstructorContainer.fromCache(List.of(
                new NativeMethod(List.of(VarTypeManager.STRING), VarTypeManager.VOID, ) {
                    @Override
                    public LoxClass type() {
                        return VarTypeManager.VOID;
                    }

                    @Override
                    public List<? extends LoxClass> argTypes() {
                        return List.of(VarTypeManager.STRING);
                    }

                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        ((ClassInstance) environment.getThis()).assignField("message", arguments.get(0));
                        return null;
                    }

                    @Override
                    public boolean isAbstract() {
                        return false;
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }
                }
        ), this);
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return -1;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    @Override
    public MethodMap getMethods() {
        return null;
    }
}
