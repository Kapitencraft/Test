package net.kapitencraft.lang.compiler.ast_optimize;

import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;

import java.util.List;

public interface ASTOptimization {

    void optimize(List<Stmt> code);

    default Expr.Literal createNumber(int value) {
        Expr.Literal literal = new Expr.Literal();
        literal.literal = new Token(TokenType.NUM, String.valueOf(value), new LiteralHolder(value, VarTypeManager.INTEGER), -1, -1);
        return literal;
    }
}
