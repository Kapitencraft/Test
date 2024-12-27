package net.kapitencraft.lang.natives.scripted.lang.annotation;

import net.kapitencraft.lang.oop.clazz.wrapper.NativeAnnotation;
import java.util.Map;

public class OverrideAnnotation extends NativeAnnotation {
    public OverrideAnnotation() {
        super("Override", "scripted.lang", Map.of());
    }
}
