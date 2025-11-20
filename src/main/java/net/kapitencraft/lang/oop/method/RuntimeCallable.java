package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.bytecode.storage.LineNumberTable;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.StringReader;

import java.util.List;

public class RuntimeCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final ClassReference[] params;
    private final Chunk body;
    private final short modifiers;
    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeCallable(ClassReference retType, List<ClassReference> params, Chunk body, short modifiers, RuntimeAnnotationClassInstance[] annotations) {
        this.retType = retType;
        this.params = params.toArray(ClassReference[]::new);
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public static RuntimeCallable load(JsonObject data) {
        ClassReference retType = VarTypeManager.parseType(new StringReader(GsonHelper.getAsString(data, "retType")));
        JsonArray paramData = GsonHelper.getAsJsonArray(data, "params");

        List<ClassReference> params = paramData.asList().stream().map(JsonElement::getAsString).map(StringReader::new).map(VarTypeManager::parseType).toList();

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        Chunk b;
        if (Modifiers.isAbstract(modifiers)) b = null;
        else b  = Chunk.load(GsonHelper.getAsJsonObject(data, "body"));

        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new RuntimeCallable(retType, params, b, modifiers, annotations);
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
}