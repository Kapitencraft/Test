package net.kapitencraft.lang.compiler.parser;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.GenericSourceClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
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
    protected final Deque<List<ClassReference>> args = new ArrayDeque<>(); //TODO either use or remove
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

    protected void expectType(ClassReference... types) {
        args.push(List.of(types));
    }

    protected void popExpectation() {
        args.pop();
    }

    protected boolean typeAllowed(ClassReference target) {
        return searched().contains(target);
    }

    protected List<ClassReference> searched() {
        return args.getLast();
    }

    protected void expectType(List<ClassReference> types) {
        args.push(ImmutableList.copyOf(types));
    }

    protected ClassReference expectType(Token errorLoc, Expr value, ClassReference type) {
        ClassReference got = finder.findRetType(value);
        return expectType(errorLoc, got, type);
    }

    protected ClassReference expectType(Expr value, ClassReference type) {
        return expectType(this.locFinder.find(value), value, type);
    }

    protected void expectCondition(Token errorLoc, Expr gotten) {
        expectType(errorLoc, gotten, VarTypeManager.BOOLEAN.reference());
    }

    protected void expectCondition(Expr gotten) {
        expectCondition(locFinder.find(gotten), gotten);
    }

    protected ClassReference expectType(Token errorLoc, ClassReference gotten, ClassReference expected) {
        if (gotten == null) return VarTypeManager.VOID.reference();
        if (expected == VarTypeManager.OBJECT) return gotten;
        if ( !expected.get().isParentOf(gotten.get())) errorLogger.errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        return gotten;
    }

    protected void createVar(Token name, ClassReference type, boolean hasValue, boolean isFinal) {
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
        return current >= tokens.length - 1;
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

    protected Optional<SourceClassReference> tryConsumeVarType(Holder.Generics generics) {
        if (generics.hasGeneric(peek().lexeme())) {
            Token source = advance();
            return Optional.of(SourceClassReference.from(source, generics.getReference(source.lexeme())));
        }
        if (VarTypeManager.hasPackage(peek().lexeme()) && !varAnalyser.has(peek().lexeme())) {
            advance();
            if (check(DOT)) return Optional.ofNullable(consumeVarType(generics));
            current--;
        }
        Token t = advance();
        ClassReference reference = parser.getClass(t.lexeme());
        if (reference != null && !check(DOT)) {
            return Optional.of(SourceClassReference.from(t,  reference));
        } else
            current--;
        return Optional.empty();
    }

    protected SourceClassReference consumeVarType(Holder.Generics generics) {
        StringBuilder typeName = new StringBuilder();
        Token token = consumeIdentifier();
        typeName.append(token.lexeme());
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            if (generics.hasGeneric(token.lexeme())) {
                return new GenericSourceClassReference(token, generics.getReference(token.lexeme()));
            }
        }
        if (reference == null) {
            Package p = VarTypeManager.getPackage(token.lexeme());
            while (match(DOT)) {
                String id = consumeIdentifier().lexeme();
                typeName.append(".").append(id);
                if (p.hasClass(id)) {
                    reference = p.getClass(id);
                    break;
                }
                p = p.getPackage(id);
            }
        }

        Token last = previous();
        while (match(DOT) && reference != null) {
            String enclosingName = consumeIdentifier().lexeme();
            typeName.append(".").append(enclosingName);
            if (!reference.get().hasEnclosing(enclosingName)) {
                return SourceClassReference.from(last, reference);
            }
            last = previous();
            reference = reference.get().getEnclosing(enclosingName);
        }
        if (reference == null) {
            error(token, "unknown class '" + typeName + "'");
            return null; //skip rest
        }
        else while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            reference = reference.array();
            last = previous();
        }
        return SourceClassReference.from(last, reference);
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
