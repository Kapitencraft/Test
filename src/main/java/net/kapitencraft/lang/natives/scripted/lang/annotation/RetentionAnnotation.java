package net.kapitencraft.lang.natives.scripted.lang.annotation;


import net.kapitencraft.lang.oop.clazz.AnnotationClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeAnnotation;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.Map;

public class RetentionAnnotation extends NativeAnnotation {
    //TODO add Retention and Target annotations for annotations
    public RetentionAnnotation() {
        super("Retention", "scripted.lang.annotations",
                Map.of("value", AnnotationCallable.enumType(VarTypeManager.RETENTION_POLICY))
        );
    }
}
