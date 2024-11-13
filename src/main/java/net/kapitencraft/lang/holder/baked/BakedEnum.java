package net.kapitencraft.lang.holder.baked;

import com.google.common.cache.Cache;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

public record BakedEnum(
        Compiler.ErrorLogger logger,
        PreviewClass target,
        Pair<Token, GeneratedCallable>[] constructor,
        Pair<Token, GeneratedCallable>[] methods,
        Pair<Token, GeneratedCallable>[] staticMethods,
        Stmt.VarDecl[] fields, Stmt.VarDecl[] staticFields,
        LoxClass[] interfaces,
        Token name, String pck, Compiler.ClassBuilder[] enclosed
) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {
        return new GeneratedEnum();
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.ENUM;
    }
}

