package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Util;

import java.util.ArrayList;
import java.util.List;

public class ConstructorContainer extends DataMethodContainer {

    public ConstructorContainer(ScriptedCallable[] methods) {
        super(methods);
    }

    public static ConstructorContainer fromCache(List<ScriptedCallable> methods, LoxClass targetClass) {
        if (methods.isEmpty()) {
            methods.add(new ScriptedCallable() {

                @Override
                public ClassReference type() {
                    return ClassReference.of(targetClass);
                }

                @Override
                public List<ClassReference> argTypes() {
                    return List.of();
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return new ClassInstance(targetClass, interpreter);
                }

                @Override
                public boolean isAbstract() {
                    return false;
                }

                @Override
                public boolean isFinal() {
                    return false;
                }
            });
        }
        return new ConstructorContainer(methods.toArray(new ScriptedCallable[0]));
    }

    public static class Builder {
        private final List<String> finalVars;
        private final List<ScriptedCallable> methods = new ArrayList<>();
        private final Token className;

        public Builder(List<String> finalVars, Token className) {
            this.finalVars = finalVars;
            this.className = className;
        }

        public void addMethod(Compiler.ErrorLogger errorLogger, ScriptedCallable callable, Token constructorLocation) {
            List<? extends List<ClassReference>> appliedTypes = methods.stream().map(ScriptedCallable::argTypes).toList();
            List<ClassReference> argTypes = callable.argTypes();
            for (List<ClassReference> appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorLogger.errorF(constructorLocation, "duplicate constructors with args %s in class %s", Util.getDescriptor(argTypes), className.lexeme());
                    return;
                }
            }
            methods.add(callable);
        }

        public ConstructorContainer build(LoxClass targetClass) {
            if (methods.isEmpty()) {
                if (finalVars.isEmpty()) {
                    methods.add(new ScriptedCallable() {
                        @Override
                        public ClassReference type() {
                            return ClassReference.of(targetClass);
                        }

                        @Override
                        public List<ClassReference> argTypes() {
                            return List.of();
                        }

                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return new ClassInstance(targetClass, interpreter);
                        }

                        @Override
                        public boolean isAbstract() {
                            return false;
                        }

                        @Override
                        public boolean isFinal() {
                            return false;
                        }
                    });
                }
                else {

                }
            }
            return new ConstructorContainer(methods.toArray(new ScriptedCallable[0]));
        }
    }
}
