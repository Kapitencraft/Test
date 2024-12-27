package net.kapitencraft.lang.natives.scripted.lang.annotation;


import net.kapitencraft.lang.oop.clazz.wrapper.NativeAnnotation;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;

import java.util.Map;

public class RetentionAnnotation extends NativeAnnotation {
    public RetentionAnnotation() {
        super("Retention", "scripted.lang.annotation",
                Map.of("value", AnnotationCallable.enumType(null))
        );
    }
}
