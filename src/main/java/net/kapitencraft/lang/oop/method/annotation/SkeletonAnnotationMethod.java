package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.load.ClassLoader;

public class SkeletonAnnotationMethod extends AnnotationCallable {
    private final boolean hasValue;
    
    public SkeletonAnnotationMethod(LoxClass type, boolean hasValue) {
        super(type, null);
        this.hasValue = hasValue;
    }

    public static SkeletonAnnotationMethod fromJson(JsonObject object) {
        LoxClass type = ClassLoader.loadClassReference(object, "type");
        boolean hasValue = object.has("val");
        return new SkeletonAnnotationMethod(type, hasValue);
    }

    @Override
    public boolean isAbstract() {
        return !hasValue;
    }
}
