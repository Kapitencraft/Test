package net.kapitencraft.lang.oop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compile.parser.SkeletonParser;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class SkeletonMethod implements LoxCallable {
    private final List<? extends LoxClass> args;
    private final LoxClass retType;
    private final boolean isAbstract;

    public SkeletonMethod(List<? extends LoxClass> args, LoxClass retType, boolean isAbstract) {
        this.args = args;
        this.retType = retType;
        this.isAbstract = isAbstract;
    }

    public static SkeletonMethod create(SkeletonParser.MethodDecl decl) {
        return new SkeletonMethod(decl.params().stream().map(Pair::left).toList(), decl.type(), decl.isAbstract());
    }

    public static SkeletonMethod fromJson(JsonObject object) {
        LoxClass retType = ClassLoader.loadClassReference(object, "retType");
        List<? extends LoxClass> args = GsonHelper.getAsJsonArray(object, "params").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(object1 -> ClassLoader.loadClassReference(object1, "type"))
                .toList();
        List<String> flags = ClassLoader.readFlags(object);
        return new SkeletonMethod(args, retType, flags.contains("isAbstract"));
    }

    @Override
    public int arity() {
        return args.size();
    }

    @Override
    public LoxClass type() {
        return retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
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
}
