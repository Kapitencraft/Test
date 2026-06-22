package net.kapitencraft.lang.func;

import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.HashMap;
import java.util.Map;

public interface ScriptedCallable {

    static Map<String, ScriptedCallable> parseMethods(Map<String, DataMethodContainer> methods) {
        Map<String, ScriptedCallable> map = new HashMap<>();
        methods.forEach((string, dataMethodContainer) -> {
            for (ScriptedCallable method : dataMethodContainer.methods()) {
                map.put(VarTypeManager.getMethodSignatureNoTarget(string, method.argTypes()), method);
            }
        });
        return map;
    }

    ClassReference retType();

    ClassReference[] argTypes();

    Object call(Object[] arguments);

    default String getMethodTypeSignature(ScriptedClass compileClass) {
        return "(" +
    }

    default Chunk getChunk() {
        return null;
    }

    boolean isAbstract();

    boolean isFinal();

    boolean isStatic();

    default boolean isNative() {
        return true;
    }

    short modifiers();
}