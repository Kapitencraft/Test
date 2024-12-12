package net.kapitencraft.lang.run.load;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedEnum;
import net.kapitencraft.lang.oop.clazz.generated.GeneratedInterface;
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
        this.type =GsonHelper.getAsString(data, "TYPE");
    }

    @Override
    protected boolean isInterface() {
        return "interface".equals(type);
    }

    @Override
    public LoxClass createSkeleton()  {
        List<PreviewClass> enclosed = Arrays.stream(children).map(classHolder -> {
            classHolder.previewClass.apply(classHolder.createSkeleton());
            return classHolder.previewClass;
        }).toList();
        PreviewClass[] enclosedClasses = enclosed.toArray(new PreviewClass[0]);
        if (isInterface())
            return SkeletonInterface.fromCache(data, pck(), enclosedClasses);
        else if ("enum".equals(type))
            return SkeletonEnum.fromCache(data, pck(), enclosedClasses);
        else
            return SkeletonClass.fromCache(data, pck(), enclosedClasses);
    }

    @Override
    public LoxClass loadClass()  {
        List<LoxClass> enclosed = Arrays.stream(children).map(ClassHolder::loadClass).toList();
        LoxClass target;
        if (isInterface())
            target = GeneratedInterface.load(data, enclosed, pck());
        else if ("enum".equals(type))
            target = GeneratedEnum.load(data, enclosed, pck());
        else
            target = GeneratedClass.load(data, enclosed, pck());

        return this.previewClass.apply(target);
    }
}
