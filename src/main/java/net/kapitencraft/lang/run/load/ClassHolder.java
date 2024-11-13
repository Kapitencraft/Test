package net.kapitencraft.lang.run.load;

import net.kapitencraft.lang.oop.clazz.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public abstract class ClassHolder {
    public final File file;
    protected final ClassHolder[] children;
    public final PreviewClass previewClass;

    public ClassHolder(File file, ClassHolder[] children) {
        this.file = file;
        this.children = children;
        String[] enclosed = file.getName().replace(".scrc", "").split("\\$");
        this.previewClass = new PreviewClass(enclosed[enclosed.length-1].replace(".scr", ""), isInterface());
        //TODO fix call before constructors end
    }

    protected abstract boolean isInterface();

    protected String pck() {
        return ClassLoader.pck(file);
    }

    public abstract LoxClass createSkeleton();

    public abstract LoxClass loadClass();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClassHolder) obj;
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
