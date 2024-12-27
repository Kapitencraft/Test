package net.kapitencraft.lang.compiler.parser;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.analyser.VarAnalyser;
import net.kapitencraft.lang.compiler.visitor.LocationFinder;
import net.kapitencraft.lang.compiler.visitor.RetTypeFinder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.*;

import static net.kapitencraft.lang.holder.token.TokenType.*;

@SuppressWarnings({"ThrowableNotThrown", "UnusedReturnValue"})
public class AbstractParser {
    private static final Map<TokenTypeCategory, TokenType[]> categoryLookup = createCategoryLookup();

    private static Map<TokenTypeCategory, TokenType[]> createCategoryLookup() {
        Map<TokenTypeCategory, TokenType[]> lookup = new HashMap<>();
        for (TokenTypeCategory category : TokenTypeCategory.values()) {
            lookup.put(category, Arrays.stream(values()).filter(tokenType -> tokenType.isCategory(category)).toArray(TokenType[]::new));
        }
        return lookup;
    }

    protected int current;
    protected Token[] tokens;
    protected VarTypeParser parser;
    protected RetTypeFinder finder;
    protected final LocationFinder locFinder = new LocationFinder();
    protected final Deque<List<LoxClass>> args = new ArrayDeque<>(); //TODO either use or remove
    protected final Compiler.ErrorLogger errorLogger;
    protected VarAnalyser varAnalyser;

    protected void checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        if (!varAnalyser.has(varName)) {
            error(name, "cannot find symbol");
        } else if (requireValue && !varAnalyser.hasValue(varName)) {
            error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
        } else if (!mayBeFinal && varAnalyser.isFinal(varName)) {
            error(name, "Can not assign to final variable");
        }
    }

    protected void checkVarType(Token name, Expr value) {
        if (!varAnalyser.has(name.lexeme())) return;
        expectType(name, value, varAnalyser.getType(name.lexeme()));
    }

    protected void expectType(LoxClass... types) {
        args.push(List.of(types));
    }

    protected void popExpectation() {
        args.pop();
    }

    protected boolean typeAllowed(LoxClass target) {
        return searched().contains(target);
    }

    protected List<LoxClass> searched() {
        return args.getLast();
    }

    protected void expectType(List<LoxClass> types) {
        args.push(ImmutableList.copyOf(types));
    }

    protected LoxClass expectType(Token errorLoc, Expr value, LoxClass type) {
        LoxClass got = finder.findRetType(value);
        return expectType(errorLoc, got, type);
    }

    protected LoxClass expectType(Expr value, LoxClass type) {
        return expectType(this.locFinder.find(value), value, type);
    }

    protected void expectCondition(Token errorLoc, Expr gotten) {
        expectType(errorLoc, gotten, VarTypeManager.BOOLEAN);
    }

    protected void expectCondition(Expr gotten) {
        expectCondition(locFinder.find(gotten), gotten);
    }

    protected LoxClass expectType(Token errorLoc, LoxClass gotten, LoxClass expected) {
        if (expected == VarTypeManager.OBJECT.get()) return gotten;
        if (!expected.isParentOf(gotten)) errorLogger.errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        return gotten;
    }

    protected void createVar(Token name, LoxClass type, boolean hasValue, boolean isFinal) {
        if (varAnalyser.has(name.lexeme())) {
            errorLogger.errorF(name, "Variable '%s' already defined in current scope", name.lexeme());
        }
        varAnalyser.add(name.lexeme(), type, hasValue, isFinal);
    }

    public AbstractParser(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
        this.varAnalyser = new VarAnalyser();
        this.finder = new RetTypeFinder(varAnalyser);
    }

    protected boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    protected boolean check(TokenType... types) {
        if (isAtEnd()) return false;
        return Arrays.stream(types).anyMatch(this::check);
    }

    protected boolean match(TokenTypeCategory category) {
        return match(categoryLookup.get(category));
    }

    /**
     * same as {@link AbstractParser#check(TokenType) check} but consumes token
     */
    protected boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    protected boolean isAtEnd() {
        return current == tokens.length - 1;
    }

    protected Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    protected Token peek() {
        return tokens[current];
    }

    protected Token previous() {
        return tokens[current - 1];
    }


    protected Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    protected Optional<LoxClass> tryConsumeVarType() {
        if (VarTypeManager.hasPackage(peek().lexeme())) {
            if (!varAnalyser.has(peek().lexeme())) {
                return Optional.of(consumeVarType());
            }
        }
        LoxClass loxClass = parser.getClass(advance().lexeme());
        if (loxClass != null && !check(DOT)) {
            return Optional.of(loxClass);
        } else
            current--;
        return Optional.empty();
    }

    protected LoxClass consumeVarType() {
        StringBuilder typeName = new StringBuilder();
        Token token = consumeIdentifier();
        typeName.append(token.lexeme());
        LoxClass loxClass = parser.getClass(token.lexeme());
        if (loxClass == null) {
            Package p = VarTypeManager.getPackage(token.lexeme());
            while (match(DOT)) {
                String id = consumeIdentifier().lexeme();
                typeName.append(".").append(id);
                if (p.hasClass(id)) {
                    loxClass = p.getClass(id);
                    break;
                }
                p = p.getPackage(id);
            }
        }
        while (match(DOT) && loxClass != null) {
            String enclosingName = consumeIdentifier().lexeme();
            typeName.append(".").append(enclosingName);
            if (!loxClass.hasEnclosing(enclosingName)) {
                return loxClass;
            }
            loxClass = loxClass.getEnclosing(enclosingName);
        }
        if (loxClass == null) error(token, "unknown class '" + typeName + "'");
        else while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            loxClass = loxClass.array();
        }
        return loxClass;
    }

    protected Token consumeIdentifier() {
        return consume(IDENTIFIER, "<identifier> expected");
    }

    protected Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    protected Token consumeCurlyOpen(String method) {
        return this.consume(C_BRACKET_O, "Expected '{' after '" + method + "'.");
    }

    protected Token consumeCurlyClose(String method) {
        return this.consume(C_BRACKET_C, "Expected '}' after " + method + ".");
    }

    protected Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    protected Token consumeEndOfArg() {
        return this.consume(EOA, "';' expected");
    }

    protected static class ParseError extends RuntimeException {}

    protected ParseError error(Token token, String message) {
        errorLogger.error(token, message);
        return new ParseError();
    }

    protected void warn(Token token, String message) {
        errorLogger.warn(token, message);
    }


    protected void synchronize() {

        while (!isAtEnd()) {
            switch (peek().type()) {
                case EOF:
                case CLASS:
                case IMPORT:
                case FUNC:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
