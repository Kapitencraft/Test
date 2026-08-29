package net.kapitencraft.lang.compiler;

public interface Modifiers {
    int SF = Modifiers.STATIC | Modifiers.FINAL;

    int STATIC = 8,
            FINAL = 0x10,
            NATIVE = 0x100,
            INTERFACE = 0x200,
            ABSTRACT = 0x400,
            SYNTHETIC = 0x1000,
            ANNOTATION = 0x2000,
            ENUM = 0x4000
    ;
    //no access modifiers to worry about. yay!
    //short has 16 bits. should be sufficient

    static boolean isFinal(int modifiers) {
        return (modifiers & FINAL) != 0;
    }

    static boolean isStatic(int modifiers) {
        return (modifiers & STATIC) != 0;
    }

    static boolean isAbstract(int modifiers) {
        return (modifiers & ABSTRACT) != 0;
    }

    static int pack(int... modifiers) {
        int m = 0;
        for (int modifier : modifiers) {
            m |= modifier;
        }
        return m;
    }

    /**
     * extracts usable scripted modifiers from the given java modifiers.
     * <br> used for native class loading
     * @param javaMods the given java modifiers
     * @return the extracted scripted mods
     * @see java.lang.reflect.Modifier
     */
    static int fromJavaMods(int javaMods) {
        return javaMods & 0x7718;
    }
}
