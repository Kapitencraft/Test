package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Util;

import java.util.ArrayList;
import java.util.List;

public class ConstructorContainer extends DataMethodContainer {

    public ConstructorContainer(LoxCallable[] methods) {
        super(methods);
    }

    public static ConstructorContainer fromCache(List<LoxCallable> methods, LoxClass targetClass) {
        if (methods.isEmpty()) {
            methods.add(new LoxCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public LoxClass type() {
                    return targetClass;
                }

                @Override
                public List<? extends LoxClass> argTypes() {
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
            });
        }
        return new ConstructorContainer(methods.toArray(new LoxCallable[0]));
    }

    public static class Builder {
        private final List<String> finalVars;
        private final List<LoxCallable> methods = new ArrayList<>();
        private final Token className;

        public Builder(List<String> finalVars, Token className) {
            this.finalVars = finalVars;
            this.className = className;
        }

        public void addMethod(Compiler.ErrorLogger errorLogger, LoxCallable callable, Token constructorLocation) {
            List<? extends List<? extends LoxClass>> appliedTypes = methods.stream().map(LoxCallable::argTypes).toList();
            List<? extends LoxClass> argTypes = callable.argTypes();
            for (List<? extends LoxClass> appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorLogger.error(constructorLocation, String.format("duplicate constructor with args %s in class %s", Util.getDescriptor(argTypes), className.lexeme()));
                    return;
                }
            }
            methods.add(callable);
        }

        public ConstructorContainer build(LoxClass targetClass) {
            if (methods.isEmpty()) {
                if (finalVars.isEmpty()) {
                    methods.add(new LoxCallable() {
                        @Override
                        public int arity() {
                            return 0;
                        }

                        @Override
                        public LoxClass type() {
                            return targetClass;
                        }

                        @Override
                        public List<? extends LoxClass> argTypes() {
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
                    });
                }
                else {

                }
            }
            return new ConstructorContainer(methods.toArray(new LoxCallable[0]));
        }
    }
}
