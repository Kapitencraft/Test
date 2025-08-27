package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenTypeCategory.*;

@SuppressWarnings("ThrowableNotThrown")
public class ExprParser extends AbstractParser {
    private final List<ClassReference> fallback;
    protected GenericStack generics = new GenericStack();

    public ExprParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
        this.fallback = new ArrayList<>();
    }

    protected ClassReference currentFallback() {
        if (fallback.isEmpty()) throw new IllegalArgumentException("no fallback applied");
        return fallback.get(fallback.size() - 1);
    }

    public void pushGenerics(Holder.Generics generics) {
        generics.pushToStack(this.generics);
    }

    public void pushFallback(ClassReference fallback) {
        this.fallback.add(fallback);
    }

    public void popFallback() {
        if (this.fallback.isEmpty()) throw new IllegalStateException("fallback stack underflow");
        this.fallback.remove(this.fallback.size() - 1);
    }

    public Expr expression() {
        if (match(SWITCH)) {
            return switchExpr();
        }

        return when();
    }

    public Expr literalOrReference() {
        if (match(AT)) {
            SourceClassReference reference = consumeVarType(generics);
            Token errorPoint = previous();
            if (match(BRACKET_O)) {
                parseAnnotationProperties(reference, errorPoint);
            }
        }
        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }
        ClassReference target = consumeVarType(generics).getReference();
        Token name = previous();

        return new Expr.StaticGet(target, name);
    }

    public CompileAnnotationClassInstance parseAnnotation(Holder.AnnotationObj obj, VarTypeParser varTypeParser) {
        this.apply(obj.properties(), varTypeParser);
        return parseAnnotationProperties(obj.type(), obj.type().getToken());
    }

    public CompileAnnotationClassInstance parseAnnotationProperties(SourceClassReference typeRef, Token errorPoint) {
        ScriptedClass type = typeRef.getReference().get();

        if (!type.isAnnotation()) {
            error(typeRef.getToken(), "annotation type expected");
            return null;
        }
        Map<String, ScriptedCallable> annotationMethods = new HashMap<>();

        type.getMethods().asMap().forEach((s, dataMethodContainer) ->
                annotationMethods.put(s, dataMethodContainer.getMethods()[0])
        );

        List<String> abstracts = new ArrayList<>();
        annotationMethods.forEach((s, scriptedCallable) -> {
            if (scriptedCallable.isAbstract()) abstracts.add(s);
        });

        if (isAtEnd()) {
            if (!abstracts.isEmpty()) {
                errorMissingProperties(errorPoint, abstracts);
            }
            return CompileAnnotationClassInstance.noAbstract(type);
        }
        Expr singleProperty;
        if (!check(IDENTIFIER)) {
            singleProperty = literalOrReference();
        } else {
            advance();
            if (check(ASSIGN)) {
                current--;
                Map<String, Expr> properties = new HashMap<>();
                do {
                    Token propertyName = consumeIdentifier();
                    if (properties.containsKey(propertyName.lexeme())) errorLogger.errorF(propertyName, "duplicate annotation property with name %s", propertyName.lexeme());
                    consume(ASSIGN, "'=' expected");
                    Expr property = literalOrReference();
                    properties.put(propertyName.lexeme(), property);
                } while (match(COMMA));
                List<String> requiredProperties = new ArrayList<>(abstracts);
                requiredProperties.removeAll(properties.keySet());
                if (!requiredProperties.isEmpty()) errorMissingProperties(errorPoint, requiredProperties);
                return CompileAnnotationClassInstance.fromPropertyMap(type, properties);
            } else {
                current--;
                singleProperty = literalOrReference();
            }
        }
        if (abstracts.size() > 1) {
            ArrayList<String> c = new ArrayList<>(abstracts);
            c.remove("value");
            errorMissingProperties(errorPoint, c);
        } else if (!abstracts.contains("value")) {
            error(previous(), "can not find annotation method 'value'");
        }
        return CompileAnnotationClassInstance.fromSingleProperty(type, singleProperty);
    }

    private void errorMissingProperties(Token errorPoint, List<String> propertyNames) {
        error(errorPoint, propertyNames.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")) + " missing though required");
    }

    private Expr when() {
        Expr expr = castCheck();
        if (match(QUESTION_MARK)) {
            expectCondition(expr);
            Expr ifTrue = expression();
            consume(TokenType.COLON, "':' expected");
            Expr ifFalse = expression();
            ClassReference ifTrueClass = finder.findRetType(ifTrue);
            ClassReference ifFalseClass = finder.findRetType(ifFalse);
            if (!(ifTrueClass.get().isParentOf(ifFalseClass.get()) || ifFalseClass.get().isParentOf(ifTrueClass.get()))) error(locFinder.find(ifTrue), "both expressions on when statement must return the same type");
            expr = new Expr.When(expr, ifTrue, ifFalse);
        }

        return expr;
    }

    private Expr castCheck() {
        Expr expr = assignment();
        if (match(INSTANCEOF)) {
            ClassReference loxClass = consumeVarType(generics).getReference();
            Token patternVar = null;
            if (match(IDENTIFIER)) {
                patternVar = previous();
                varAnalyser.add(patternVar.lexeme(), loxClass, true, false);
            }
            return new Expr.CastCheck(expr, loxClass, patternVar);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN) || match(OPERATION_ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.VarRef variable) {
                Token name = variable.name();

                checkVarExistence(name, assign.type() != TokenType.ASSIGN,
                        false);
                checkVarType(name, value);
                Pair<ClassReference, Operand> executor;
                if (assign.type() == TokenType.ASSIGN) {
                    varAnalyser.setHasValue(variable.ordinal());
                    executor = Pair.of(WILDCARD, Operand.LEFT);
                } else executor = getExecutor(varAnalyser.getType(name.lexeme()), assign, value);

                return new Expr.Assign(name, value, assign, variable.ordinal(), executor.left(), executor.right());
            } else if (expr instanceof Expr.Get get) {
                ClassReference target = finder.findRetType(get.object());
                expectType(get.name(), target.get().getFieldType(get.name().lexeme()), finder.findRetType(value));

                Pair<ClassReference, Operand> executor;
                if (assign.type() != ASSIGN) executor = getExecutor(target, assign, value);
                else executor = Pair.of(WILDCARD, Operand.LEFT);
                return new Expr.Set(get.object(), get.name(), value, assign, executor.left(), executor.right());
            } else if (expr instanceof Expr.ArrayGet get) {

                Pair<ClassReference, Operand> executor = getExecutor(get, assign, value);
                return new Expr.ArraySet(get.object(), get.index(), value, assign, executor.left(), executor.right());
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.VarRef ref) {
                Token name = ref.name();

                ClassReference type = checkVarExistence(name, true, false);
                if (!type.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorLogger.errorF(name, "Operator '%s' can not be applied to '%s'", assign.lexeme(), type.absoluteName());
                }
                return new Expr.SpecialAssign(name, assign, ref.ordinal(), type);
            }

            if (expr instanceof Expr.Get get) {
                ClassReference reference = finder.findRetType(get.object()).get().getFieldType(get.name().lexeme());
                if (!reference.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorLogger.errorF(get.name(), "Operator '%s' can not be applied to '%s'", assign.lexeme(), reference.absoluteName());
                }
                return new Expr.SpecialSet(get.object(), get.name(), assign, reference);
            }

            if (expr instanceof Expr.ArrayGet arrayGet) {
                ClassReference reference = finder.findRetType(arrayGet.object()).get().getComponentType().reference();
                if (!reference.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorLogger.errorF(locFinder.find(arrayGet.object()), "Operator '%s' can not be applied to '%s'", assign.lexeme(), reference.absoluteName());
                }
                return new Expr.ArraySpecial(arrayGet.object(), arrayGet.index(), assign, reference);
            }
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND, XOR)) {
            Token operator = previous();
            Expr right = equality();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Pair<ClassReference, Operand> getExecutor(ClassReference left, Token operator, ClassReference right) {
        Operand operand = Operand.LEFT;
        ClassReference executor = left;
        OperationType type = OperationType.of(operator.type());
        assert type != null;
        ScriptedClass result = left.get().checkOperation(type, operand, right);
        if (result == VarTypeManager.VOID) {
            operand = Operand.RIGHT;
            result = right.get().checkOperation(type, operand, left);
            executor = right;
        }
        if (result == VarTypeManager.VOID) {
            errorLogger.errorF(operator, "operator '%s' not possible for argument types %s and %s", operator.lexeme(), left.absoluteName(), right.absoluteName());
            return Pair.of(WILDCARD, Operand.LEFT);
        }
        return Pair.of(executor, operand);
    }

    private Pair<ClassReference, Operand> getExecutor(Expr leftArg, Token operator, Expr rightArg) {
        return getExecutor(finder.findRetType(leftArg), operator, finder.findRetType(rightArg));
    }

    private Pair<ClassReference, Operand> getExecutor(ClassReference left, Token operator, Expr rightArg) {
        return getExecutor(left, operator, finder.findRetType(rightArg));
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUALITY)) {
            Token operator = previous();
            Expr right = comparison();
            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, right, operator, executorInfo.left(), executorInfo.right());
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();
            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, right, operator, executorInfo.left(), executorInfo.right());
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();

            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, right, operator, executorInfo.left(), executorInfo.right());
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD, POW)) {
            Token operator = previous();
            Expr right = unary();
            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, right, operator, executorInfo.left(), executorInfo.right());
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            ClassReference executor;
            if (operator.type() == NOT) {
                expectCondition(right);
                executor = VarTypeManager.BOOLEAN.reference();
            }
            else executor = expectType(right, VarTypeManager.NUMBER.reference());
            return new Expr.Unary(operator, right, executor);
        }

        return call();
    }

    private Expr switchExpr() {
        Token keyword = previous();
        consumeBracketOpen("switch");

        expectType(VarTypeManager.ENUM, VarTypeManager.STRING, VarTypeManager.INTEGER.reference(), VarTypeManager.DOUBLE.reference(), VarTypeManager.FLOAT.reference(), VarTypeManager.CHAR.reference());
        Expr provider = expression();
        popExpectation();

        consumeBracketClose("switch");

        consumeCurlyOpen("switch body");
        Map<Object, Expr> params = new HashMap<>();
        Expr def = null;

        while (!check(C_BRACKET_C)) {
            if (match(CASE)) {
                Object key = literal();
                if (params.containsKey(key)) errorLogger.errorF(previous(), "Duplicate case key '%s'", previous().lexeme());
                consume(LAMBDA, "not a statement");
                Expr expr = expression();
                consumeEndOfArg();
                params.put(key, expr);
            } else if (match(DEFAULT)) {
                if (def != null) error(previous(), "Duplicate default key");
                consume(LAMBDA, "not a statement");
                def = expression();
                consumeEndOfArg();
            } else {
                error(peek(), "unexpected token");
            }
        }

        consumeCurlyClose("switch body");
        return new Expr.Switch(provider, params, def, keyword);
    }

    private Expr staticAssign(ClassReference target, Token name) {
        Token type = previous();
        Expr value = expression();
        Pair<ClassReference, Operand> executor = getExecutor(target.get().getStaticFieldType(name.lexeme()), type, value);
        return new Expr.StaticSet(target, name, value, type, executor.left(), executor.right());
    }

    private Expr staticSpecialAssign(ClassReference target, Token name) {
        return new Expr.StaticSpecial(target, name, previous(), target.get().getStaticFieldType(name.lexeme()));
    }

    public Expr[] args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments.toArray(new Expr[0]);
    }

    public ClassReference[] argTypes(Expr[] args) {
        return Arrays.stream(args).map(this.finder::findRetType).toArray(ClassReference[]::new);
    }


    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(S_BRACKET_O)) {
                Token bracketO = previous();
                if (match(COLON)) {
                    Expr end = check(COLON) ? null : expression();
                    consume(COLON, "':' expected");
                    Expr interval = check(S_BRACKET_C) ? null : expression();
                    if (end == null && interval == null) error(bracketO, "slice without any definition");
                    consume(S_BRACKET_C, "']' expected");
                    //expr = new Expr.Slice(expr, null, end, interval);
                    continue;
                }
                Expr index = expression();
                if (match(COLON)) {
                    Expr end = check(COLON) ? null : expression();
                    consume(COLON, "':' expected");
                    Expr interval = check(S_BRACKET_C) ? null : expression();
                    consume(S_BRACKET_C, "']' expected");
                    expr = new Expr.Slice(expr, index, end, interval);
                    continue;
                }
                consume(S_BRACKET_C, "']' expected");
                if (!finder.findRetType(expr).get().isArray()) error(bracketO, "array type expected");
                expr = new Expr.ArrayGet(expr, index);
            } else if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get)
                    expr = finishCall(get.name(), finder.findRetType(get.object()), get.object());
                else error(locFinder.find(expr), "obj expected");
            } else if (match(DOT)) {
                if (expr instanceof Expr.Literal && !check(IDENTIFIER)) continue;
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                ScriptedClass targetType = finder.findRetType(expr).get();
                if (!check(BRACKET_O)) { //ensure not to check for field if it's a method
                    if (!targetType.hasField(name.lexeme())) error(name, "unknown symbol");
                }
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    public ClassReference checkArguments(Expr[] args, ScriptedCallable target, @Nullable ClassReference obj, Token loc) {
        ClassReference[] expectedTypes = target.argTypes();
        ClassReference[] givenTypes = argTypes(args);
        if (expectedTypes.length != givenTypes.length) {
            errorLogger.errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

            errorLogger.logError("required: " + Util.getDescriptor(expectedTypes));
            errorLogger.logError("found:    " + Util.getDescriptor(givenTypes));
            errorLogger.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < givenTypes.length; i++) {
                expectType(locFinder.find(args[i]), givenTypes[i], expectedTypes[i]);
            }
        }

        ClassReference type = target.type();
        //TODO figure out how to extract gotten generics
        if (type instanceof GenericClassReference genericClassReference) {
            GenericStack genericStack = new GenericStack();
            if (obj instanceof AppliedGenericsReference reference) {
                reference.push(genericStack, errorLogger);
            }

            Map<String, ClassReference> types = new HashMap<>();
            for (int i = 0; i < expectedTypes.length; i++) {
                if (expectedTypes[i] instanceof GenericClassReference gCR) {
                    types.put(gCR.getTypeName(), givenTypes[i]);
                }
            }
            if (!types.isEmpty()) genericStack.push(types);


            return genericClassReference.unwrap(genericStack);
        }

        return type;
    }

    private Object literal() {
        if (match(FALSE)) return false;
        if (match(TRUE)) return true;
        if (match(NULL)) return null;

        if (match(NUM, STR)) {
            return previous().literal();
        }

        throw error(peek(), "Expected literal");
    }

    private Expr primary() {
        if (match(NEW)) {
            ClassReference loxClass = consumeVarType(generics).getReference();
            Token loc = previous();
            consumeBracketOpen("constructors");
            Expr[] args = args();
            consumeBracketClose("constructors");

            if (match(C_BRACKET_O)) {

                //TODO parse anonymous
                consumeCurlyClose("anonymous class");
            }

            String signature = VarTypeManager.getMethodSignatureNoTarget("<init>", argTypes(args));

            ScriptedCallable callable = loxClass.get().getMethod(signature);
            checkArguments(args, callable, null, loc);

            Holder.Generics classGenerics = loxClass.get().getGenerics();
            if (classGenerics != null) {
                Map<String, ClassReference> types = new HashMap<>();
                for (int i = 0; i < callable.argTypes().length; i++) {
                    if (callable.argTypes()[i] instanceof GenericClassReference genericClassReference) {
                        types.put(
                                genericClassReference.getTypeName(),
                                finder.findRetType(args[i])
                        );
                    }
                }
                List<ClassReference> ordered = new ArrayList<>();
                for (int i = 0; i < classGenerics.variables().length; i++) {
                    ordered.add(types.get(classGenerics.variables()[i].name().lexeme()));
                }
                loxClass = new AppliedGenericsReference(loxClass, new Holder.AppliedGenerics(loc, ordered.toArray(new ClassReference[0])));
            }

            return new Expr.Constructor(loc, loxClass, args, signature);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            BytecodeVars.FetchResult result = varAnalyser.get(previous.lexeme());
            if (result == BytecodeVars.FetchResult.FAIL) {
                if (currentFallback().exists()) {
                    ClassReference fallbackReference = currentFallback();
                    ScriptedClass fallback = fallbackReference.get();
                    String name = previous.lexeme();
                    if (match(BRACKET_O)) {
                        if (fallback.hasMethod(name)) {
                            return finishCall(previous, fallbackReference, new Expr.VarRef(
                                            Token.createNative("this"),
                                            (byte) 0
                                    )
                            );
                        }
                    } else {
                        if (fallback.hasField(name)) {
                            return  new Expr.Get(new Expr.VarRef(
                                    Token.createNative("this"),
                                    (byte) 0),
                                    previous
                            );
                        }
                        if (fallback.hasStaticField(name)) {
                            return  new Expr.StaticGet(fallbackReference, previous);
                        }
                    }
                }
                current--;
                return statics();
            }
            checkVarExistence(previous, true, true);
            return new Expr.VarRef(
                    previous,
                    result.ordinal()
            );
        }

        if (match(THIS) || match(SUPER)) return new Expr.VarRef(
                previous(),
                (byte)0
        );

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Illegal start of expression");
    }

    private Expr statics() {
        ClassReference target = consumeVarType(generics).getReference();
        Token name = previous();
        if (match(BRACKET_O)) return finishCall(name, target, null);
        if (match(ASSIGN) || match(OPERATION_ASSIGN)) return staticAssign(target, name);
        if (match(GROW, SHRINK)) return staticSpecialAssign(target, name);
        return new Expr.StaticGet(target, name);
    }

    private Expr finishCall(Token name, ClassReference objType, @Nullable Expr obj) {
        Expr[] arguments = args();

        ClassReference[] givenTypes = argTypes(arguments);
        ScriptedClass targetClass = objType.get();

        if (!targetClass.hasMethod(name.lexeme())) {
            error(name, "unknown method '" + name.lexeme() + "'");
            consumeBracketClose("arguments");
            return new Expr.StaticCall(objType, name, arguments, WILDCARD, "?");
        }
        ScriptedCallable callable = Util.getClosest(targetClass, VarTypeManager.getMethodSignatureNoTarget(name.lexeme(), givenTypes));

        ClassReference retType = callable == null ? VarTypeManager.VOID.reference() : checkArguments(arguments, callable, objType, name);
        String signature = VarTypeManager.getMethodSignature(targetClass, name.lexeme(), givenTypes);

        consumeBracketClose("arguments");

        if (callable == null || callable.isStatic()) {
            return new Expr.StaticCall(objType, name, arguments, retType, signature);
        } else {
            if (obj == null) {
                error(name, "Non-static method can not be referenced from a static context");
            }
            return new Expr.InstCall(obj, name, arguments, retType, signature);
        }
    }
}
