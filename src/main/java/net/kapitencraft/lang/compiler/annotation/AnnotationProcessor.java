package net.kapitencraft.lang.compiler.annotation;

import java.util.Set;

public interface AnnotationProcessor {

    Set<String> getSupportedTypes();

    void process(AnnotationMirror mirror, ProcessingEnvironment environment);
}