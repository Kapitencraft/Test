package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.holder.class_ref.ClassReference;

public interface ScriptedField {

    ClassReference type();

    short modifiers();

    boolean isFinal();

    boolean isStatic();
}