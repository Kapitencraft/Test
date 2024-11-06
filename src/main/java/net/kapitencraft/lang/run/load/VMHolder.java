package net.kapitencraft.lang.run.load;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class VMHolder extends ClassHolder {
    private final JsonObject data;
    private final boolean isInterface;

    public VMHolder(File file, ClassHolder[] children) {
        super(file, children);
        try {
            this.data = Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.isInterface = "interface".equals(GsonHelper.getAsString(data, "TYPE"));
    }

    @Override
    protected boolean isInterface() {
        return isInterface;
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
        else
            return SkeletonClass.fromCache(data, pck(), enclosedClasses);
    }

    @Override
    public LoxClass loadClass()  {
        List<LoxClass> enclosed = Arrays.stream(children).map(ClassHolder::loadClass).toList();
        LoxClass target;
        if (isInterface())
            target = GeneratedInterface.load(data, enclosed, pck());
        else
            target = GeneratedClass.load(data, enclosed, pck());

        return this.previewClass.apply(target);
    }
}
