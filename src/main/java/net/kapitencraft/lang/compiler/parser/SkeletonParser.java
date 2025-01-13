package net.kapitencraft.lang.compiler.parser;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.decl.*;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class SkeletonParser extends AbstractParser {
    private final ModifiersParser MODIFIERS = new ModifiersParserBuilder()
            .acceptsAll(FINAL, ABSTRACT, STATIC, DEFAULT)
            .illegalCombination(FINAL, DEFAULT)
            .illegalCombination(ABSTRACT, DEFAULT)
            .illegalCombination(STATIC, DEFAULT)
            .illegalCombination(FINAL, ABSTRACT)
            .illegalCombination(ABSTRACT, STATIC)
            .illegalCombination(STATIC, FINAL)
            .build();
    private final String fileName;

    public SkeletonParser(Compiler.ErrorLogger errorLogger, String fileName) {
        super(errorLogger);
        this.fileName = fileName;
    }

    public void parseImports() {
        while (check(IMPORT)) {
            importStmt();
        }
    }

    private ClassDecl classDecl(boolean classAbstract, boolean classFinal, String pckID, @Nullable String fileId, ClassReference target, AnnotationObj[] classAnnotations) {

        consume(CLASS, "'class' expected");

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        if (target == null) target = new ClassReference(name.lexeme());

        parser.addClass(target, null);
        ClassReference superClass = VarTypeManager.OBJECT;
        if (match(EXTENDS)) superClass = consumeVarType();

        List<ClassReference> implemented = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                implemented.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<MethodDecl> constructors = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassConstructor<?>> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            AnnotationObj[] annotations = parseAnnotations();
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            String enclosedId = pckID + name.lexeme() + "$";
            if (readClass(enclosed::add, enclosedId, modifiers, annotations)) {
                if (Objects.equals(peek().lexeme(), name.lexeme())) {
                    Token constName = consume(IDENTIFIER, "that shouldn't have happened... (expected class name to be identifier)");
                    consumeBracketOpen("constructors");
                    MethodDecl decl = funcDecl(ClassReference.of(VarTypeManager.VOID), annotations, constName, false, false, false);
                    constructors.add(decl);
                } else {
                    ModifierScope.CLASS.check(this, modifiers);
                    ClassReference type = consumeVarType();
                    Token elementName = consumeIdentifier();
                    if (match(BRACKET_O)) {
                        MethodDecl decl = funcDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic(), modifiers.isAbstract());
                        methods.add(decl);
                    } else {
                        if (modifiers.isAbstract()) error(elementName, "fields may not be abstract");
                        FieldDecl decl = fieldDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic());
                        fields.add(decl);
                    }
                }
            }
        }
        consumeCurlyClose("class");
        return new ClassDecl(
                parser, errorLogger,
                classAbstract, classFinal,
                target, name, pckID, superClass,
                implemented.toArray(new ClassReference[0]),
                constructors.toArray(new MethodDecl[0]),
                methods.toArray(new MethodDecl[0]),
                fields.toArray(new FieldDecl[0]),
                enclosed.toArray(new ClassConstructor<?>[0]),
                classAnnotations
        );
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private InterfaceDecl interfaceDecl(String pckID, String fileId, ClassReference target, AnnotationObj[] interfaceAnnotations) {
        consume(INTERFACE, "'interface' expected");

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        if (target == null) target = new ClassReference(name.lexeme());

        parser.addClass(target, null);

        List<ClassReference> parentInterfaces = new ArrayList<>();

        if (match(EXTENDS)) {
            do {
                parentInterfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassConstructor<?>> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            AnnotationObj[] annotations = parseAnnotations();
            modifiers.parse();
            if (readClass(enclosed::add, pckID, modifiers, annotations)) {
                ClassReference type = consumeVarType();
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    ModifierScope.INTERFACE.check(this, modifiers);
                    MethodDecl decl = funcDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic(), !modifiers.isDefault());
                    methods.add(decl);
                } else {
                    ModifierScope.INTERFACE_FIELD.check(this, modifiers);
                    FieldDecl decl = fieldDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic());
                    fields.add(decl);
                }
            }
        }
        consumeCurlyClose("class");
        return new InterfaceDecl(parser, errorLogger, target, name, pckID,
                parentInterfaces.toArray(new ClassReference[0]), methods.toArray(new MethodDecl[0]),
                fields.toArray(new FieldDecl[0]), enclosed.toArray(new ClassDecl[0]), interfaceAnnotations
        );
    }

    private EnumDecl enumDecl(String pckID, String fileId, ClassReference target, AnnotationObj[] enumAnnotations) {
        consume(ENUM, "'enum' expected");

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        if (target == null) target = new ClassReference(name.lexeme());

        parser.addClass(target, null);

        List<ClassReference> interfaces = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                interfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("enum");

        List<EnumConstDecl> enumConstants = new ArrayList<>();

        if (!check(C_BRACKET_C, EOA)) {
            int ordinal = 0;
            do {
                Token constName = consumeIdentifier();
                Token[] args;
                if (match(BRACKET_O)) {
                    args = getBracketEnclosedCode();
                    consumeBracketClose("enum constant");
                } else args = new Token[0];
                enumConstants.add(new EnumConstDecl(constName, ordinal++, args));
            } while (match(COMMA));
        }

        if (!check(C_BRACKET_C)) consumeEndOfArg();

        List<ClassConstructor<?>> enclosed = new ArrayList<>();
        List<MethodDecl> constructors = new ArrayList<>();
        List<MethodDecl> methods = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            AnnotationObj[] annotations = parseAnnotations();
            modifiers.parse();
            String enclosedId = pckID + name.lexeme() + "$";
            if (readClass(enclosed::add, enclosedId, modifiers, annotations)) {
                if (Objects.equals(peek().lexeme(), name.lexeme())) {
                    Token constName = consume(IDENTIFIER, "that shouldn't have happened... (expected class name to be identifier)");
                    consumeBracketOpen("constructors");
                    MethodDecl decl = funcDecl(ClassReference.of(VarTypeManager.VOID), annotations, constName, false, false, false);
                    constructors.add(decl);
                } else {
                    ModifierScope.CLASS.check(this, modifiers);
                    ClassReference type = consumeVarType();
                    Token elementName = consumeIdentifier();
                    if (match(BRACKET_O)) {
                        MethodDecl decl = funcDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic(), modifiers.isAbstract());
                        methods.add(decl);
                    } else {
                        if (modifiers.isAbstract()) error(elementName, "fields may not be abstract");
                        FieldDecl decl = fieldDecl(type, annotations, elementName, modifiers.isFinal(), modifiers.isStatic());
                        fields.add(decl);
                    }
                }
            }
        }

        consumeCurlyClose("enum");

        return new EnumDecl(
                parser, errorLogger,
                target, name, pckID,
                interfaces.toArray(new ClassReference[0]),
                enumConstants.toArray(new EnumConstDecl[0]),
                constructors.toArray(new MethodDecl[0]),
                methods.toArray(new MethodDecl[0]),
                fields.toArray(new FieldDecl[0]),
                enclosed.toArray(new ClassConstructor<?>[0]),
                enumAnnotations
        );
    }

    private AnnotationDecl annotationDecl(String pckId, String fileId, ClassReference target, AnnotationObj[] annotationAnnotations) {
        consume(ANNOTATION, "'annotation' expected");

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        if (target == null) target = new ClassReference(name.lexeme());

        parser.addClass(target, null);

        consumeCurlyOpen("annotation");

        List<AnnotationMethodDecl> methods = new ArrayList<>();
        List<ClassConstructor<?>> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            AnnotationObj[] annotations = parseAnnotations();
            modifiers.parse();
            String enclosedId = pckId + name.lexeme() + "$";
            if (readClass(enclosed::add, enclosedId, modifiers, annotations)) {
                ModifierScope.ANNOTATION.check(this, modifiers);
                ClassReference type = consumeVarType();
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    AnnotationMethodDecl decl = annotationMethodDecl(type, annotations, elementName);
                    methods.add(decl);
                } else error(peek(), "'(' expected");
            }
        }
        consumeCurlyClose("annotation");
        return new AnnotationDecl(
                parser, errorLogger,
                target, name, pckId,
                methods.toArray(new AnnotationMethodDecl[0]),
                enclosed.toArray(new ClassConstructor<?>[0]),
                annotationAnnotations
        );
    }

    private AnnotationMethodDecl annotationMethodDecl(ClassReference type, AnnotationObj[] annotations, Token elementName) {
        consumeBracketClose("annotation");
        Token[] defaultCode = new Token[0];
        boolean defaulted = false;
        if (match(DEFAULT)) {
            defaultCode = getFieldCode();
            defaulted = true;
        }
        else consumeEndOfArg();
        return new AnnotationMethodDecl(defaultCode, annotations, elementName, type, defaulted);
    }

    public ClassConstructor<?> parse(ClassReference target) {
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
        ModifiersParser modifiersParser = MODIFIERS;
        modifiersParser.parse();

        String pckId = pck.stream().map(Token::lexeme).collect(Collectors.joining(".", "", "."));
        AnnotationObj[] annotations = parseAnnotations();
        try {
            return readClass(pckId, fileName, modifiersParser, target, annotations);
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private void checkFileName(Token name, String fileId) {
        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }
    }

    /**
     * @return true if it didn't read a class, false otherwise
     */
    private boolean readClass(Consumer<ClassConstructor<?>> sink, String pckID, ModifiersParser modifiers, AnnotationObj[] annotations) {
        if (check(CLASS)) {
            ModifierScope.CLASS.check(this, modifiers);
            sink.accept(classDecl(modifiers.isAbstract(), modifiers.isFinal(), pckID, null, null, annotations));
        } else if (check(INTERFACE)) {
            ModifierScope.INTERFACE.check(this, modifiers);
            sink.accept(interfaceDecl(pckID, null, null, annotations));
        } else if (check(ENUM)) {
            ModifierScope.ENUM.check(this, modifiers);
            sink.accept(enumDecl(pckID, null, null, annotations));
        } else if (check(ANNOTATION)) {
            ModifierScope.ANNOTATION.check(this, modifiers);
            sink.accept(annotationDecl(pckID, null, null, annotations));
        } else return true;
        return false;
    }

    private ClassConstructor<?> readClass(String pckID, String fileId, ModifiersParser modifiers, ClassReference target, AnnotationObj[] annotations) {
        if (check(CLASS)) {
            return classDecl(modifiers.isAbstract(), modifiers.isFinal(), pckID, fileId, target, annotations);
        } else if (check(INTERFACE)) {
            return interfaceDecl(pckID, fileId, target, annotations);
        } else if (check(ENUM)) {
            return enumDecl(pckID, fileId, target, annotations);
        } else if (check(ANNOTATION)) {
            return annotationDecl(pckID, fileId, target, annotations);
        } else {
            throw error(peek(), "'class', 'interface' or 'enum' expected");
        }
    }

    private MethodDecl funcDecl(ClassReference type, AnnotationObj[] annotations, Token name, boolean isFinal, boolean isStatic, boolean isAbstract) {

        List<Pair<ClassReference, String>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                ClassReference pType = consumeVarType();
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName.lexeme()));
            } while (match(COMMA));
        }
        consumeBracketClose("parameters");

        Token[] code = null;

        if (!isAbstract) { //body only if method isn't abstract
            consumeCurlyOpen("method body");

            code = getCurlyEnclosedCode();

            consumeCurlyClose("method body");
        } else consumeEndOfArg();
        return new MethodDecl(code, annotations, name, parameters, type, isFinal, isStatic, isAbstract);
    }

    private FieldDecl fieldDecl(ClassReference type, AnnotationObj[] annotations, Token name, boolean isFinal, boolean isStatic) {
        Token[] code = null;

        if (match(ASSIGN)) code = getFieldCode();
        else consumeEndOfArg();

        return new FieldDecl(code, annotations, name, type, isFinal, isStatic);
    }

    private void importStmt() {
        consume(IMPORT, "Expected import or class");
        List<Token> packages = readPackage();
        String nameOverride = null;
        if (match(AS)) nameOverride = consumeIdentifier().lexeme();
        consumeEndOfArg();
        ClassReference target = VarTypeManager.getClass(packages, this::error);
        if (target != null) {
            if (parser.hasClass(target, nameOverride)) {
                error(packages.get(packages.size()-1), "unknown class '" + packages.stream().map(Token::lexeme).collect(Collectors.joining(".")) + "'");
            }
            parser.addClass(target, nameOverride);
        }
    }

    private List<Token> readPackage() {
        List<Token> packages = new ArrayList<>();
        packages.add(consumeIdentifier());
        while (!check(EOA, AS)) {
            consume(DOT, "unexpected name");
            packages.add(consumeIdentifier());
        }
        return packages;
    }

    private AnnotationObj[] parseAnnotations() {
        List<AnnotationObj> list = new ArrayList<>();
        while (match(AT)) {
            list.add(parseAnnotationObject());
        }
        return list.toArray(new AnnotationObj[0]);
    }

    private AnnotationObj parseAnnotationObject() {
        AbstractAnnotationClass cInst = parseAnnotationClass();
        Token errorPoint = previous();
        Token[] properties = new Token[0];
        if (match(BRACKET_O)) properties = getBracketEnclosedCode();
        return new AnnotationObj(cInst, errorPoint, properties);
    }


    private Token[] getCurlyEnclosedCode() {
        return getScopedCode(C_BRACKET_O, C_BRACKET_C);
    }

    private Token[] getFieldCode() {
        List<Token> tokens = new ArrayList<>();
        do {
            tokens.add(advance());
        } while (!check(EOA));

        tokens.add(advance());
        return tokens.toArray(Token[]::new);
    }

    private Token[] getBracketEnclosedCode() {
        return getScopedCode(BRACKET_O, BRACKET_C);
    }

    private Token[] getScopedCode(TokenType increase, TokenType decrease) {
        if (peek().type() == decrease) return new Token[0];
        List<Token> tokens = new ArrayList<>();
        int i = 1;
        tokens.add(peek());
        do {
            advance();
            tokens.add(peek());
            if (peek().type() == increase) i++;
            else if (peek().type() == decrease) i--;
        } while (i > 0 && !isAtEnd());
        return tokens.toArray(Token[]::new);
    }

    public record AnnotationMethodDecl(Token[] body, AnnotationObj[] annotations, Token name, ClassReference target, boolean defaulted) {

    }

    public record MethodDecl(Token[] body, AnnotationObj[] annotations, Token name, List<Pair<ClassReference, String>> params, ClassReference type, boolean isFinal, boolean isStatic, boolean isAbstract) {

    }

    public record FieldDecl(Token[] body, AnnotationObj[] annotations, Token name, ClassReference type, boolean isFinal, boolean isStatic) {

    }

    public record EnumConstDecl(Token name, int ordinal, Token[] args) {

    }

    public interface ClassConstructor<I extends Compiler.ClassBuilder> {

        I construct(StmtParser stmtParser, ExprParser exprParser);

        ScriptedClass createSkeleton();

        ClassReference target();

        Token name();

        String pck();

        Compiler.ErrorLogger logger();

        default ScriptedClass applySkeleton() {
            this.target().setTarget(createSkeleton());
            return this.target().get();
        }
    }

    public class ModifiersParser {
        private final Map<TokenType, Token> encountered = new HashMap<>();
        private final List<TokenType> acceptable = new ArrayList<>();
        private final Map<TokenType, List<TokenType>> illegalCombinations = new HashMap<>();
        private final TokenType[] interrupt = {IDENTIFIER, CLASS, INTERFACE, ANNOTATION, ENUM, EOF};

        public ModifiersParser(List<TokenType> acceptable, Map<TokenType, List<TokenType>> combinations) {
            this.acceptable.addAll(acceptable);
            this.illegalCombinations.putAll(combinations);
        }

        public void parse() {
            this.clear();
            a: while (!check(interrupt) && !isAtEnd()) {
                boolean handled = false;
                for (TokenType type : acceptable) {
                    if (match(type)) {
                        if (encountered.containsKey(type)) {
                            error(previous(), "duplicate modifier '" + type.id() + "'");
                            continue a;
                        }
                        encountered.put(type, previous());
                        illegalCombinations.get(type).stream()
                                .filter(encountered::containsKey)
                                .forEach(tokenType ->
                                        error(previous(), String.format(
                                                "Illegal combination of modifiers '%s' and '%s'",
                                                type.id(),
                                                tokenType.id()
                                        ))
                                );
                        handled = true;
                    }
                }
                if (!handled) error(peek(), "modifier or <identifier> expected");
            }
        }

        private void clear() {
            encountered.clear();
        }

        private boolean isFinal() {
            return encountered.containsKey(FINAL);
        }

        private boolean isStatic() {
            return encountered.containsKey(STATIC);
        }

        private boolean isAbstract() {
            return encountered.containsKey(ABSTRACT);
        }

        private boolean isDefault() {
            return encountered.containsKey(DEFAULT);
        }

        public Token get(TokenType type) {
            return encountered.get(type);
        }
    }

    public class ModifiersParserBuilder {
        private final List<TokenType> acceptable = new ArrayList<>();
        private final Map<TokenType, List<TokenType>> illegalCombinations = new HashMap<>();

        public ModifiersParser build() {
            return new ModifiersParser(acceptable, illegalCombinations);
        }

        @SuppressWarnings("UnusedReturnValue")
        public ModifiersParserBuilder accepts(TokenType type) {
            acceptable.add(type);
            return this;
        }

        public ModifiersParserBuilder acceptsAll(TokenType... types) {
            Arrays.stream(types).forEach(this::accepts);
            return this;
        }

        public ModifiersParserBuilder illegalCombination(TokenType type, TokenType other) {
            illegalCombinations.putIfAbsent(type, new ArrayList<>());
            illegalCombinations.get(type).add(other);
            illegalCombinations.putIfAbsent(other, new ArrayList<>());
            illegalCombinations.get(other).add(type);
            return this;
        }
    }

    public enum ModifierScope {
        CLASS(List.of(), DEFAULT),
        INTERFACE(List.of(), FINAL, ABSTRACT),
        INTERFACE_FIELD(List.of()),
        ENUM(List.of(FINAL), ABSTRACT, DEFAULT),
        ANNOTATION(List.of(FINAL), STATIC, DEFAULT);

        private final List<TokenType> illegalModifiers;
        private final List<TokenType> redundantModifiers;

        ModifierScope(List<TokenType> redundantModifiers, TokenType... illegalModifiers) {
            this.redundantModifiers = redundantModifiers;
            this.illegalModifiers = List.of(illegalModifiers);
        }


        public void check(SkeletonParser skeletonParser, ModifiersParser parser) {
            illegalModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> skeletonParser.error(token, String.format("modifier '%s' not allowed here", token.lexeme())));
            redundantModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> skeletonParser.warn(token, String.format("redundant modifier '%s'", token.lexeme())));
        }
    }

    public record AnnotationObj(AbstractAnnotationClass type, Token errorPoint, Token[] params) {
    }
}