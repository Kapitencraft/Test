package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;

import java.util.List;
import java.util.Map;

public class GeneratedEnum implements CacheableClass {
    private final MethodMap methods;
    private final MethodMap staticMethods;
    private final Map<String, DataMethodContainer> allMethods;

    private final MethodLookup lookup;

    private final ConstructorContainer constructor;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, LoxClass> enclosing;

    private final LoxClass superclass;
    private final LoxClass[] implemented;
    private final String name;
    private final String packageRepresentation;


    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        return null;
    }

    @Override
    public CacheableClass[] enclosing() {
        return new CacheableClass[0];
    }

    @Override
    public MethodLookup methods() {
        return null;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String packageRepresentation() {
        return "";
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return 0;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return 0;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    @Override
    public MethodMap getMethods() {
        return null;
    }
}
