package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;

public interface CacheableClass extends LoxClass {

    JsonObject save(CacheBuilder cacheBuilder);

    CacheableClass[] enclosing();

    MethodLookup methods();
}
