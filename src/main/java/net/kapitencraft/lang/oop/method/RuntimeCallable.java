package net.kapitencraft.lang.oop.method;

import com.google.common.collect.Multimap;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeOwner;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

import java.util.Collection;
import java.util.List;

public class RuntimeCallable implements ScriptedCallable, AttributeOwner {
    private final ClassReference retType;
    private final ClassReference[] params;
    private final ClassReference[] thrown;
    private final Chunk body;
    private final short modifiers;
    private final Annotation[] annotations;
    private final Multimap<String, AttributeInfo> attributes;

    public RuntimeCallable(ClassReference retType, List<ClassReference> params, List<ClassReference> thrown, Chunk body, short modifiers, Annotation[] annotations, AttributeInfo[] attributes) {
        this.retType = retType;
        this.params = params.toArray(ClassReference[]::new);
        this.thrown = thrown.toArray(ClassReference[]::new);
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
        this.attributes = AttributeOwner.createLookup(attributes);
    }

    @Override
    public <T extends AttributeInfo> Collection<T> getAttribute(String name) {
        return (Collection<T>) attributes.get(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("do not call directly!");
    }

    @Override
    public Chunk getChunk() {
        return this.body;
    }

    @Override
    public boolean isAbstract() {
        return body == null;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public short modifiers() {
        return modifiers;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params;
    }

    @Override
    public ClassReference[] thrown() {
        return thrown;
    }
}