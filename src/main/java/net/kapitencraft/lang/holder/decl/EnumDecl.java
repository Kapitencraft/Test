package net.kapitencraft.lang.holder.decl;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.baked.BakedEnum;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;

public record EnumDecl(VarTypeParser parser, Compiler.ErrorLogger logger, PreviewClass target, Token name, String pck, LoxClass[] interfaces, SkeletonParser.EnumConstDecl[] constants, SkeletonParser.MethodDecl[] constructor, SkeletonParser.MethodDecl[] methods, SkeletonParser.FieldDecl[] fields, SkeletonParser.ClassConstructor<?>[] enclosed) implements SkeletonParser.ClassConstructor<BakedEnum> {

    @Override
    public BakedEnum construct(StmtParser stmtParser, ExprParser exprParser) {
        return null;
    }

    @Override
    public LoxClass createSkeleton() {
        return null;
    }
}