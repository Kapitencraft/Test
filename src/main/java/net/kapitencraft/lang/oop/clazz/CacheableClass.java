package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.VarTypeManager;

public interface CacheableClass {

    default ClassReference reference() {
        return VarTypeManager.getOrCreateClass(name(), pck());
    }

    String pck();

    String name();

    default String absoluteName() {
        return pck() + "." + name();
    }
}
