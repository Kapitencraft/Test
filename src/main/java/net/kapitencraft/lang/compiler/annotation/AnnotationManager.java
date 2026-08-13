package net.kapitencraft.lang.compiler.annotation;

import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationManager {
    private static final List<AnnotationProcessor> processors = new ArrayList<>();

    public static void process(Annotation annotation) {
        AnnotationMirror mirror = annotation.mirror();

        for (AnnotationProcessor processor : processors) {
            if (processor.getSupportedTypes().contains(mirror.getType())) {
                //processor.process(mirror, );
            }
        }
    }
}
