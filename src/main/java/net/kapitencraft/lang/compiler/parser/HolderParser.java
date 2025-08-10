package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenType.DOT;

@SuppressWarnings("ThrowableNotThrown")
public class HolderParser extends AbstractParser {
    private GenericStack activeGenerics = new GenericStack();
    private final Deque<String> activePackages = new ArrayDeque<>();

    public HolderParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    public void parseImports() {
        while (check(IMPORT)) {
            importStmt();
        }
    }

    private void importStmt() {
        consume(IMPORT, "Expected import or class");
        List<Token> packages = readPackage();
        String nameOverride = null;
        if (match(AS)) nameOverride = consumeIdentifier().lexeme();
        consumeEndOfArg();
        SourceClassReference target = VarTypeManager.getOrCreateClass(packages);
        if (parser.hasClass(target.getReference(), nameOverride)) {
            error(packages.get(packages.size() - 1), "unknown class '" + packages.stream().map(Token::lexeme).collect(Collectors.joining(".")) + "'");
        }
        parser.addClass(target, nameOverride);
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

    private Holder.AnnotationObj[] parseAnnotations() {
        List<Holder.AnnotationObj> list = new ArrayList<>();
        while (match(AT)) {
            list.add(parseAnnotationObject());
        }
        return list.toArray(new Holder.AnnotationObj[0]);
    }

    private Holder.AnnotationObj parseAnnotationObject() {
        SourceClassReference cInst = consumeVarType();
        Token[] properties = new Token[0];
        if (match(BRACKET_O)) {
            properties = getBracketEnclosedCode();
            consumeBracketClose("annotation object");
        }
        return new Holder.AnnotationObj(cInst, properties);
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

    protected SourceClassReference consumeVarType() {
        StringBuilder typeName = new StringBuilder();
        Token token = consumeIdentifier();
        typeName.append(token.lexeme());
        if (activeGenerics != null) {
            Optional<ClassReference> generic = activeGenerics.getValue(token.lexeme());
            if (generic.isPresent()) {
                return SourceClassReference.from(token, generic.get());
            }
        }
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            if (!check(DOT)) {
                String pckId = this.activePackages.peek();
                if (pckId != null) {
                    Package p = VarTypeManager.getOrCreatePackage(pckId);
                    reference = p.getOrCreateClass(token.lexeme());
                }
            } else {
                Package p = VarTypeManager.getPackage(token.lexeme());
                while (match(DOT) && p != null) {
                    String id = consumeIdentifier().lexeme();
                    typeName.append(".").append(id);
                    if (check(DOT)) p = p.getPackage(id);
                    else reference = p.getOrCreateClass(id);
                }
            }
        }

        if (reference == null) {
            reference = VarTypeManager.getOrCreateClass(typeName.toString(), activePackages.getLast());
        }
        Holder.AppliedGenerics generics = appliedGenerics();
        while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            if (reference != null) reference = reference.array();
        }
        if (reference == null) return null;
        if (generics != null) reference = new AppliedGenericsReference(reference, generics);
        return SourceClassReference.from(token, reference);
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

    private void checkFileName(Token name, String fileId) {
        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }
    }

    public Holder.Class parseFile(String fileName) {
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

        String pckId = pck.stream().map(Token::lexeme).collect(Collectors.joining("."));

        ModifiersParser parser = MODS_NO_GENERICS;
        parser.parse();

        try {
            return switch (advance().type()) {
                case CLASS -> classDecl(parser, pckId, fileName);
                case ENUM -> enumDecl(parser, pckId, fileName);
                case ANNOTATION -> annotationDecl(parser, pckId, fileName);
                case INTERFACE -> interfaceDecl(parser, pckId, fileName);
                default -> throw error(peek(), "'interface', 'class', 'enum' or 'annotation' expected");
            };
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private static ClassReference getOrCreate(String name, String pck) {
        return VarTypeManager.getOrCreateClass(name, pck);
    }



    public void parseClassProperties(ModifierScope.Group scope, List<Holder.Method> methods, @Nullable List<Holder.Constructor> constructors, List<Holder.Field> fields, ClassReference target, List<Holder.Class> enclosed, String pckId, Token name) {
        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            Holder.AnnotationObj[] annotations = modifiers.getAnnotations();
            if (readClass(enclosed::add, pckId, modifiers)) {
                modifiers.generics.pushToStack(activeGenerics);
                if (constructors != null && Objects.equals(peek().lexeme(), name.lexeme())) {
                    Token constName = consume(IDENTIFIER, "that shouldn't have happened... (expected class name to be identifier)");
                    consumeBracketOpen("constructors");
                    Holder.Constructor decl = constDecl(annotations, modifiers.getGenerics(), constName);
                    constructors.add(decl);
                } else {
                    SourceClassReference type = consumeVarType();
                    Token elementName = consumeIdentifier();
                    if (match(BRACKET_O)) {
                        scope.method.check(this, modifiers);
                        Holder.Method decl = funcDecl(type, modifiers, elementName);
                        methods.add(decl);
                    } else {
                        if (modifiers.generics.variables().length > 0) {
                            error(modifiers.generics.variables()[0].name(), "generics not allowed here");
                        }
                        scope.field.check(this, modifiers);
                        if (modifiers.isAbstract()) error(elementName, "fields may not be abstract");
                        Holder.Field decl = fieldDecl(type, annotations, elementName, modifiers.packModifiers());
                        fields.add(decl);
                    }
                }
                activeGenerics.pop();
            }
        }
    }

    public List<Pair<SourceClassReference, String>> parseParams() {
        List<Pair<SourceClassReference, String>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 params.");
                }

                SourceClassReference pType = consumeVarType();
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName.lexeme()));
            } while (match(COMMA));
        }
        return parameters;
    }


    private Holder.Constructor constDecl(Holder.AnnotationObj[] annotation, Holder.Generics generics, Token origin) {
        List<Pair<SourceClassReference, String>> parameters = parseParams();
        consumeBracketClose("params");

        consumeCurlyOpen("method body");

        Token[] code = getCurlyEnclosedCode();

        consumeCurlyClose("method body");

        return new Holder.Constructor(annotation, generics, origin, parameters, code);
    }

    private Holder.Method funcDecl(SourceClassReference type, ModifiersParser modifiers, Token name) {
        List<Pair<SourceClassReference, String>> parameters = parseParams();
        consumeBracketClose("params");

        short mods = modifiers.packModifiers();

        Token[] code = null;

        if (!Modifiers.isAbstract(mods)) { //body only if method isn't abstract
            consumeCurlyOpen("method body");

            GenericStack shadowed = null;
            if (Modifiers.isStatic(mods)) {
                shadowed = activeGenerics;
                activeGenerics = new GenericStack();
            }

            code = getCurlyEnclosedCode();

            if (shadowed != null) activeGenerics = shadowed;

            consumeCurlyClose("method body");
        } else consumeEndOfArg();
        return new Holder.Method(modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), type, name, parameters, code);
    }

    private Holder.Field fieldDecl(SourceClassReference type, Holder.AnnotationObj[] annotations, Token name, short modifiers) {
        Token[] code = null;

        if (match(ASSIGN)) code = getFieldCode();
        else consumeEndOfArg();

        return new Holder.Field(modifiers, annotations, type, name, code);
    }

    /**
     * @return true if it didn't read a class, false otherwise
     */
    private boolean readClass(Consumer<Holder.Class> sink, String pckID, ModifiersParser modifiers) {
        if (match(CLASS)) {
            ModifierScope.CLASS.check(this, modifiers);
            sink.accept(classDecl(modifiers, pckID, null));
        } else if (match(INTERFACE)) {
            ModifierScope.INTERFACE.check(this, modifiers);
            sink.accept(interfaceDecl(modifiers, pckID, null));
        } else if (match(ENUM)) {
            ModifierScope.ENUM.check(this, modifiers);
            sink.accept(enumDecl(modifiers, pckID, null));
        } else if (match(ANNOTATION)) {
            ModifierScope.ANNOTATION.check(this, modifiers);
            sink.accept(annotationDecl(modifiers, pckID, null));
        } else return true;
        return false;
    }

    private Holder.Class classDecl(ModifiersParser mods, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), null);
        SourceClassReference superClass = SourceClassReference.from(null, VarTypeManager.OBJECT);
        Holder.Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            }
            classGenerics.pushToStack(activeGenerics);
        }

        if (match(EXTENDS)) superClass = consumeVarType();

        List<SourceClassReference> implemented = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                implemented.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("class");

        activePackages.push(pckID + "." + name.lexeme());

        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Constructor> constructors = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();
        List<Holder.Class> enclosed = new ArrayList<>();

        parseClassProperties(Modifiers.isAbstract(mods.packModifiers()) ? ModifierScope.Group.ABSTRACT_CLASS : ModifierScope.Group.CLASS, methods, constructors, fields, target, enclosed, pckID + (fileId == null ? "$" : ".") + name.lexeme(), name);

        consumeCurlyClose("class");

        if (stack != null) activeGenerics = stack;
        activePackages.pop();
        return new Holder.Class(ClassType.CLASS,
                target,
                mods.packModifiers(),
                mods.getAnnotations(),
                classGenerics,
                pckID, name,
                superClass,
                implemented.toArray(new SourceClassReference[0]),
                constructors.toArray(new Holder.Constructor[0]),
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                null,
                enclosed.toArray(new Holder.Class[0])
        );
    }

    protected @Nullable Holder.AppliedGenerics appliedGenerics() {
        if (match(LESSER)) {
            Token t = previous();
            List<ClassReference> references = new ArrayList<>();
            do {
                references.add(consumeVarType().getReference());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.AppliedGenerics(t, references.toArray(new ClassReference[0]));
        }
        return null;
    }

    protected @Nullable Holder.Generics generics() {
        if (match(LESSER)) {
            List<Holder.Generic> generics = new ArrayList<>();
            do {
                generics.add(generic());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.Generics(generics.toArray(new Holder.Generic[0]));
        }
        return null;
    }

    private Holder.Generic generic() {
        Token name = consumeIdentifier();
        SourceClassReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarType();
        } else if (match(SUPER)) {
            upperBound = consumeVarType();
        }

        return new Holder.Generic(name, lowerBound, upperBound);
    }


    private Holder.Class enumDecl(ModifiersParser modifiers, String pckID, String fileId) {

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), null);

        List<SourceClassReference> interfaces = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                interfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("enum");
        activePackages.push(pckID + "." + name.lexeme());

        List<Holder.EnumConstant> enumConstants = new ArrayList<>();

        if (!check(C_BRACKET_C, EOA)) {
            int ordinal = 0;
            do {
                Token constName = consumeIdentifier();
                Token[] args;
                if (match(BRACKET_O)) {
                    args = getBracketEnclosedCode();
                    consumeBracketClose("enum constant");
                } else args = new Token[0];
                enumConstants.add(new Holder.EnumConstant(constName, ordinal++, args));
            } while (match(COMMA));
        }

        if (!check(C_BRACKET_C)) consumeEndOfArg();

        List<Holder.Constructor> constructors = new ArrayList<>();
        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();
        List<Holder.Class> enclosed = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.ENUM, methods, constructors, fields, target, enclosed, pckID, name);

        consumeCurlyClose("enum");
        activePackages.pop();

        return new Holder.Class(ClassType.ENUM,
                target, modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), pckID, name,
                null,
                interfaces.toArray(new SourceClassReference[0]),
                constructors.toArray(new Holder.Constructor[0]),
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                enumConstants.toArray(new Holder.EnumConstant[0]),
                enclosed.toArray(new Holder.Class[0])
        );
    }

    private Holder.Class annotationDecl(ModifiersParser mods, String pckId, String fileId) {

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckId);

        parser.addClass(SourceClassReference.from(name, target), null);

        consumeCurlyOpen("annotation");
        activePackages.push(pckId + "." + name.lexeme());

        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Class> enclosed = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            Holder.AnnotationObj[] annotations = modifiers.getAnnotations();
            String enclosedId = pckId + (fileId == null ? "$" : ".") + name.lexeme();
            if (readClass(enclosed::add, enclosedId, modifiers)) {
                ModifierScope.ANNOTATION.check(this, modifiers);
                SourceClassReference type = consumeVarType();
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    Holder.Method decl = annotationMethodDecl(type, annotations, elementName);
                    methods.add(decl);
                } else error(peek(), "'(' expected");
            }
        }

        consumeCurlyClose("annotation");
        activePackages.pop();
        return new Holder.Class(ClassType.ANNOTATION,
                target, mods.packModifiers(), mods.getAnnotations(), mods.getGenerics(), pckId, name,
                null, null, null,
                methods.toArray(new Holder.Method[0]),
                null, null,
                enclosed.toArray(new Holder.Class[0])
        );
    }

    private Holder.Method annotationMethodDecl(SourceClassReference type, Holder.AnnotationObj[] annotations, Token elementName) {
        consumeBracketClose("annotation");
        Token[] defaultCode = new Token[0];
        boolean defaulted = false;
        if (match(DEFAULT)) {
            defaultCode = getFieldCode();
            defaulted = true;
        }
        else consumeEndOfArg();
        return new Holder.Method(Modifiers.pack(false, false, !defaulted), annotations, null, type, elementName, List.of(), defaultCode);
    }

    private Holder.Class interfaceDecl(ModifiersParser mods, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), null);

        Holder.Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            } else classGenerics.pushToStack(activeGenerics);
        }

        List<SourceClassReference> parentInterfaces = new ArrayList<>();

        if (match(EXTENDS)) {
            do {
                parentInterfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        activePackages.push(pckID + "." + name.lexeme());

        consumeCurlyOpen("class");

        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();
        List<Holder.Class> enclosed = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.INTERFACE, methods, null, fields, target, enclosed, pckID, name);

        consumeCurlyClose("class");

        if (stack != null) activeGenerics = stack;
        else if (classGenerics != null) activeGenerics.pop();
        activePackages.pop();
        return new Holder.Class(ClassType.INTERFACE, target, mods.packModifiers(),
                mods.getAnnotations(), classGenerics, pckID, name, null,
                parentInterfaces.toArray(new SourceClassReference[0]),
                null,
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                null,
                enclosed.toArray(new Holder.Class[0])
        );
    }

    private final ModifiersParser MODIFIERS = from(true, FINAL, ABSTRACT, STATIC, DEFAULT);
    private final ModifiersParser MODS_NO_GENERICS = from(false, FINAL, ABSTRACT, STATIC, DEFAULT);

    public ModifiersParser from(boolean generics, TokenType... accepted) {
        Map<TokenType, List<TokenType>> illegals = new HashMap<>();
        for (TokenType type : accepted) {
            List<TokenType> others = new ArrayList<>();
            for (TokenType type1 : accepted) {
                if (type != type1) others.add(type1);
            }
            illegals.put(type, others);
        }
        return new ModifiersParser(List.of(accepted), illegals, generics);
    }

    public class ModifiersParser {
        private final Map<TokenType, Token> encountered = new HashMap<>();
        private final List<TokenType> acceptable = new ArrayList<>();
        private Holder.Generics generics;
        private Holder.AnnotationObj[] annotations;
        private final Map<TokenType, List<TokenType>> illegalCombinations = new HashMap<>();
        private final TokenType[] interrupt = {IDENTIFIER, CLASS, INTERFACE, ANNOTATION, ENUM, EOF};
        private boolean defaultAbstract = false;
        private final boolean allowGenerics;

        public ModifiersParser(List<TokenType> acceptable, Map<TokenType, List<TokenType>> combinations, boolean allowGenerics) {
            this.acceptable.addAll(acceptable);
            this.illegalCombinations.putAll(combinations);
            this.allowGenerics = allowGenerics;
        }

        public void parse() {
            annotations = parseAnnotations();
            this.clear();
            List<Holder.Generic> generics = new ArrayList<>();
            a: while (!check(interrupt) && !isAtEnd()) {
                boolean handled = false;
                if (match(LESSER) && allowGenerics) {
                    if (!generics.isEmpty()) error(previous(), "duplicate generic declaration");
                    do {
                        generics.add(generic());
                    } while (match(COMMA));
                    consume(GREATER, "unclosed generic declaration");
                    handled = true;
                } else for (TokenType type : acceptable) {
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
            this.generics = new Holder.Generics(generics.toArray(new Holder.Generic[0]));
        }

        private void clear() {
            encountered.clear();
        }

        private short packModifiers() {
            return Modifiers.pack(isFinal(), isStatic(), isAbstract() && !isDefault());
        }

        private boolean isFinal() {
            return encountered.containsKey(FINAL);
        }

        private boolean isStatic() {
            return encountered.containsKey(STATIC);
        }

        private boolean isAbstract() {
            return defaultAbstract || encountered.containsKey(ABSTRACT);
        }

        private boolean isDefault() {
            return encountered.containsKey(DEFAULT);
        }

        public Token get(TokenType type) {
            return encountered.get(type);
        }

        public Holder.AnnotationObj[] getAnnotations() {
            return annotations;
        }

        public void setDefaultAbstract(boolean b) {
            this.defaultAbstract = b;
        }

        public Holder.Generics getGenerics() {
            return generics;
        }
    }

    public enum ModifierScope {
        ABSTRACT_CLASS(List.of()),
        CLASS(List.of(), DEFAULT),
        INTERFACE(List.of(ABSTRACT), FINAL),
        INTERFACE_FIELD(List.of()),
        ENUM(List.of(FINAL), ABSTRACT, DEFAULT),
        ANNOTATION(List.of(FINAL), STATIC, DEFAULT);

        private final List<TokenType> illegalModifiers;
        private final List<TokenType> redundantModifiers;

        ModifierScope(List<TokenType> redundantModifiers, TokenType... illegalModifiers) {
            this.redundantModifiers = redundantModifiers;
            this.illegalModifiers = List.of(illegalModifiers);
        }


        public void check(HolderParser holderParser, ModifiersParser parser) {
            parser.setDefaultAbstract(this == INTERFACE);
            illegalModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> holderParser.error(token, java.lang.String.format("modifier '%s' not allowed here", token.lexeme())));
            redundantModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> holderParser.warn(token, java.lang.String.format("redundant modifier '%s'", token.lexeme())));
        }

        public enum Group {
            ABSTRACT_CLASS(ModifierScope.ABSTRACT_CLASS, ModifierScope.CLASS),
            CLASS(ModifierScope.CLASS, ModifierScope.CLASS),
            INTERFACE(ModifierScope.INTERFACE, ModifierScope.INTERFACE_FIELD),
            ENUM(ModifierScope.ENUM, ModifierScope.CLASS);

            private final ModifierScope method, field;

            Group(ModifierScope method, ModifierScope field) {
                this.method = method;
                this.field = field;
            }
        }
    }
}
