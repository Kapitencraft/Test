package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.compiler.analyser.FinalsPopulatedAnalyser;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public interface CacheableClass {

    default ClassReference reference() {
        return VarTypeManager.getOrCreateClass(name(), pck());
    }

    String pck();

    String name();

    default String absoluteName() {
        return pck() + "." + name();
    }

    void optimize();

    void analyseFinals(FinalsPopulatedAnalyser analyser);
}
