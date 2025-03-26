package net.kapitencraft.lang.oop.method.builder;

import com.google.common.collect.Lists;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.tool.Util;

import java.util.ArrayList;
import java.util.List;

public class ConstructorContainer extends DataMethodContainer {

    public ConstructorContainer(ScriptedCallable[] methods) {
        super(methods);
    }

    public static ConstructorContainer fromCache(List<ScriptedCallable> methods, ScriptedClass targetClass) {
        methods = new ArrayList<>(methods); //ensure mutable
        if (methods.isEmpty()) {
            methods.add(new ScriptedCallable() {

                @Override
                public ClassReference type() {
                    return ClassReference.of(targetClass);
                }

                @Override
                public ClassReference[] argTypes() {
                    return new ClassReference[0];
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return new DynamicClassInstance(targetClass, interpreter);
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

    public static NativeBuilder builder(ScriptedCallable... methods) {
        return new NativeBuilder(Lists.newArrayList(methods));
    }

    public static class NativeBuilder {
        private final List<ScriptedCallable> methods;

        public NativeBuilder(List<ScriptedCallable> methods) {
            this.methods = methods;
        }

        public ConstructorContainer build(ScriptedClass target) {
            if (methods.isEmpty()) {
                methods.add(new ScriptedCallable() {
                    @Override
                    public ClassReference type() {
                            return ClassReference.of(target);
                        }

                    @Override
                    public ClassReference[] argTypes() {
                            return new ClassReference[0];
                        }

                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        return new DynamicClassInstance(target, interpreter);
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
    }

    public static class Builder {
        private final List<Token> finalVars;
        private final List<ScriptedCallable> methods = new ArrayList<>();
        private final Token className;
        private final Compiler.ErrorLogger logger;

        public Builder(List<Token> finalVars, Token className, Compiler.ErrorLogger logger) {
            this.finalVars = finalVars;
            this.className = className;
            this.logger = logger;
        }

        public void addMethod(Compiler.ErrorLogger errorLogger, ScriptedCallable callable, Token constructorLocation) {
            List<? extends ClassReference[]> appliedTypes = methods.stream().map(ScriptedCallable::argTypes).toList();
            ClassReference[] argTypes = callable.argTypes();
            for (ClassReference[] appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorLogger.errorF(constructorLocation, "duplicate constructors with args %s in class %s", Util.getDescriptor(argTypes), className.lexeme());
                    return;
                }
            }
            methods.add(callable);
        }

        public ConstructorContainer build(ScriptedClass targetClass) {
            if (methods.isEmpty()) {
                if (finalVars.isEmpty()) {
                    methods.add(new ScriptedCallable() {
                        @Override
                        public ClassReference type() {
                            return ClassReference.of(targetClass);
                        }

                        @Override
                        public ClassReference[] argTypes() {
                            return new ClassReference[0];
                        }

                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return new DynamicClassInstance(targetClass, interpreter);
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
                    finalVars.forEach(token -> logger.error(token, "field '" + token.lexeme() + "' not initialized"));
                }
            }
            return new ConstructorContainer(methods.toArray(new ScriptedCallable[0]));
        }
    }
}
