package net.kapitencraft.lang.oop.method;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.StringReader;

import java.util.List;

public class SkeletonMethod implements ScriptedCallable {
    private final ClassReference[] args;
    private final ClassReference retType;
    private final short modifiers;

    public SkeletonMethod(ClassReference[] args, ClassReference retType, short modifiers) {
        this.args = args;
        this.retType = retType;
        this.modifiers = modifiers;
    }

    public static SkeletonMethod create(Holder.Method decl) {
        return create(decl.params(), decl.type().getReference(), decl.modifiers());
    }

    private static SkeletonMethod create(List<? extends Pair<SourceClassReference, String>> params, ClassReference type, short modifiers) {
        return new SkeletonMethod(params.stream().map(Pair::left).map(SourceClassReference::getReference).toArray(ClassReference[]::new), type, modifiers);
    }

    public static SkeletonMethod create(Holder.Constructor decl, ClassReference type) {
        return create(decl.params(), type, (short) 0);
    }

    public static SkeletonMethod fromJson(JsonObject object) {
        ClassReference retType = VarTypeManager.parseType(new StringReader(GsonHelper.getAsString(object, "retType")));
        ClassReference[] args = GsonHelper.getAsJsonArray(object, "params").asList().stream()
                .map(JsonElement::getAsString).map(StringReader::new).map(VarTypeManager::parseType)
                .toArray(ClassReference[]::new);

        short modifiers = object.has("modifiers") ? GsonHelper.getAsShort(object, "modifiers") : 0;
        return new SkeletonMethod(args, retType, modifiers);
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
        return null;
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
}
