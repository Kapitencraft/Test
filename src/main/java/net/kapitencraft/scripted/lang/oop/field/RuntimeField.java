package net.kapitencraft.scripted.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.tool.GsonHelper;

public record RuntimeField(ClassReference type, int modifiers) implements ScriptedField {

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(this.modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(this.modifiers);
    }
}
