package net.kapitencraft.lang.compiler.parser;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.analyser.VarAnalyser;
import net.kapitencraft.lang.compiler.visitor.LocationFinder;
import net.kapitencraft.lang.compiler.visitor.RetTypeFinder;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.lang.holder.token.TokenType.*;

@SuppressWarnings({"ThrowableNotThrown", "UnusedReturnValue"})
public class AbstractParser {
    protected static final ClassReference WILDCARD = new GenericClassReference("", null, null);

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
    protected BytecodeVars varAnalyser;

    protected void checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        BytecodeVars.FetchResult result = varAnalyser.get(varName);
        if (result == BytecodeVars.FetchResult.FAIL) {
            error(name, "cannot find symbol");
        } else if (requireValue && !result.assigned()) {
            error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
        } else if (!mayBeFinal && !result.canAssign()) {
            error(name, "Can not assign to final variable");
        }
    }

    protected void checkVarType(Token name, CompileExpr value) {
        BytecodeVars.FetchResult result = varAnalyser.get(name.lexeme());
        if (result == BytecodeVars.FetchResult.FAIL) return;
        expectType(name, value, result.type());
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

    protected ClassReference expectType(Token errorLoc, CompileExpr value, ClassReference type) {
        ClassReference got = finder.findRetType(value);
        return expectType(errorLoc, got, type);
    }

    protected ClassReference expectType(CompileExpr value, ClassReference type) {
        return expectType(this.locFinder.find(value), value, type);
    }

    protected void expectCondition(Token errorLoc, CompileExpr gotten) {
        expectType(errorLoc, gotten, VarTypeManager.BOOLEAN.reference());
    }

    protected void expectCondition(CompileExpr gotten) {
        expectCondition(locFinder.find(gotten), gotten);
    }

    protected ClassReference expectType(Token errorLoc, ClassReference gotten, ClassReference expected) {
        if (gotten == null) return WILDCARD;
        if (gotten == WILDCARD) return expected;
        if (expected == VarTypeManager.OBJECT) return gotten;
        if (!gotten.get().isChildOf(expected.get())) errorLogger.errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        return gotten;
    }

    protected byte createVar(Token name, ClassReference type, boolean hasValue, boolean isFinal) {
        BytecodeVars.FetchResult result = varAnalyser.get(name.lexeme());
        if (result != BytecodeVars.FetchResult.FAIL) {
            errorLogger.errorF(name, "Variable '%s' already defined in current scope", name.lexeme());
        }
        return varAnalyser.add(name.lexeme(), type, hasValue, isFinal);
    }

    public AbstractParser(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
        this.varAnalyser = new BytecodeVars();
        this.finder = new RetTypeFinder(varAnalyser);
    }

    protected @Nullable Holder.Generics generics(GenericStack genericStack) {
        if (match(LESSER)) {
            List<Holder.Generic> generics = new ArrayList<>();
            do {
                generics.add(generic(genericStack));
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.Generics(generics.toArray(new Holder.Generic[0]));
        }
        return null;
    }

    private Holder.Generic generic(GenericStack genericStack) {
        Token name = consumeIdentifier();
        SourceClassReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarType(genericStack);
        } else if (match(SUPER)) {
            upperBound = consumeVarType(genericStack);
        }

        return new Holder.Generic(name, lowerBound, upperBound);
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

    protected Optional<SourceClassReference> tryConsumeVarType(GenericStack generics) {
        Optional<ClassReference> optional = generics.getValue(peek().lexeme());
        if (optional.isPresent()) return Optional.of(SourceClassReference.from(advance(), optional.get()));
        if (VarTypeManager.hasPackage(peek().lexeme()) && varAnalyser.get(peek().lexeme()) == BytecodeVars.FetchResult.FAIL) {
            advance();
            if (check(DOT)) {
                current--;
                return Optional.ofNullable(consumeVarType(generics));
            }
        }
        Token t = advance();
        ClassReference reference = parser.getClass(t.lexeme());
        if (reference != null && !check(DOT)) {
            Holder.Generics declared = generics(generics);
            if (declared != null) return Optional.of(new AppliedGenericsReference(reference, declared, t));
            return Optional.of(SourceClassReference.from(t, reference));
        } else
            current--;
        return Optional.empty();
    }

    protected SourceClassReference consumeVarType(GenericStack generics) {
        Token token = consumeIdentifier();
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            Optional<ClassReference> optional = generics.getValue(token.lexeme());
            if (optional.isPresent()) return SourceClassReference.from(token, optional.get());
        }
        if (reference == null) {
            Package p = VarTypeManager.getPackage(token.lexeme());
            while (match(DOT) && p != null) {
                String id = consumeIdentifier().lexeme();
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
            if (!reference.get().hasEnclosing(enclosingName)) {
                return SourceClassReference.from(last, reference);
            }
            last = previous();
            reference = reference.get().getEnclosing(enclosingName);
        }
        if (reference == null) {
            error(token, "unknown symbol");
            return null; //skip rest
        }
        Holder.Generics declared = generics(generics);
        while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            reference = reference.array();
            last = previous();
        }
        if (declared != null) return new AppliedGenericsReference(reference, declared, last);
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
