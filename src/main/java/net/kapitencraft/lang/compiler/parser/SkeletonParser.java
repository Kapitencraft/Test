package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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

        Token name = consumeIdentifier();

        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }

        PreviewClass previewClass = new PreviewClass(name.lexeme());
        parser.addClass(previewClass, null);
        LoxClass superClass = VarTypeManager.OBJECT;
        if (match(EXTENDS)) superClass = consumeVarType(parser);

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<MethodDecl> constructors = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassConstructor<?>> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = new ModifiersParser();
            modifiers.parse();
            if (check(CLASS)) {
                enclosed.add(classDecl(modifiers.isAbstract, modifiers.isFinal, pckID, null));
            } else if (check(INTERFACE)) {
                if (modifiers.isAbstract) error(peek(), "interfaces can not be declared abstract");
                if (modifiers.isFinal) error(peek(), "interfaces can not be final");
                enclosed.add(interfaceDecl(pckID, null));
            } else if (check(ENUM)) {
                if (modifiers.isAbstract) error(peek(), "enums can not be abstract");
                if (modifiers.isFinal) error(peek(), "enums can not be final");
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
                        MethodDecl decl = funcDecl(parser, type, elementName, modifiers.isFinal, modifiers.isStatic, modifiers.isAbstract);
                        methods.add(decl);
                    } else {
                        if (modifiers.isAbstract) error(elementName, "fields may not be abstract");
                        FieldDecl decl = fieldDecl(type, elementName, modifiers.isFinal, modifiers.isStatic);
                        fields.add(decl);
                    }
                }
            }
        }
        consumeCurlyClose("class");
        return new ClassDecl(classAbstract, classFinal, previewClass, name, pckID, superClass, constructors.toArray(new MethodDecl[0]), methods.toArray(new MethodDecl[0]), fields.toArray(new FieldDecl[0]), enclosed.toArray(new ClassDecl[0]));
    }

    private InterfaceDecl interfaceDecl(String pckID, String fileId) {
        consume(INTERFACE, "'interface' expected");

        Token name = consumeIdentifier();

        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }

        PreviewClass preview = new PreviewClass(name.lexeme());
        parser.addClass(preview, null);

        List<LoxClass> parentInterfaces = new ArrayList<>();

        if (match(EXTENDS)) {
            do {
                parentInterfaces.add(consumeVarType(parser));
            } while (match(COMMA));
        }

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassConstructor<?>> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = new ModifiersParser();
            modifiers.parse();
            if (!readClass(enclosed::add, pckID, modifiers)) {
                LoxClass type = consumeVarType(parser);
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    MethodDecl decl = funcDecl(parser, type, elementName, modifiers.isFinal, modifiers.isStatic, modifiers.isAbstract);
                    methods.add(decl);
                } else {
                    FieldDecl decl = fieldDecl(type, elementName, modifiers.isFinal, modifiers.isStatic);
                    fields.add(decl);
                }
            }
        }
        consumeCurlyClose("class");
        return new InterfaceDecl(preview, name, pckID, parentInterfaces.toArray(new LoxClass[0]), methods.toArray(new MethodDecl[0]), fields.toArray(new FieldDecl[0]), enclosed.toArray(new ClassDecl[0]));
    }

    private EnumDecl enumDecl(String pckID, String fileId) {
        return null;
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
                else if (isAbstract) error(previous(), "illegal combination of modifiers final and abstract");
                isFinal = true;
            } else if (match(ABSTRACT)) {
                if (isAbstract) error(previous(), "duplicate abstract keyword");
                else if (isFinal) error(previous(), "illegal combination of modifiers abstract and final");
                isAbstract = true;
            } else {
                error(advance(), "modifier or <identifier> expected");
            }
        }
        String pckId = pck.stream().map(Token::lexeme).collect(Collectors.joining("."));
        return classDecl(isAbstract, isFinal, pckId, fileName);
    }

    private boolean readClass(Consumer<ClassConstructor<?>> sink, String pckID, ModifiersParser modifiers) {
        if (check(CLASS)) {
            sink.accept(classDecl(false, modifiers.isFinal, pckID, null));
        } else if (check(INTERFACE)) {
            sink.accept(interfaceDecl(pckID, null));
        } else if (check(ENUM)) {
            sink.accept(enumDecl(pckID, null));
        } else
            return false;
        return true;
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
        String nameOverride = null;
        if (match(AS)) nameOverride = consumeIdentifier().lexeme();
        consumeEndOfArg();
        LoxClass target = VarTypeManager.getClass(packages, this::error);
        if (target != null) {
            parser.addClass(target, nameOverride);
        }
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

    public record ClassDecl(boolean isAbstract, boolean isFinal, PreviewClass target, Token name, String pck, LoxClass superclass, MethodDecl[] constructors, MethodDecl[] methods, FieldDecl[] fields, ClassConstructor<?>[] enclosed) implements ClassConstructor<Compiler.BakedClass> {

        @Override
        public Compiler.BakedClass construct(Compiler.ErrorLogger logger, VarTypeParser parser, String pck) {
            return null;
        }
    }

    public record InterfaceDecl(PreviewClass target, Token name, String pck, LoxClass[] parentInterfaces, MethodDecl[] methods, FieldDecl[] fields, ClassConstructor<?>[] enclosed) implements ClassConstructor<Compiler.BakedInterface> {

        @Override
        public Compiler.BakedInterface construct(Compiler.ErrorLogger logger, VarTypeParser parser, String pck) {
            return null;
        }
    }

    public record EnumDecl(PreviewClass target, Token name, String pck, LoxClass[] interfaces, MethodDecl[] methods, FieldDecl[] fields, ClassConstructor<?>[] enclosed) implements ClassConstructor<Compiler.BakedEnum> {

        @Override
        public Compiler.BakedEnum construct(Compiler.ErrorLogger logger, VarTypeParser parser, String pck) {
            return null;
        }
    }

    public interface ClassConstructor<I extends Compiler.ClassBuilder> {
        I construct(Compiler.ErrorLogger logger, VarTypeParser parser, String pck);
    }

    public class ModifiersParser {
        private boolean isFinal;
        private boolean isStatic;
        private boolean isAbstract;

        public void parse() {
            while (!check(IDENTIFIER) && !check(CLASS) && !check(EOF)) {
                if (match(STATIC)) {
                    if (isStatic) error(previous(), "duplicate static keyword");
                    if (isFinal) error(previous(), "illegal combination of modifiers 'static' and 'final'");
                    if (isAbstract) error(previous(), "illegal combination of modifiers 'static' and 'abstract'");
                    isStatic = true;
                } else if (match(FINAL)) {
                    if (isFinal) error(previous(), "duplicate final keyword");
                    if (isStatic) error(previous(), "illegal combination of modifiers 'final' and 'static'");
                    if (isAbstract) error(previous(), "illegal combination of modifiers 'final' and 'abstract'");
                    isFinal = true;
                } else if (match(ABSTRACT)) {
                    if (isAbstract) error(previous(), "duplicate final keyword");
                    if (isStatic) error(previous(), "illegal combination of modifiers 'abstract' and 'static'");
                    if (isFinal) error(previous(), "illegal combination of modifiers 'abstract' and 'final'");
                    isAbstract = true;
                } else {
                    error(peek(), "<identifier> expected");
                }
            }
        }
    }
}