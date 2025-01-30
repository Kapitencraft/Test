package net.kapitencraft.lang.run.load;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedAnnotation;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedEnum;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedInterface;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonEnum;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class VMHolder extends ClassHolder {
    private final JsonObject data;
    private final ClassType type;

    public VMHolder(File file, ClassHolder[] children) {
        super(file, children);
        try {
            this.data = Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.type = ClassType.valueOf(GsonHelper.getAsString(data, "TYPE"));
    }

    @Override
    protected ClassType getType() {
        return type;
    }

    @Override
    public ScriptedClass createSkeleton()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(classHolder -> {
            classHolder.reference.setTarget(classHolder.createSkeleton());
            return classHolder.reference;
        }).toList();
        ClassReference[] enclosedClasses = enclosed.toArray(new ClassReference[0]);
        return switch (type) {
            case ENUM -> SkeletonEnum.fromCache(data, pck(), enclosedClasses);
            case CLASS -> SkeletonClass.fromCache(data, pck(), enclosedClasses);
            case INTERFACE -> SkeletonInterface.fromCache(data, pck(), enclosedClasses);
            case ANNOTATION -> SkeletonAnnotation.fromCache(data, pck(), enclosedClasses);
            case UNKNOWN -> throw new IllegalStateException("can not create class from unknown class type");
        };
    }

    @Override
    public ScriptedClass loadClass()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(ClassHolder::loadClass).map(ClassReference::of).toList();
        ScriptedClass target;
        target = switch (type) {
            case ANNOTATION -> GeneratedAnnotation.load(data, enclosed, pck());
            case INTERFACE -> GeneratedInterface.load(data, enclosed, pck());
            case CLASS -> GeneratedClass.load(data, enclosed, pck());
            case ENUM -> GeneratedEnum.load(data, enclosed, pck());
            case UNKNOWN -> throw new IllegalStateException("can not create class from unknown class type");
        };
        this.reference.setTarget(target);
        return target;
    }
}
