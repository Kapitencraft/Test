package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.analyser.LocalVariableContainer;
import net.kapitencraft.lang.holder.ast.ElifBranch;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenType.C_BRACKET_C;

@SuppressWarnings("ThrowableNotThrown")
public class StmtParser extends ExprParser {

    public StmtParser(Compiler.ErrorStorage errorStorage) {
        super(errorStorage);
    }

    private ClassReference funcRetType = VarTypeManager.VOID.reference();
    private final Stack<Boolean> seenReturn = new Stack<>();
    private int loopIndex = 0;

    @Override
    public void apply(Token[] toParse, VarTypeContainer targetAnalyser) {
        super.apply(toParse, targetAnalyser);
        seenReturn.clear(); //reset entire return stack
        seenReturn.add(false);
    }

    private void seenReturn() {
        seenReturn.set(seenReturn.size()-1, true);
    }

    private void pushScope() {
        seenReturn.add(false);
    }

    private void popScope() {
        seenReturn.pop();
    }

    private Stmt popScopeStmt() {
        popScope();
        return new Stmt.ClearLocals();
    }

    private Stmt declaration() {
        if (seenReturn.peek()) {
            error(peek(), "unreachable statement");
        }
        try {
            if (match(FINAL)) return varDeclaration(true, consumeVarType(generics).getReference());

            Optional<SourceReference> type = tryConsumeVarType(generics);
            return type.map(sourceClassReference -> varDeclaration(false, sourceClassReference.getReference())).orElseGet(this::statement);
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

        //byte index = createVar(name, type, initializer != null, isFinal);
//
        //if (initializer != null) {
        //    checkVarType(name, initializer);
        //}

        consumeEndOfArg();
        Stmt.VarDecl varDecl = new Stmt.VarDecl();
        varDecl.name = name;
        varDecl.type = type;
        varDecl.initializer = initializer;
        varDecl.isFinal = isFinal;
        return varDecl;
    }

    private Stmt varDeclaration(boolean isFinal, ClassReference type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        return varDecl(isFinal, type, name);
    }

    private Stmt statement() {
        try {
            if (match(C_BRACKET_O)) {
                Stmt.Block block = new Stmt.Block();
                block.statements = block("block");
                return block;
            }
            if (match(RETURN)) return returnStatement();
            if (match(TRY)) return tryStatement();
            if (match(THROW)) return thrStatement();
            if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
            if (match(FOR)) return forStatement();
            if (match(IF)) return ifStatement();
            if (match(WHILE)) return whileStatement();
            if (match(TRACE)) return debugTrace();

            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt debugTrace() {
        Token keyword = previous();
        List<Token> locals = new ArrayList<>();
        if (match(S_BRACKET_O)) {
            do {
                locals.add(consumeIdentifier());
            } while (match(COMMA));
            consume(S_BRACKET_C, "expected ']' after trace debug");
        }
        consumeEndOfArg();
        Stmt.DebugTrace debugTrace = new Stmt.DebugTrace();
        debugTrace.keyword = keyword;
        debugTrace.localNames = locals.toArray(Token[]::new);
        return debugTrace;
    }

    private Stmt tryStatement() {
        consumeCurlyOpen("try statement");
        Stmt.Block tryBlock = new Stmt.Block();
        tryBlock.statements = block("try statement");
        Token brClose = previous();

        List<Pair<Pair<ClassReference[],Token>, Stmt.Block>> catches = new ArrayList<>(); //what an insane varType
        while (match(CATCH)) {
            List<ClassReference> targets = new ArrayList<>();
            consumeBracketOpen("catch");
            do {
                targets.add(consumeVarType(generics).getReference());
            } while (match(SINGLE_OR));
            pushScope();
            Token name = consumeIdentifier();
            consumeBracketClose("catch");
            consumeCurlyOpen("catch statement");
            Stmt.Block body = new Stmt.Block();
            body.statements =  block("catch statement");
            body.statements.add(popScopeStmt());
            catches.add(Pair.of(
                    Pair.of(
                            targets.toArray(new ClassReference[0]),
                            name
                    ),
                    body
            ));
        }
        Stmt.Block finallyBlock = null;
        if (match(FINALLY)) {
            consumeCurlyOpen("finally statement");
            finallyBlock = new Stmt.Block();
            finallyBlock.statements = block("finally statement");
        } else if (catches.isEmpty()) error(brClose, "expected 'catch' or 'finally'");

        Stmt.Try aTry = new Stmt.Try();
        aTry.body = tryBlock;
        aTry.catches = catches.toArray(Pair[]::new);
        aTry.finale = finallyBlock;
        return aTry;
    }

    private Stmt thrStatement() {
        Token keyword = previous();
        Expr val = expression();
        consumeEndOfArg();
        seenReturn();
        Stmt.Throw aThrow = new Stmt.Throw();
        aThrow.keyword = keyword;
        aThrow.value = val;
        return aThrow;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        consumeEndOfArg();
        seenReturn();
        Stmt.Return aReturn = new Stmt.Return();
        aReturn.keyword = keyword;
        aReturn.value = value;
        return aReturn;
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        if (loopIndex <= 0) error(token, "'" + token.lexeme() + "' can only be used inside loops");
        consumeEndOfArg();
        seenReturn();
        Stmt.LoopInterruption loopInterruption = new Stmt.LoopInterruption();
        loopInterruption.type = token;
        return loopInterruption;
    }

    private Stmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");

        Optional<SourceReference> type = tryConsumeVarType(generics);

        Stmt initializer;
        if (type.isPresent()) {
            Token name = consumeIdentifier();
            ClassReference reference = type.get().getReference();
            if (match(COLON)) {
                Expr init = expression();
                consumeBracketClose("for");
                pushScope();
                loopIndex++;

                Stmt stmt = statement();
                stmt = mergeBody(stmt, popScopeStmt());
                Stmt.ForEach forEach = new Stmt.ForEach();
                forEach.type = reference;
                forEach.name = name;
                forEach.initializer = init;
                forEach.body = stmt;
                return forEach;
            }
            pushScope();
            loopIndex++;
            initializer = varDecl(false, reference, name);
        } else if (match(EOA)) {
            pushScope();
            loopIndex++;
            initializer = null;
        } else if (match(IDENTIFIER) && parser.hasClass(previous().lexeme())) {
            pushScope();
            loopIndex++;
            initializer = varDeclaration(false, parser.getClass(previous().lexeme()));
        } else {
            pushScope();
            loopIndex++;
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        consumeEndOfArg();

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        pushScope();

        Stmt body = statement();

        body = mergeBody(body, popScopeStmt());

        Stmt.For aFor = new Stmt.For();
        aFor.init = initializer;
        aFor.condition = condition;
        aFor.increment = increment;
        aFor.body = body;
        aFor.keyword = keyword;
        return aFor;
    }

    private Stmt ifStatement() {
        Token keyword = previous();
        consumeBracketOpen("if");
        Expr condition = expression();
        consumeBracketClose("if condition");

        Stmt thenBranch = statement();
        thenBranch = mergeBody(thenBranch, popScopeStmt());
        Stmt elseBranch = null;
        List<ElifBranch> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            consumeBracketClose("elif condition");
            Stmt elifStmt = statement();
            elifStmt = mergeBody(elifStmt, popScopeStmt());
            elifs.add(new ElifBranch(elifCondition, elifStmt, false));
        }

        boolean elseBranchSeenReturn = false;
        if (match(ELSE)) {
            elseBranch = statement();
            elseBranch = mergeBody(elseBranch, popScopeStmt());
        }

        Stmt.If anIf = new Stmt.If();
        anIf.condition = condition;
        anIf.thenBranch = thenBranch;
        anIf.elseBranch = elseBranch;
        anIf.elifs = elifs.toArray(ElifBranch[]::new);
        anIf.keyword = keyword;
        return anIf;
    }

    private Stmt mergeBody(Stmt first, Stmt second) {
        if (first instanceof Stmt.Block block) {
            block.statements.add(second);
            return block;
        } else  if (second instanceof Stmt.Block block) {
            block.statements.addFirst(first);
            return block;
        }
        Stmt.Block stmt = new Stmt.Block();
        stmt.statements = new ArrayList<>();
        stmt.statements.add(first);
        stmt.statements.add(second);
        return stmt;
    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        consumeBracketClose("while condition");
        this.loopIndex++;
        this.pushScope();
        Stmt body = statement();
        body = mergeBody(body, popScopeStmt());

        Stmt.While aWhile = new Stmt.While();
        aWhile.condition = condition;
        aWhile.body = body;
        aWhile.keyword = keyword;
        return aWhile;
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
        Stmt.Expression stmt = new Stmt.Expression();
        stmt.expression = expr;
        return stmt;
    }

    public Stmt[] parse() {
        if (tokens.length == 0) return new Stmt[] {new Stmt.Return()};
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(declaration());
        if (!seenReturn.peek()) stmts.add(new Stmt.Return());
        return stmts.toArray(Stmt[]::new);
    }

    public void applyMethod(ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());
    }

    public void popMethod(Token methodEnd) {
        if (!funcRetType.is(VarTypeManager.VOID) && !seenReturn.peek())
            error(methodEnd, "missing return statement");
        this.popScope(); //ignore return value; methods that end get their locals removed either way
        this.generics.pop();
        funcRetType = VarTypeManager.VOID.reference();
    }

    public void applyStaticMethod(List<? extends Pair<SourceReference, String>> params, ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());

    }
}
