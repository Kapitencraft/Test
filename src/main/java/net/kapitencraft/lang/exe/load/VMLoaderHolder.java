package net.kapitencraft.lang.exe.load;

import net.kapitencraft.lang.holder.bytecode.ClassFile;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VMLoaderHolder extends ClassLoaderHolder<VMLoaderHolder> {
    private final byte[] data;
    private final ClassType type;
    public final String name;
    final ClassReference reference;

    public VMLoaderHolder(File file) {
        super(file);
        try {
            this.data = new FileInputStream(file).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String fileId = file.getPath().substring(12);
        String[] packages = fileId.substring(0, fileId.length() - 5).split("\\\\");
        StringBuilder pck = new StringBuilder(packages[0]);
        for (int i = 1; i < packages.length - 1; i++) {
            pck.append(".");
            pck.append(packages[i]);
        }
        this.name = packages[packages.length - 1];
        this.reference = new ClassReference(name, pck.toString());
        this.type = ClassType.valueOf(GsonHelper.getAsString(data, "TYPE").toUpperCase());
    }

    @Override
    public void applySkeleton() {

        ScriptedClass skeleton = switch (type) {
            case ENUM, CLASS -> SkeletonClass.fromCache(data, pck());
            case INTERFACE -> SkeletonInterface.fromCache(data, pck());
            case ANNOTATION -> SkeletonAnnotation.fromCache(data, pck());
        };
        this.reference.setTarget(skeleton);
    }

    public ScriptedClass loadClass() {
        ScriptedClass target;
        try {
            target = ClassFile.load(data);
            this.reference.setTarget(target);
            return target;
        } catch (Exception e) {
            System.err.println("Error Loading Class '" + reference.absoluteName() + "': " + e.getMessage());
        }
        return null;
    }
}
