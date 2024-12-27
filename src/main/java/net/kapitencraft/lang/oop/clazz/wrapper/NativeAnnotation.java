package net.kapitencraft.lang.oop.clazz.wrapper;

import net.kapitencraft.lang.oop.clazz.AnnotationClass;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;

import java.util.Map;

public class NativeAnnotation extends AnnotationClass {
    public NativeAnnotation(String name, String pck, Map<String, AnnotationCallable> properties) {
        super(name, pck, new AnnotationMethodMap(properties), Map.of());
    }
}
