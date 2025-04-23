
package net.kapitencraft.lang.oop.method.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.CompileAnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.RuntimeAnnotationCallable;
import net.kapitencraft.tool.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public class GeneratedAnnotationMethodMap extends AnnotationMethodMap {
    public GeneratedAnnotationMethodMap(Map<String, AnnotationCallable> methods) {
        super(methods);
    }

    public static GeneratedAnnotationMethodMap read(JsonObject data, String methods) {
        JsonObject object = GsonHelper.getAsJsonObject(data, methods);
        Map<String, AnnotationCallable> map = new HashMap<>();
        object.asMap().forEach((string, jsonElement) -> map.put(string, RuntimeAnnotationCallable.of((JsonObject) jsonElement)));
        return new GeneratedAnnotationMethodMap(map);
    }

    public JsonElement save(CacheBuilder builder) {
        Map<String, AnnotationCallable> methods = getMethods();
        JsonObject object = new JsonObject();
        methods.forEach((string, annotationCallable) -> object.add(string, ((CompileAnnotationCallable) annotationCallable).save(builder)));
        return object;
    }
}
