package net.kapitencraft.lang.compiler;

public interface Modifiers {
    short FINAL = 1,
    STATIC = 2,
    ABSTRACT = 4;
    //no access modifiers to worry about. yay!

    static boolean isFinal(short modifiers) {
        return (modifiers & FINAL) != 0;
    }

    static boolean isStatic(short modifiers) {
        return (modifiers & STATIC) != 0;
    }

    static boolean isAbstract(short modifiers) {
        return (modifiers & ABSTRACT) != 0;
    }

    static short pack(boolean isFinal, boolean isStatic, boolean isAbstract) {
        return (short) ((isFinal ? 1 : 0) | ((isStatic ? 1 : 0) << 1) | ((isAbstract ? 1 : 0) << 2));
    }
}
