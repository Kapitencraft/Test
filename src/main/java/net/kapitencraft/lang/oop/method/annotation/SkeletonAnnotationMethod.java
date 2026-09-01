package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.load.ClassLoader;

public class SkeletonAnnotationMethod extends AnnotationCallable {
    private final boolean hasValue;
    
    public SkeletonAnnotationMethod(ClassReference type, boolean hasValue, ClassReference declaring) {
        super(type, null, declaring);
        this.hasValue = hasValue;
    }

    public static SkeletonAnnotationMethod fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        boolean hasValue = object.has("val");
        ClassReference declaring = ClassLoader.loadClassReference(object, "declaring");
        return new SkeletonAnnotationMethod(type, hasValue, declaring);
    }

    @Override
    public boolean isAbstract() {
        return !hasValue;
    }
}
