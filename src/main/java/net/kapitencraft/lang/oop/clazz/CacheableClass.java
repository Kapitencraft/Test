package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;

public interface CacheableClass extends ScriptedClass {

    JsonObject save(CacheBuilder cacheBuilder);

    ClassReference[] enclosed();

    MethodLookup methods();
}
