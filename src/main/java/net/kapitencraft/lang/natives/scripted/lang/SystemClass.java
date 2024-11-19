package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.NativeUtilClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class SystemClass extends NativeUtilClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(
            "print",
            DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.OBJECT.get()), VarTypeManager.VOID, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            System.out.println(Interpreter.stringify(arguments.get(0)));
                            return null;
                        }
                    }
            ),
            "time",
            DataMethodContainer.of(
                    new NativeMethodImpl(List.of(), VarTypeManager.INTEGER, true, false) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return (int) (System.currentTimeMillis() - Interpreter.millisAtStart);
                        }
                    }),
            "input", DataMethodContainer.of(
                    new NativeMethodImpl(List.of(VarTypeManager.OBJECT.get()), VarTypeManager.STRING.get(), true, false) {
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
