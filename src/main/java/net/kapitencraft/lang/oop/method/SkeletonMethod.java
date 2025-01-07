package net.kapitencraft.lang.oop.method;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class SkeletonMethod implements ScriptedCallable {
    private final List<ClassReference> args;
    private final ClassReference retType;
    private final boolean isAbstract, isFinal;

    public SkeletonMethod(List<ClassReference> args, ClassReference retType, boolean isAbstract, boolean isFinal) {
        this.args = args;
        this.retType = retType;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public static SkeletonMethod create(SkeletonParser.MethodDecl decl) {
        return new SkeletonMethod(decl.params().stream().map(Pair::left).toList(), decl.type(), decl.isAbstract(), decl.isFinal());
    }

    public static SkeletonMethod fromJson(JsonObject object) {
        ClassReference retType = ClassLoader.loadClassReference(object, "retType");
        List<ClassReference> args = GsonHelper.getAsJsonArray(object, "params").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(object1 -> ClassLoader.loadClassReference(object1, "type"))
                .toList();
        List<String> flags = ClassLoader.readFlags(object);
        return new SkeletonMethod(args, retType, flags.contains("isAbstract"), flags.contains("isFinal"));
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
    public ClassReference type() {
        return retType;
    }

    @Override
    public List<ClassReference> argTypes() {
        return args;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
