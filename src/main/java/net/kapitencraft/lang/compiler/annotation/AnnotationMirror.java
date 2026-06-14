package net.kapitencraft.lang.compiler.annotation;

import java.util.Map;

public class AnnotationMirror {
    private final String type;
    private final Map<String, Object> entries;

    public AnnotationMirror(String type, Map<String, Object> entries) {
        this.type = type;
        this.entries = entries;
    }

    /**
     * @return the name of the class, in bytecode format
     */
    public String getType() {
        return type;
    }

    /**
     * @param name the name of the annotation method
     * @return the value of named field. will always return a value for
     */
    public Object getValue(String name) {
        return this.entries.get(name);
    }
}
