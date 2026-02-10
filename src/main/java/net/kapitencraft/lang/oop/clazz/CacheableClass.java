package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Synthesizer;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;

public interface CacheableClass {

    JsonObject save(Synthesizer cacheBuilder);

    default ClassReference reference() {
        return VarTypeManager.getOrCreateClass(name(), pck());
    }

    String pck();

    String name();

    default String absoluteName() {
        return pck() + "." + name();
    }
}
