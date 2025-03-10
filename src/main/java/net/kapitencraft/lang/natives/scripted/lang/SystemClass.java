package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.run.natives.impl.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeUtilClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class SystemClass extends NativeUtilClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(
            "print",
            DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.OBJECT), VarTypeManager.VOID.reference(), true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            interpreter.output.accept(Interpreter.stringify(arguments.get(0)));
                            return null;
                        }
                    }
            ),
            "time",
            DataMethodContainer.of(
                    new NativeMethodImpl(List.of(), VarTypeManager.INTEGER.reference(), true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return (int) interpreter.elapsedMillis();
                        }
                    }),
            "input", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.OBJECT), VarTypeManager.STRING, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            System.out.print(Interpreter.stringify(arguments.get(0)));
                            return Interpreter.in.nextLine();
                        }
                    })
    );

    public SystemClass() {
        super(METHODS, Map.of(), "System", "scripted.lang");
    }
}
