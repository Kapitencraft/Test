package net.kapitencraft.lang.compile.visitor;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.compile.VarTypeParser;
import net.kapitencraft.lang.compile.analyser.VarAnalyser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import static net.kapitencraft.lang.holder.token.TokenTypeCategory.*;
import static net.kapitencraft.lang.holder.token.TokenTypeCategory.EQUALITY;

public class RetTypeFinder implements Expr.Visitor<LoxClass> {
    private final VarTypeParser parser;
    private final VarAnalyser varAnalyser;

    public LoxClass findRetType(Expr expr) {
        return expr.accept(this);
    }

    public RetTypeFinder(VarTypeParser parser, VarAnalyser varAnalyser) {
        this.parser = parser;
        this.varAnalyser = varAnalyser;
    }

    @Override
    public LoxClass visitAssignExpr(Expr.Assign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitBinaryExpr(Expr.Binary expr) {
        LoxClass left = findRetType(expr.left);
        LoxClass right = findRetType(expr.right);
        TokenType type = expr.operator.type();
        if (type == TokenType.ADD && (left == VarTypeManager.STRING || right == VarTypeManager.STRING)) return VarTypeManager.STRING; //check if at least one of the values is string
        if (type.isCategory(COMPARATORS) || type.isCategory(EQUALITY)) return VarTypeManager.BOOLEAN;
        return left;
    }

    @Override
    public LoxClass visitWhenExpr(Expr.When expr) {
        return findRetType(expr.ifTrue);
    }

    @Override
    public LoxClass visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public LoxClass visitInstCallExpr(Expr.InstCall expr) {
        return findRetType(expr.callee).getMethodByOrdinal(expr.name.lexeme(), expr.methodOrdinal).type();
    }

    @Override
    public LoxClass visitGetExpr(Expr.Get expr) {
        return findRetType(expr.object).getFieldType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSetExpr(Expr.Set expr) {
        return findRetType(expr.object).getFieldType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSpecialSetExpr(Expr.SpecialSet expr) {
        return findRetType(expr.callee).getFieldType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSwitchExpr(Expr.Switch expr) {
        return findRetType(expr.defaulted);
    }

    @Override
    public LoxClass visitCastCheckExpr(Expr.CastCheck expr) {
        return VarTypeManager.BOOLEAN;
    }

    @Override
    public LoxClass visitGroupingExpr(Expr.Grouping expr) {
        return findRetType(expr.expression);
    }

    @Override
    public LoxClass visitLiteralExpr(Expr.Literal expr) {
        return expr.value.literal().type();
    }

    @Override
    public LoxClass visitLogicalExpr(Expr.Logical expr) {
        return VarTypeManager.BOOLEAN;
    }

    @Override
    public LoxClass visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public LoxClass visitVarRefExpr(Expr.VarRef expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitFuncRefExpr(Expr.FuncRef expr) {
        return null; //removed soon
    }

    @Override
    public LoxClass visitConstructorExpr(Expr.Constructor expr) {
        return expr.target;
    }
}