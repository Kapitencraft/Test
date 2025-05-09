package net.kapitencraft.lang.run.load;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.clazz.generated.RuntimeClass;
import net.kapitencraft.lang.oop.clazz.generated.RuntimeEnum;
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

public class VMLoaderHolder extends ClassLoaderHolder<VMLoaderHolder> {
    private final JsonObject data;
    private final ClassType type;
    public final String name;
    final ClassReference reference;

    public VMLoaderHolder(File file, List<VMLoaderHolder> children) {
        super(file, children.toArray(VMLoaderHolder[]::new));
        try {
            this.data = Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String fileId = file.getPath().substring(10);
        String[] packages = fileId.substring(0, fileId.length() - 5).split("[\\\\$]");
        StringBuilder pck = new StringBuilder(packages[0]);
        for (int i = 1; i < packages.length - 2; i++) {
            pck.append(".");
            pck.append(packages[i]);
        }
        this.name = packages[packages.length-1];
        this.reference = new ClassReference(name, pck.toString());
        this.type = ClassType.valueOf(GsonHelper.getAsString(data, "TYPE").toUpperCase());
    }

    @Override
    public void applySkeleton()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(classLoaderHolder -> {
            classLoaderHolder.applySkeleton();
            return classLoaderHolder.reference;
        }).toList();
        ClassReference[] enclosedClasses = enclosed.toArray(new ClassReference[0]);

        ScriptedClass skeleton = switch (type) {
            case ENUM -> SkeletonEnum.fromCache(data, pck(), enclosedClasses);
            case CLASS -> SkeletonClass.fromCache(data, pck(), enclosedClasses);
            case INTERFACE -> SkeletonInterface.fromCache(data, pck(), enclosedClasses);
            case ANNOTATION -> SkeletonAnnotation.fromCache(data, pck(), enclosedClasses);
        };
        this.reference.setTarget(skeleton);
    }

    public ScriptedClass loadClass()  {
        List<ClassReference> enclosed = Arrays.stream(children).map(VMLoaderHolder::loadClass).map(ClassReference::of).toList();
        ScriptedClass target;
        try {
            target = switch (type) {
                case ENUM -> RuntimeEnum.load(data, enclosed, pck());
                default -> RuntimeClass.load(data, enclosed, pck());
            };
            this.reference.setTarget(target);
            return target;
        } catch (Exception e) {
            System.err.println("Error Loading Class '" + reference.absoluteName() + "': " + e.getMessage());
        }
        return null;
    }
}
