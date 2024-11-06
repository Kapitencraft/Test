package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;

public interface CacheableClass extends LoxClass {

    JsonObject save(CacheBuilder cacheBuilder);

    CacheableClass[] enclosing();
}
