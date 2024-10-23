package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.run.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonClass implements LoxClass {
    private final String name;
    private final String pck;

    private final LoxClass superclass;
    private final Map<String, LoxClass> fields;
    private final Map<String, LoxClass> staticFields;

    private final Map<String, PreviewClass> enclosed;

    private final Map<String, DataMethodContainer> methods;
    private final Map<String, DataMethodContainer> staticMethods;
    private final ConstructorContainer constructor;

    private final boolean isAbstract;
    private final boolean isFinal;

    public SkeletonClass(String name, String pck, LoxClass superclass,
                         Map<String, LoxClass> staticFields, Map<String, LoxClass> fields,
                         Map<String, PreviewClass> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                         boolean isAbstract, boolean isFinal) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.constructor = constructor.build(this);
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public SkeletonClass(String name, String pck, LoxClass superclass,
                         Map<String, LoxClass> staticFields, Map<String, LoxClass> fields,
                         Map<String, PreviewClass> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer constructor,
                         boolean isAbstract, boolean isFinal) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.constructor = constructor;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public static SkeletonClass create(Compiler.ErrorLogger logger, SkeletonParser.ClassDecl decl) {
        //fields
        ImmutableMap.Builder<String, LoxClass> fields = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<String, LoxClass> staticFields = new ImmutableMap.Builder<>();
        List<String> finalFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : decl.fields()) {
            if (field.isStatic()) staticFields.put(field.name().lexeme(), field.type());
            else {
                fields.put(field.name().lexeme(), field.type());
                if (field.isFinal() && field.body() == null) finalFields.add(field.name().lexeme());
            }
        }

        //enclosed classes
        ImmutableMap.Builder<String, PreviewClass> enclosed = new ImmutableMap.Builder<>();
        for (SkeletonParser.ClassDecl enclosedDecl : decl.enclosed()) {
            SkeletonClass skeletonClass = create(logger, enclosedDecl);
            enclosedDecl.target().apply(skeletonClass);
            enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
        }

        //methods
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (SkeletonParser.MethodDecl method : decl.methods()) {
            if (method.isStatic()) {
                staticMethods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(decl.name()));
                DataMethodContainer.Builder builder = staticMethods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            } else {
                methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(decl.name()));
                DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            }
        }

        //constructor
        ConstructorContainer.Builder constructorBuilder = new ConstructorContainer.Builder(finalFields, decl.name());
        for (SkeletonParser.MethodDecl constructor : decl.constructors()) {
            constructorBuilder.addMethod(
                    logger,
                    SkeletonMethod.create(constructor),
                    constructor.name()
            );
        }

        return new SkeletonClass(
                decl.name().lexeme(),
                null, decl.superclass(),
                staticFields.build(),
                fields.build(),
                enclosed.build(),
                DataMethodContainer.bakeBuilders(methods),
                DataMethodContainer.bakeBuilders(staticMethods),
                constructorBuilder,
                decl.isAbstract(),
                decl.isFinal());
    }

    public static SkeletonClass fromCache(JsonObject data, String pck, PreviewClass[] enclosed) {
            String name = GsonHelper.getAsString(data, "name");
        LoxClass superclass = VarTypeManager.getClassForName(GsonHelper.getAsString(data, "superclass"));
        ImmutableMap.Builder<String, DataMethodContainer> methods = new ImmutableMap.Builder<>();
        {
            JsonObject methodData = GsonHelper.getAsJsonObject(data, "methods");
            methodData.asMap().forEach((s, element) -> {
                SkeletonMethod[] methodDeclarations =
                element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(SkeletonMethod::fromJson)
                        .toArray(SkeletonMethod[]::new);
                methods.put(s, new DataMethodContainer(methodDeclarations));
            });
        }
        ImmutableMap.Builder<String, DataMethodContainer> staticMethods = new ImmutableMap.Builder<>();
        {
            JsonObject methodData = GsonHelper.getAsJsonObject(data, "staticMethods");
            methodData.asMap().forEach((s, element) -> {
                SkeletonMethod[] methodDeclarations =
                        element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(SkeletonMethod::fromJson)
                                .toArray(SkeletonMethod[]::new);
                methods.put(s, new DataMethodContainer(methodDeclarations));
            });
        }
        ConstructorContainer constructorContainer = new ConstructorContainer(
                GsonHelper.getAsJsonArray(data, "constructor").asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(SkeletonMethod::fromJson)
                        .toArray(SkeletonMethod[]::new)
        );
        ImmutableMap.Builder<String, LoxClass> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "fields");
            fieldData.asMap().forEach((s, element) ->
                    fields.put(s, ClassLoader.loadClassReference(element.getAsJsonObject(), "type"))
            );
        }
        ImmutableMap.Builder<String, LoxClass> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) ->
                    staticFields.put(s, ClassLoader.loadClassReference(element.getAsJsonObject(), "type"))
            );
        }

        List<String> flags = ClassLoader.readFlags(data);

        return new SkeletonClass(name, pck, superclass,
                staticFields.build(), fields.build(),
                Arrays.stream(enclosed).collect(Collectors.toMap(LoxClass::name, Function.identity())),
                methods.build(), staticMethods.build(),
                constructorContainer,
                flags.contains("isAbstract"), flags.contains("isFinal")
        );
    }

    @Override
    public Object getStaticField(String name) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        return pck + "." + name;
    }

    @Override
    public boolean hasField(String name) {
        return this.fields.containsKey(name) || LoxClass.super.hasField(name);
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Util.nonNullElse(fields.get(name), LoxClass.super.getFieldType(name));
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return staticFields.get(name);
    }

    @Override
    public LoxCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethod(args);
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethodOrdinal(args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.containsKey(name);
    }

    @Override
    public DataMethodContainer getConstructor() {
        return constructor;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public LoxCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return methods.get(name).getMethodOrdinal(types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return enclosed.get(name);
    }
}
