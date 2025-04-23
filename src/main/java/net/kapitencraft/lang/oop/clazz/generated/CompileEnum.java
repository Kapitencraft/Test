package net.kapitencraft.lang.oop.clazz.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.CompileEnumConstant;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;

import java.util.*;

public class CompileEnum implements CacheableClass {

    private final GeneratedMethodMap methods;
    private final GeneratedMethodMap staticMethods;

    private final DataMethodContainer constructor;

    private final Map<String, CompileField> allFields;
    private final Map<String, CompileEnumConstant> enumConstants;
    private final Map<String, CompileField> allStaticFields;

    private final Map<String, ClassReference> enclosing;

    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final CompileAnnotationClassInstance[] annotations;

    public CompileEnum(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods,
                       DataMethodContainer.Builder constructor,
                       Map<String, CompileField> allFields, Map<String, CompileEnumConstant> enumConstants, Map<String, CompileField> allStaticFields, Map<String, ClassReference> enclosing, ClassReference[] implemented, String name, String packageRepresentation, CompileAnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.constructor = constructor.create();
        this.allFields = allFields;
        this.enumConstants = enumConstants;
        this.allStaticFields = allStaticFields;
        this.enclosing = enclosing;
        this.implemented = implemented;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.annotations = annotations;
    }

    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "enum");
        object.addProperty("name", name);
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(ClassReference::absoluteName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        object.add("methods", methods.save(cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
        object.add("constructors", constructor.cache(cacheBuilder));
        {
            JsonObject constants = new JsonObject();
            enumConstants.forEach((name, field) -> constants.add(name, field.cache(cacheBuilder)));
            object.add("enumConstants", constants);
        }
        {
            JsonObject fields = new JsonObject();
            allFields.forEach((name, field) -> fields.add(name, field.cache(cacheBuilder)));
            object.add("fields", fields);
        }
        {
            JsonObject staticFields = new JsonObject();
            allStaticFields.forEach((name, field) -> staticFields.add(name, field.cache(cacheBuilder)));
            object.add("staticFields", staticFields);
        }

        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        return object;
    }

    @Override
    public CacheableClass[] enclosed() {
        return new CacheableClass[0];
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
