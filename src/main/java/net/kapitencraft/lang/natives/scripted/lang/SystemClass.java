package net.kapitencraft.lang.natives.scripted.lang;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.func.NativeMethod;
import net.kapitencraft.lang.oop.clazz.NativeUtilClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class SystemClass extends NativeUtilClass {

    public SystemClass() {
        super(setupMethods(), "System", "scripted.lang");
    }


    private static Map<String, DataMethodContainer> setupMethods() {
        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        builder.put("print", new DataMethodContainer(new ScriptedCallable[]{
                new NativeMethod(List.of(VarTypeManager.OBJECT), VarTypeManager.VOID) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        System.out.println(Interpreter.stringify(arguments.get(0)));
                        return null;
                    }
                }
        }));
        builder.put("time", new DataMethodContainer(new ScriptedCallable[]{
                new NativeMethod(List.of(), VarTypeManager.INTEGER) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        return (int) (System.currentTimeMillis() - Interpreter.millisAtStart);
                    }
                }
        }));
        builder.put("input", new DataMethodContainer(new ScriptedCallable[]{
                new NativeMethod(List.of(VarTypeManager.OBJECT), VarTypeManager.STRING) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        System.out.print(Interpreter.stringify(arguments.get(0)));
                        return Interpreter.in.nextLine();
                    }
                }
        }));
        return builder.build();
    }
}
