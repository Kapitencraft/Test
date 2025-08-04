package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenType.C_BRACKET_C;

@SuppressWarnings("ThrowableNotThrown")
public class StmtParser extends ExprParser {

    public StmtParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    private ClassReference funcRetType = VarTypeManager.VOID.reference();
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

    private CompileStmt declaration() {
        if (seenReturn.peek()) {
            error(peek(), "unreachable statement");
        }
        try {
            if (match(FINAL)) return varDeclaration(true, consumeVarType(generics).getReference());

            Optional<SourceClassReference> type = tryConsumeVarType(generics);
            return type.map(sourceClassReference -> varDeclaration(false, sourceClassReference.getReference())).orElseGet(this::statement);
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private CompileStmt.VarDecl varDecl(boolean isFinal, ClassReference type, Token name) {

        CompileExpr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        createVar(name, type, initializer != null, isFinal);

        if (initializer != null) {
            checkVarType(name, initializer);
        }

        consumeEndOfArg();
        return new CompileStmt.VarDecl(name, type, initializer, isFinal);
    }

    private CompileStmt varDeclaration(boolean isFinal, ClassReference type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        return varDecl(isFinal, type, name);
    }

    private CompileStmt statement() {
        try {
            if (match(C_BRACKET_O)) return new CompileStmt.Block(block("block"));
            if (match(RETURN)) return returnStatement();
            if (match(TRY)) return tryStatement();
            if (match(THROW)) return thrStatement();
            if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
            if (match(FOR)) return forStatement();
            if (match(IF)) return ifStatement();
            if (match(WHILE)) return whileStatement();

            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private CompileStmt statementWithScope() {
        try {
            if (match(C_BRACKET_O)) return new CompileStmt.Block(block("block"));
            try {
                this.pushScope();

                if (match(RETURN)) return returnStatement();
                if (match(TRY)) return tryStatement();
                if (match(THROW)) return thrStatement();
                if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
                if (match(FOR)) return forStatement();
                if (match(IF)) return ifStatement();
                if (match(WHILE)) return whileStatement();

                return expressionStatement();
            } finally {
                this.popScope();
            }
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private CompileStmt tryStatement() {
        consumeCurlyOpen("try statement");
        CompileStmt.Block tryBlock = new CompileStmt.Block(block("try statement"));
        Token brClose = previous();

        List<Pair<Pair<ClassReference[],Token>, CompileStmt.Block>> catches = new ArrayList<>(); //what an insane varType
        while (match(CATCH)) {
            List<ClassReference> targets = new ArrayList<>();
            consumeBracketOpen("catch");
            do {
                targets.add(consumeVarType(generics).getReference());
            } while (match(SINGLE_OR));
            pushScope();
            Token name = consumeIdentifier();
            consumeBracketClose("catch");
            createVar(name, VarTypeManager.THROWABLE, true, false);
            consumeCurlyOpen("catch statement");
            CompileStmt.Block block = new CompileStmt.Block(block("catch statement"));
            popScope();
            catches.add(Pair.of(
                    Pair.of(
                            targets.toArray(new ClassReference[0]),
                            name
                    ),
                    block
            ));
        }
        CompileStmt.Block finallyBlock = null;
        if (match(FINALLY)) {
            consumeCurlyOpen("finally statement");
            finallyBlock = new CompileStmt.Block(block("finally statement"));
        } else if (catches.isEmpty()) error(brClose, "expected 'catch' or 'finally'");
        return new CompileStmt.Try(tryBlock, catches.toArray(Pair[]::new), finallyBlock);
    }

    private CompileStmt thrStatement() {
        Token keyword = previous();
        expectType(VarTypeManager.THROWABLE);
        CompileExpr val = expression();
        expectType(val, VarTypeManager.THROWABLE);
        consumeEndOfArg();
        popExpectation();
        seenReturn();
        return new CompileStmt.Throw(keyword, val);
    }

    private CompileStmt returnStatement() {
        Token keyword = previous();
        CompileExpr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        if (funcRetType == VarTypeManager.VOID.reference() && value != null) error(keyword, "incompatible types: unexpected return value.");
        else if (value != null) expectType(value, funcRetType);
        else if (funcRetType != VarTypeManager.VOID.reference()) error(keyword, "incompatible types: missing return value.");

        consumeEndOfArg();
        seenReturn();
        return new CompileStmt.Return(keyword, value);
    }

    private CompileStmt loopInterruptionStatement() {
        Token token = previous();
        if (loopIndex <= 0) error(token, "'" + token.lexeme() + "' can only be used inside loops");
        consumeEndOfArg();
        seenReturn();
        return new CompileStmt.LoopInterruption(token);
    }

    private CompileStmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");
        pushScope();
        loopIndex++;

        Optional<SourceClassReference> type = tryConsumeVarType(generics);

        CompileStmt initializer;
        if (type.isPresent()) {
            Token name = consumeIdentifier();
            ClassReference reference = type.get().getReference();
            if (match(COLON)) {
                ClassReference arrayType = reference.array();
                expectType(arrayType);
                CompileExpr init = expression();
                popExpectation();
                expectType(init, arrayType);
                consumeBracketClose("for");
                varAnalyser.add(name.lexeme(), reference, true, true);
                CompileStmt stmt = statement();
                loopIndex--;
                popScope();
                return new CompileStmt.ForEach(reference, name, init, stmt);
            }
            initializer = varDecl(false, reference, name);
        } else if (match(EOA)) {
            initializer = null;
        } else if (match(IDENTIFIER) && parser.hasClass(previous().lexeme())) {
            initializer = varDeclaration(false, parser.getClass(previous().lexeme()));
        } else {
            initializer = expressionStatement();
        }

        CompileExpr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        expectCondition(condition);
        consumeEndOfArg();

        CompileExpr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        CompileStmt body = statement();

        popScope();
        loopIndex--;

        return new CompileStmt.For(initializer, condition, increment, body, keyword);
    }

    private CompileStmt ifStatement() {
        Token statement = previous();
        consumeBracketOpen("if");
        CompileExpr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("if condition");

        CompileStmt thenBranch = statementWithScope();
        CompileStmt elseBranch = null;
        List<Pair<CompileExpr, CompileStmt>> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            CompileExpr elifCondition = expression();
            this.expectCondition(elifCondition);
            consumeBracketClose("elif condition");
            CompileStmt elifStmt = statementWithScope();
            elifs.add(Pair.of(elifCondition, elifStmt));
        }

        if (match(ELSE)) {
            elseBranch = statementWithScope();
        }

        return new CompileStmt.If(condition, thenBranch, elseBranch, elifs.toArray(Pair[]::new), statement);
    }

    private CompileStmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        CompileExpr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("while condition");
        this.loopIndex++;
        this.pushScope();
        CompileStmt body = statement();
        this.popScope();
        this.loopIndex--;

        return new CompileStmt.While(condition, body, keyword);
    }

    private CompileStmt[] block(String name) {
        List<CompileStmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose(name);
        return statements.toArray(new CompileStmt[0]);
    }

    private CompileStmt expressionStatement() {
        CompileExpr expr = expression();
        consumeEndOfArg();
        return new CompileStmt.Expression(expr);
    }

    public CompileStmt[] parse() {
        if (tokens.length == 0) return new CompileStmt[0];
        List<CompileStmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(declaration());
        return stmts.toArray(CompileStmt[]::new);
    }

    public void applyMethod(List<? extends Pair<SourceClassReference, String>> params, ClassReference targetClass, ClassReference superclass, ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());
        if (targetClass != null) this.varAnalyser.add("this", targetClass, true, true);
        if (superclass != null) this.varAnalyser.add("super", superclass, true, true);
        for (Pair<SourceClassReference, String> param : params) {
            varAnalyser.add(param.right(), param.left().getReference(), true, true);
        }
    }

    public void popMethod() {
        this.popScope();
        this.generics.pop();
        funcRetType = VarTypeManager.VOID.reference();
    }

    public void applyStaticMethod(List<? extends Pair<? extends ClassReference, String>> params, ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());

        for (Pair<? extends ClassReference, String> param : params) {
            varAnalyser.add(param.right(), param.left(), true, true);
        }
    }
}
