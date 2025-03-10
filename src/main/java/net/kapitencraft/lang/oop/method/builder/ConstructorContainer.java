package net.kapitencraft.lang.oop.method.builder;

import com.google.common.collect.Lists;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Util;

import java.util.ArrayList;
import java.util.List;

public class ConstructorContainer extends DataMethodContainer {

    public ConstructorContainer(ScriptedCallable[] methods) {
        super(methods);
    }

    public static ConstructorContainer fromCache(List<ScriptedCallable> methods, ScriptedClass targetClass) {
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
                    public List<ClassReference> argTypes() {
                            return List.of();
                        }

                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        return new ClassInstance(target, interpreter);
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
        private final List<String> finalVars;
        private final List<ScriptedCallable> methods = new ArrayList<>();
        private final Token className;

        public Builder(List<String> finalVars, Token className) {
            this.finalVars = finalVars;
            this.className = className;
        }

        public void addMethod(Compiler.ErrorLogger errorLogger, ScriptedCallable callable, Token constructorLocation) {
            List<? extends List<? extends ClassReference>> appliedTypes = methods.stream().map(ScriptedCallable::argTypes).toList();
            List<? extends ClassReference> argTypes = callable.argTypes();
            for (List<? extends ClassReference> appliedType : appliedTypes) {
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
