package net.kapitencraft.lang.oop.method;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exe.load.ClassLoader;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.attribute.ConstructorHolder;
import net.kapitencraft.lang.holder.oop.attribute.MethodHolder;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class SkeletonMethod implements ScriptedCallable {
    private final ClassReference[] args;
    private final ClassReference[] thrown;
    private final ClassReference retType;
    private final int modifiers;
    private final ClassReference declaring;

    public SkeletonMethod(ClassReference[] args, ClassReference[] thrown, ClassReference retType, int modifiers, ClassReference declaring) {
        this.args = args;
        this.thrown = thrown;
        this.retType = retType;
        this.modifiers = modifiers;
        this.declaring = declaring;
    }

    public static SkeletonMethod create(MethodHolder decl, ClassReference declaring) {
        return create(decl.params(), decl.thrown(), decl.type().getReference(), declaring, decl.modifiers());
    }

    private static SkeletonMethod create(List<? extends Pair<SourceReference, String>> params, List<SourceReference> thrown, ClassReference type, ClassReference declaring, int modifiers) {
        return new SkeletonMethod(
                params.stream()
                        .map(Pair::first)
                        .map(SourceReference::getReference)
                        .toArray(ClassReference[]::new),
                thrown.stream()
                        .map(SourceReference::getReference)
                        .toArray(ClassReference[]::new),
                type,
                modifiers,
                declaring
        );
    }

    public static SkeletonMethod create(ConstructorHolder decl, ClassReference type) {
        return create(decl.params(), decl.thrown(), type, type, (short) 0);
    }

    public static SkeletonMethod createNative(ClassReference[] args, ClassReference[] thrown, ClassReference retType, int modifiers, ClassReference declaring) {
        return new SkeletonMethod(args, thrown, retType, modifiers, declaring);
    }


    public static SkeletonMethod fromJson(JsonObject object) {
        ClassReference retType = ClassLoader.loadClassReference(object, "retType");
        ClassReference[] args = GsonHelper.getAsClassReferenceList(object, "params").toArray(ClassReference[]::new);

        ClassReference[] thrown = GsonHelper.getAsClassReferenceList(object, "thrown").toArray(ClassReference[]::new);

        short modifiers = object.has("modifiers") ? GsonHelper.getAsShort(object, "modifiers") : 0;

        ClassReference declaring = ClassLoader.loadClassReference(object, "declaring");
        return new SkeletonMethod(args, thrown, retType, modifiers, declaring);
    }

    public static ImmutableMap<String, DataMethodContainer> readFromCache(JsonObject data, String subElementName) {
        ImmutableMap.Builder<String, DataMethodContainer> methods = new ImmutableMap.Builder<>();
        JsonObject methodData = GsonHelper.getAsJsonObject(data, subElementName);
        methodData.asMap().forEach((s, element) -> {
            SkeletonMethod[] methodDeclarations =
                    element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(SkeletonMethod::fromJson)
                            .toArray(SkeletonMethod[]::new);
            methods.put(s, new DataMethodContainer(methodDeclarations));
        });
        return methods.build();
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return args;
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not call skeleton method");
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
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
    public ClassReference[] thrown() {
        return thrown;
    }

    @Override
    public ClassReference declaringClass() {
        return declaring;
    }
}
