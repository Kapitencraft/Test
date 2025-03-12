package net.kapitencraft.lang.oop.clazz;

public interface AbstractAnnotationClass extends ScriptedClass {

    @Override
    default ClassType getClassType() {
        return ClassType.ANNOTATION;
    }
}
