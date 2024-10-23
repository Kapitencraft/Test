package net.kapitencraft.lang.oop.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.run.CacheLoader;
import net.kapitencraft.lang.run.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;

public class GeneratedField extends LoxField {
    private final Stmt.VarDecl decl;

    public GeneratedField(Stmt.VarDecl decl) {
        this.decl = decl;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        if (!hasInit()) {
            if (decl.type instanceof PrimitiveClass prim) {
                return prim.defaultValue();
            }
            return null;
        }
        return interpreter.evaluate(decl.initializer);
    }

    public boolean hasInit() {
        return decl.initializer != null;
    }

    @Override
    public LoxClass getType() {
        return decl.type;
    }

    public JsonElement cache(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.add("name", decl.name.toJson());
        object.addProperty("type", decl.type.absoluteName());
        if (hasInit()) object.add("init", cacheBuilder.cache(decl.initializer));
        if (decl.isFinal) object.addProperty("isFinal", true);
        return object;
    }

    public static GeneratedField fromJson(JsonObject object) {
        Token name = Token.readFromSubObject(object, "name");
        LoxClass type = ClassLoader.loadClassReference(object, "type");
        Expr init = object.has("init") ? CacheLoader.readSubExpr(object, "init") : null;
        boolean isFinal = object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal");
        return new GeneratedField(new Stmt.VarDecl(name, type, init, isFinal));
    }

    @Override
    public boolean isFinal() {
        return decl.isFinal;
    }
}
