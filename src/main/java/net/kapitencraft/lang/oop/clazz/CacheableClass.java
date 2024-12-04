package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.run.algebra.OperationType;

public interface CacheableClass extends LoxClass {

    JsonObject save(CacheBuilder cacheBuilder);

    CacheableClass[] enclosing();

    MethodLookup methods();
}
