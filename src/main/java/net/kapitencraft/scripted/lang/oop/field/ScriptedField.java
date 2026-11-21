package net.kapitencraft.scripted.lang.oop.field;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public interface ScriptedField {

    ClassReference type();

    int modifiers();

    boolean isFinal();

    boolean isStatic();
}