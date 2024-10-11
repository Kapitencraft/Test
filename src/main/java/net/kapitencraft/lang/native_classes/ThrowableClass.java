package net.kapitencraft.lang.native_classes;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.GeneratedCallable;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.method_builder.ConstructorContainer;
import net.kapitencraft.lang.func.method_builder.MethodContainer;
import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public class ThrowableClass implements LoxClass {
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
    public LoxCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public MethodContainer getConstructor() {
        return ConstructorContainer.fromCache(List.of(
                new LoxCallable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public LoxClass type() {
                        return ThrowableClass.this;
                    }

                    @Override
                    public List<? extends LoxClass> argTypes() {
                        return List.of(VarTypeManager.STRING);
                    }

                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        return new ClassInstance(ThrowableClass.this, interpreter);
                    }

                    @Override
                    public boolean isAbstract() {
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
    public LoxCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return 0;
    }

    @Override
    public boolean hasEnclosing(String lexeme) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String lexeme) {
        return null;
    }
}
