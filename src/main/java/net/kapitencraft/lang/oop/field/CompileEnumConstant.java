package net.kapitencraft.lang.oop.field;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

@Deprecated
public class CompileEnumConstant implements ScriptedField {

    private final int ordinal;
    private final String name;

    public CompileEnumConstant(int ordinal, String name) {
        this.ordinal = ordinal;
        this.name = name;
    }

    @Override
    public ClassReference type() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public JsonObject cache(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("ordinal", this.ordinal);
        object.addProperty("name", this.name);
        //object.add("args", builder.saveArgs(this.args)); TODO
        return object;
    }
}
