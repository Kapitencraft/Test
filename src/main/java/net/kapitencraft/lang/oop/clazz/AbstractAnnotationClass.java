package net.kapitencraft.lang.oop.clazz;

import java.util.List;

public interface AbstractAnnotationClass extends LoxClass {

    List<String> getAbstracts();

    default String findAbstractProperty() {
        return getAbstracts().isEmpty() ? null : getAbstracts().get(0);
    }
}
