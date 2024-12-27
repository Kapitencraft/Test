package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeUtilClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class MathClass extends NativeUtilClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(
            "abs", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.INTEGER), VarTypeManager.INTEGER, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.abs((Integer) arguments.get(0));
                        }
                    },
                    new NativeMethodImpl(List.of(VarTypeManager.FLOAT), VarTypeManager.FLOAT, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.abs((Float) arguments.get(0));
                        }
                    },
                    new NativeMethodImpl(List.of(VarTypeManager.DOUBLE), VarTypeManager.DOUBLE, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.abs((Double) arguments.get(0));
                        }
                    }
            ),
            "sqrt", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.DOUBLE), VarTypeManager.DOUBLE, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.sqrt((Double) arguments.get(0));
                        }
                    }
            ),
            "log", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.DOUBLE), VarTypeManager.DOUBLE, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.log((Double) arguments.get(0));
                        }
                    }
            ),
            "log10", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.DOUBLE), VarTypeManager.DOUBLE, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.log10((Double) arguments.get(0));
                        }
                    }
            ),
            "sin", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.DOUBLE), VarTypeManager.DOUBLE, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return Math.sin((Double) arguments.get(0));
                        }
                    }
            )
    );

    public MathClass() {
        super(METHODS, Map.of(), "Math", "scripted.lang");
    }
}
