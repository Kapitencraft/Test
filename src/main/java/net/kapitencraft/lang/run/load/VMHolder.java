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
    private final String type;

    public VMHolder(File file, ClassHolder[] children) {
        super(file, children);
        try {
            this.data = Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.type = GsonHelper.getAsString(data, "TYPE");
    }

    @Override
    protected boolean isInterface() {
        return "interface".equals(type);
    }

    @Override
    public LoxClass createSkeleton()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(classHolder -> {
            classHolder.reference.setTarget(classHolder.createSkeleton());
            return classHolder.reference;
        }).toList();
        ClassReference[] enclosedClasses = enclosed.toArray(new ClassReference[0]);
        if (isInterface())
            return SkeletonInterface.fromCache(data, pck(), enclosedClasses);
        else if ("enum".equals(type))
            return SkeletonEnum.fromCache(data, pck(), enclosedClasses);
        else if ("annotation".equals(type))
            return SkeletonAnnotation.fromCache(data, pck(), enclosedClasses);
        else
            return SkeletonClass.fromCache(data, pck(), enclosedClasses);
    }

    @Override
    public LoxClass loadClass()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(ClassHolder::loadClass).map(ClassReference::of).toList();
        LoxClass target;
        if (isInterface())
            target = GeneratedInterface.load(data, enclosed, pck());
        else if ("enum".equals(type))
            target = GeneratedEnum.load(data, enclosed, pck());
        else if ("annotation".equals(type))
            target = GeneratedAnnotation.load(data, enclosed, pck());
        else
            target = GeneratedClass.load(data, enclosed, pck());

        this.reference.setTarget(target);
        return target;
    }
}
