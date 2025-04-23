package net.kapitencraft.lang.oop.field;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public class CompileEnumConstant implements ScriptedField {

    private final int ordinal;
    private final String name;
    private final int constructorOrdinal;
    private final CompileExpr[] args;

    public CompileEnumConstant(int ordinal, String name, int constructorOrdinal, CompileExpr[] args) {
        this.ordinal = ordinal;
        this.name = name;
        this.constructorOrdinal = constructorOrdinal;
        this.args = args;
    }


    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        throw new IllegalAccessError("can not initializer compile enum constant");
    }

    @Override
    public ClassReference type() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    public JsonObject cache(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("ordinal", this.ordinal);
        object.addProperty("name", this.name);
        object.addProperty("constructorOrdinal", this.constructorOrdinal);
        object.add("args", builder.saveArgs(this.args));
        return object;
    }
}
