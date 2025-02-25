package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenType.C_BRACKET_C;

@SuppressWarnings("ThrowableNotThrown")
public class StmtParser extends ExprParser {

    public StmtParser(Compiler.ErrorLogger errorLogger, ClassReference fallback) {
        super(errorLogger, fallback);
    }

    private ClassReference funcRetType = ClassReference.of(VarTypeManager.VOID);
    private final Stack<Boolean> seenReturn = new Stack<>();
    private int loopIndex = 0;

    @Override
    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        super.apply(toParse, targetAnalyser);
        seenReturn.add(false);
    }

    private void seenReturn() {
        seenReturn.set(seenReturn.size()-1, true);
    }

    private void pushScope() {
        varAnalyser.push();
        seenReturn.add(false);
    }

    private void popScope() {
        varAnalyser.pop();
        seenReturn.pop();
    }

    private Stmt declaration() {
        if (seenReturn.peek()) {
            error(peek(), "unreachable statement");
        }
        try {
            if (match(FINAL)) return varDeclaration(true, consumeVarType(generics));
            if (match(IDENTIFIER)) {
                Token id = previous();
                ClassReference loxClass = parser.getClass(id.lexeme());
                if (loxClass != null && !check(DOT)) return varDeclaration(false, loxClass);
                current--; //jump back if it isn't a var decl
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt.VarDecl varDecl(boolean isFinal, ClassReference type, Token name) {

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        createVar(name, type, initializer != null, isFinal);

        if (initializer != null) {
            checkVarType(name, initializer);
        }

        consumeEndOfArg();
        return new Stmt.VarDecl(name, type, initializer, isFinal);
    }

    private Stmt varDeclaration(boolean isFinal, ClassReference type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        return varDecl(isFinal, type, name);
    }

    private Stmt statement() {
        try {
            if (match(RETURN)) return returnStatement();
            if (match(TRY)) return tryStatement();
            if (match(THROW)) return thrStatement();
            if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
            if (match(FOR)) return forStatement();
            if (match(IF)) return ifStatement();
            if (match(WHILE)) return whileStatement();
            if (match(C_BRACKET_O)) return new Stmt.Block(block("block"));
            if (check(IDENTIFIER)) {
                Optional<SourceClassReference> type = tryConsumeVarType(generics);
                if (type.isPresent()) {
                    return varDeclaration(false, type.get());
                }
            }

            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt tryStatement() {
        consumeCurlyOpen("try statement");
        Stmt.Block tryBlock = new Stmt.Block(block("try statement"));
        Token brClose = previous();

        List<Pair<Pair<List<ClassReference>,Token>, Stmt.Block>> catches = new ArrayList<>(); //what an insane varType
        while (match(CATCH)) {
            List<ClassReference> targets = new ArrayList<>();
            consumeBracketOpen("catch");
            do {
                targets.add(consumeVarType(generics));
            } while (match(SINGLE_OR));
            pushScope();
            Token name = consumeIdentifier();
            consumeBracketClose("catch");
            createVar(name, VarTypeManager.THROWABLE, true, false);
            consumeCurlyOpen("catch statement");
            Stmt.Block block = new Stmt.Block(block("catch statement"));
            popScope();
            catches.add(Pair.of(
                    Pair.of(
                            targets,
                            name
                    ),
                    block
            ));
        }
        Stmt.Block finallyBlock = null;
        if (match(FINALLY)) {
            consumeCurlyOpen("finally statement");
            finallyBlock = new Stmt.Block(block("finally statement"));
        } else if (catches.isEmpty()) error(brClose, "expected 'catch' or 'finally'");
        return new Stmt.Try(tryBlock, catches, finallyBlock);
    }

    private Stmt thrStatement() {
        Token keyword = previous();
        expectType(VarTypeManager.THROWABLE);
        Expr val = expression();
        expectType(val, VarTypeManager.THROWABLE);
        consumeEndOfArg();
        popExpectation();
        seenReturn();
        return new Stmt.Throw(keyword, val);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        if (funcRetType == VarTypeManager.VOID && value != null) error(keyword, "incompatible types: unexpected return value.");
        else if (value != null) expectType(value, funcRetType);
        else if (funcRetType != VarTypeManager.VOID) error(keyword, "incompatible types: missing return value.");

        consumeEndOfArg();
        seenReturn();
        return new Stmt.Return(keyword, value);
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        if (loopIndex <= 0) error(token, "'" + token.lexeme() + "' can only be used inside loops");
        consumeEndOfArg();
        seenReturn();
        return new Stmt.LoopInterruption(token);
    }

    private Stmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");
        pushScope();
        loopIndex++;

        Optional<SourceClassReference> type = tryConsumeVarType(generics);

        Stmt initializer;
        if (type.isPresent()) {
            Token name = consumeIdentifier();
            if (match(COLON)) {
                ClassReference arrayType = type.get().array();
                expectType(arrayType);
                Expr init = expression();
                popExpectation();
                expectType(init, arrayType);
                consumeBracketClose("for");
                varAnalyser.add(name.lexeme(), type.get(), true, false);
                Stmt stmt = statement();
                loopIndex--;
                popScope();
                return new Stmt.ForEach(type.get(), name, init, stmt);
            }
            initializer = varDecl(false, type.get(), name);
        } else if (match(EOA)) {
            initializer = null;
        } else if (match(IDENTIFIER) && parser.hasClass(previous().lexeme())) {
            initializer = varDeclaration(false, parser.getClass(previous().lexeme()));
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        expectCondition(condition);
        consumeEndOfArg();

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        Stmt body = statement();

        popScope();
        loopIndex--;

        return new Stmt.For(initializer, condition, increment, body, keyword);
    }

    private Stmt ifStatement() {
        Token statement = previous();
        consumeBracketOpen("if");
        Expr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        List<Pair<Expr, Stmt>> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            this.expectCondition(elifCondition);
            consumeBracketClose("elif condition");
            Stmt elifStmt = statement();
            elifs.add(Pair.of(elifCondition, elifStmt));
        }

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch, elifs, statement);
    }

    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("while condition");
        this.loopIndex++;
        this.pushScope();
        Stmt body = statement();
        this.popScope();
        this.loopIndex--;

        return new Stmt.While(condition, body, keyword);
    }

    private List<Stmt> block(String name) {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose(name);
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeEndOfArg();
        return new Stmt.Expression(expr);
    }

    public List<Stmt> parse() {
        if (tokens.length == 0) return List.of();
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(statement());
        return stmts;
    }

    public void applyMethod(List<? extends Pair<? extends ClassReference, String>> params, ClassReference targetClass, ClassReference superclass, ClassReference funcRetType) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (targetClass != null) this.varAnalyser.add("this", targetClass, true, true);
        if (superclass != null) this.varAnalyser.add("super", superclass, true, true);
        for (Pair<? extends ClassReference, String> param : params) {
            varAnalyser.add(param.right(), param.left(), true, false);
        }
    }

    public void popMethod() {
        this.popScope();
        funcRetType = VarTypeManager.VOID.reference();
    }

    public void applyStaticMethod(List<? extends Pair<? extends ClassReference, String>> params, ClassReference funcRetType) {
        this.pushScope();
        this.funcRetType = funcRetType;
        for (Pair<? extends ClassReference, String> param : params) {
            varAnalyser.add(param.right(), param.left(), true, false);
        }
    }
}
