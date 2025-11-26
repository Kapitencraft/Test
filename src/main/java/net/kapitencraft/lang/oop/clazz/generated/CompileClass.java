package net.kapitencraft.lang.oop.clazz.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.*;

public final class CompileClass implements CacheableClass {
    private final GeneratedMethodMap methods;
    private final Map<String, DataMethodContainer> allMethods;

    private final Map<String, CompileField> allFields;

    private final ClassReference superclass;
    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final short modifiers;

    private final Annotation[] annotations;

    public CompileClass(Map<String, DataMethodContainer> methods,
                        Map<String, CompileField> fields,
                        ClassReference superclass, String name, String packageRepresentation,
                        ClassReference[] implemented,
                        short modifiers, Annotation[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allMethods = methods;
        this.allFields = fields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);
        object.addProperty("superclass", VarTypeManager.getClassName(superclass));
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(VarTypeManager::getClassName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        object.add("methods", methods.save(cacheBuilder));
        {
            JsonObject fields = new JsonObject();
            allFields.forEach((name, field) -> fields.add(name, field.cache(cacheBuilder)));
            object.add("fields", fields);
        }

        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        if (this.modifiers != 0) object.addProperty("modifiers", modifiers);

        return object;
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + allMethods + ", " +
                "fields=" + allFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String pck() {
        return this.packageRepresentation;
    }
}