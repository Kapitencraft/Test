package net.kapitencraft.lang.run.natives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, })
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeExcluded {
}
