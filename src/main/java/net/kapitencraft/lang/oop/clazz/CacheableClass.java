package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.VarTypeManager;

public interface CacheableClass {

    JsonObject save(CacheBuilder cacheBuilder);

    default ClassReference reference() {
        return VarTypeManager.getOrCreateClass(name(), pck());
    }

    String pck();

    String name();

    default String absoluteName() {
        return pck() + "." + name();
    }
}
