package net.kapitencraft.lang.compile.parser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.compile.VarTypeParser;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class SkeletonParser extends AbstractParser {
    private final String fileName;

    private LoxClass consumeVarType(VarTypeParser parser) {
        Token token = consumeIdentifier();
        LoxClass loxClass = parser.getClass(token.lexeme());
        if (loxClass == null) error(token, "unknown symbol");
        return loxClass;
    }

    public SkeletonParser(Compiler.ErrorLogger errorLogger, String fileName) {
        super(errorLogger);
        this.fileName = fileName;
    }

    public void parseImports() {
        while (check(IMPORT)) {
            importStmt();
        }
    }

    public ClassDecl classDecl(boolean classAbstract, boolean classFinal, String pckID, @Nullable String fileId) {

        consume(CLASS, "'class' expected");

        Token name = consume(IDENTIFIER, "class name expected");

        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }

        PreviewClass previewClass = new PreviewClass(name.lexeme());
        parser.addClass(previewClass);
        LoxClass superClass = VarTypeManager.OBJECT;
        if (match(EXTENDS)) superClass = consumeVarType(parser);

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<MethodDecl> constructors = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassDecl> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            boolean isStatic = false;
            boolean isFinal = false;
            boolean isAbstract = false;
            while (!check(IDENTIFIER) && !check(CLASS) && !check(EOF)) {
                if (match(STATIC)) {
                    if (isStatic) error(previous(), "duplicate static keyword");
                    isStatic = true;
                } else if (match(FINAL)) {
                    if (isFinal) error(previous(), "duplicate final keyword");
                    isFinal = true;
                } else if (match(ABSTRACT)) {
                    if (isAbstract) error(previous(), "duplicate abstract keyword");
                    if (!classAbstract) error(previous(), "abstract method on non-abstract class");
                    isAbstract = true;
                } else {
                    error(peek(), "<identifier> expected");
                }
            }
            if (check(CLASS)) {
                enclosed.add(classDecl(isAbstract, isFinal, pckID, null));
            } else {
                if (Objects.equals(peek().lexeme(), name.lexeme())) {
                    Token constName = consume(IDENTIFIER, "that shouldn't have happened...");
                    consumeBracketOpen("constructor");
                    MethodDecl decl = funcDecl(parser, VarTypeManager.VOID, constName, false, false, false);
                    constructors.add(decl);
                } else {
                    LoxClass type = consumeVarType(parser);
                    Token elementName = consumeIdentifier();
                    if (match(BRACKET_O)) {
                        if (isAbstract && isStatic) error(elementName, "illegal combination of modifiers abstract and static");
                        MethodDecl decl = funcDecl(parser, type, elementName, isFinal, isStatic, isAbstract);
                        methods.add(decl);
                    } else {
                        if (isAbstract) error(elementName, "fields may not be abstract");
                        FieldDecl decl = fieldDecl(type, elementName, isFinal, isStatic);
                        fields.add(decl);
                    }
                }
            }
        }
        consumeCurlyClose("class");
        return new ClassDecl(classAbstract, classFinal, previewClass, name, pckID, superClass, constructors.toArray(new MethodDecl[0]), methods.toArray(new MethodDecl[0]), fields.toArray(new FieldDecl[0]), enclosed.toArray(new ClassDecl[0]));
    }

    public ClassDecl parse() {
        List<Token> pck = new ArrayList<>();
        try {
            consume(PACKAGE, "package expected!");
            pck.add(consumeIdentifier());
            while (!check(EOA)) {
                consume(DOT, "unexpected token");
                pck.add(consumeIdentifier());
            }
            consumeEndOfArg();
        } catch (ParseError error) {
            synchronize();
        }

        parseImports();
        boolean isFinal = false;
        boolean isAbstract = false;
        while (!check(CLASS) && !check(EOF)) {
            if (match(FINAL)) {
                if (isFinal) error(previous(), "duplicate final keyword");
                isFinal = true;
            } else if (match(ABSTRACT)) {
                if (isAbstract) error(previous(), "duplicate abstract keyword");
                isAbstract = true;
            } else {
                error(advance(), "<identifier> expected");
            }
        }
        String pckId = pck.stream().map(Token::lexeme).collect(Collectors.joining("."));
        return classDecl(isAbstract, isFinal, pckId, fileName);
    }

    private MethodDecl funcDecl(VarTypeParser parser, LoxClass type, Token name, boolean isFinal, boolean isStatic, boolean isAbstract) {

        List<Pair<LoxClass, Token>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                LoxClass pType = consumeVarType(parser);
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName));
            } while (match(COMMA));
        }
        consumeBracketClose("parameters");

        Token[] code = null;

        if (!isAbstract) { //body only if method isn't abstract
            consumeCurlyOpen("method body");

            code = getMethodCode();

            consumeCurlyClose("method body");
        } else consumeEndOfArg();
        return new MethodDecl(code, name, parameters, type, isFinal, isStatic, isAbstract);
    }

    private FieldDecl fieldDecl(LoxClass type, Token name, boolean isFinal, boolean isStatic) {
        Token[] code = null;

        if (match(ASSIGN)) code = getFieldCode();
        else consumeEndOfArg();

        return new FieldDecl(code, name, type, isFinal, isStatic);
    }

    private void importStmt() {
        consume(IMPORT, "Expected import or class");
        List<Token> packages = readPackage();
        consumeEndOfArg();
        LoxClass target = VarTypeManager.getClass(packages, this::error);
        if (target != null) parser.addClass(target);
    }

    private List<Token> readPackage() {
        List<Token> packages = new ArrayList<>();
        packages.add(consumeIdentifier());
        while (!check(EOA)) {
            consume(DOT, "unexpected name");
            packages.add(consumeIdentifier());
        }
        return packages;
    }

    private Token[] getMethodCode() {
        List<Token> tokens = new ArrayList<>();
        int i = 1;
        tokens.add(peek());
        do {
            advance();
            tokens.add(peek());
            if (peek().type() == C_BRACKET_O) i++;
            else if (peek().type() == C_BRACKET_C) i--;
        } while (i > 0);
        return tokens.toArray(Token[]::new);
    }

    private Token[] getFieldCode() {
        List<Token> tokens = new ArrayList<>();

        do {
            tokens.add(peek());
            advance();
        } while (!match(EOA));

        return tokens.toArray(Token[]::new);
    }


    public record MethodDecl(Token[] body, Token name, List<Pair<LoxClass, Token>> params, LoxClass type, boolean isFinal, boolean isStatic, boolean isAbstract) {

    }

    public record FieldDecl(Token[] body, Token name, LoxClass type, boolean isFinal, boolean isStatic) {

    }

    public record ClassDecl(boolean isAbstract, boolean isFinal, PreviewClass target, Token name, String pck, LoxClass superclass, MethodDecl[] constructors, MethodDecl[] methods, FieldDecl[] fields, ClassDecl[] enclosed) {

    }
}
