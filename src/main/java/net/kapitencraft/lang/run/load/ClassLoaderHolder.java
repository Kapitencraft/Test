package net.kapitencraft.lang.run.load;

import net.kapitencraft.lang.oop.clazz.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public abstract class ClassLoaderHolder<T extends ClassLoaderHolder<T>> {
    public final File file;
    protected final T[] children;

    public ClassLoaderHolder(File file, T[] children) {
        this.file = file;
        this.children = children;
    }

    protected String pck() {
        return ClassLoader.pck(file);
    }

    public abstract void applySkeleton();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClassLoaderHolder<T>) obj;
        return Objects.equals(this.file, that.file) &&
                Arrays.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, Arrays.hashCode(children));
    }

    @Override
    public String toString() {
        return "ClassHolder[" +
                "file=" + file + ", " +
                "children=" + Arrays.toString(children) + ']';
    }
}
