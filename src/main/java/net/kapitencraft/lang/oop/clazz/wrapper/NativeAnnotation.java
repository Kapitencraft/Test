package net.kapitencraft.lang.oop.clazz.wrapper;

import net.kapitencraft.lang.oop.clazz.AnnotationClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;

import java.util.List;
import java.util.Map;

public class NativeAnnotation extends AnnotationClass {
    private final AnnotationClassInstance[] annotations;

    public NativeAnnotation(String name, String pck, Map<String, AnnotationCallable> properties, AnnotationClassInstance... annotations) {
        super(name, pck, new AnnotationMethodMap(properties), Map.of());
        this.annotations = annotations;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return annotations;
    }
}
