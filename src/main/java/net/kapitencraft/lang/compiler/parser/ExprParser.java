package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenTypeCategory.*;

@SuppressWarnings("ThrowableNotThrown")
public class ExprParser extends AbstractParser {

    public ExprParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    public Expr expression() {
        if (match(SWITCH)) {
            return switchExpr();
        }

        return when();
    }

    public Expr literalOrReference() {
        if (match(AT)) {
            AbstractAnnotationClass target = parseAnnotationClass();
            Token errorPoint = previous();
            if (match(BRACKET_O)) {
                parseAnnotationProperties(target, errorPoint);
            }
        }
        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous().literal());
        }
        ClassReference target = consumeVarType();
        Token name = previous();

        return new Expr.StaticGet(target, name);
    }

    public AnnotationClassInstance parseAnnotation(SkeletonParser.AnnotationObj obj, VarTypeParser varTypeParser) {
        this.apply(obj.params(), varTypeParser);
        return parseAnnotationProperties(obj.type(), obj.errorPoint());
    }

    public AnnotationClassInstance parseAnnotationProperties(AbstractAnnotationClass type, Token errorPoint) {
        List<String> abstracts = type.getAbstracts();
        if (isAtEnd()) {
            if (!abstracts.isEmpty()) {
                errorMissingProperties(errorPoint, abstracts);
            }
            return AnnotationClassInstance.noAbstract(type);
        }
        Expr singleProperty;
        if (!check(IDENTIFIER)) {
            singleProperty = literalOrReference();
        } else {
            advance();
            if (check(EQUAL)) {
                current--;
                Map<String, Expr> properties = new HashMap<>();
                do {
                    Token propertyName = consumeIdentifier();
                    if (properties.containsKey(propertyName.lexeme())) errorLogger.errorF(propertyName, "duplicate annotation property with name %s", propertyName.lexeme());
                    consume(EQUAL, "'=' expected");
                    Expr property = literalOrReference();
                    properties.put(propertyName.lexeme(), property);
                } while (match(COMMA));
                List<String> requiredProperties = new ArrayList<>(abstracts);
                requiredProperties.removeAll(properties.keySet());
                if (!requiredProperties.isEmpty()) errorMissingProperties(errorPoint, requiredProperties);
                return AnnotationClassInstance.fromPropertyMap(type, properties);
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
        return AnnotationClassInstance.fromSingleProperty(type, singleProperty);
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
            ClassReference loxClass = consumeVarType();
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
                Token name = variable.name;

                checkVarExistence(name, assign.type() != TokenType.ASSIGN,
                        false);
                checkVarType(name, value);
                Pair<ClassReference, Operand> executor;
                if (assign.type() == TokenType.ASSIGN) {
                    varAnalyser.setHasValue(name.lexeme());
                    executor = Pair.of(VarTypeManager.VOID.reference(), Operand.LEFT);
                } else executor = getExecutor(varAnalyser.getType(name.lexeme()), assign, value);

                return new Expr.Assign(name, value, assign, executor.left(), executor.right());
            } else if (expr instanceof Expr.Get get) {
                ClassReference target = finder.findRetType(get.object);
                expectType(get.name, target.get().getFieldType(get.name.lexeme()), finder.findRetType(value));

                Pair<ClassReference, Operand> executor;
                if (assign.type() != ASSIGN) executor = getExecutor(target, assign, value);
                else executor = Pair.of(VarTypeManager.VOID.reference(), Operand.LEFT);
                return new Expr.Set(get.object, get.name, value, assign, executor.left(), executor.right());
            } else if (expr instanceof Expr.ArrayGet get) {

                Pair<ClassReference, Operand> executor = getExecutor(get, assign, value);
                return new Expr.ArraySet(get.object, get.index, value, assign, executor.left(), executor.right());
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.VarRef ref) {
                Token name = ref.name;

                checkVarExistence(name, true, false);
                return new Expr.SpecialAssign(name, assign);
            }

            if (expr instanceof Expr.Get get) {
                return new Expr.SpecialSet(get.object, get.name, assign);
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
        OperationType type = OperationType.of(operator);
        assert type != null;
        ScriptedClass result = left.get().checkOperation(type, operand, right);
        if (result == VarTypeManager.VOID) {
            operand = Operand.RIGHT;
            result = right.get().checkOperation(type, operand, left);
            executor = right;
        }
        if (result == VarTypeManager.VOID) {
            errorLogger.errorF(operator, "operator '%s' not possible for argument types %s and %s", operator.lexeme(), left.absoluteName(), right.absoluteName());
            return Pair.of(ClassReference.of(VarTypeManager.VOID), Operand.LEFT);
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
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();
            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();

            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD, POW)) {
            Token operator = previous();
            Expr right = unary();
            Pair<ClassReference, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            if (operator.type() == NOT) expectCondition(right);
            else expectType(right, VarTypeManager.NUMBER.reference());
            return new Expr.Unary(operator, right);
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

    private Expr finishInstCall(Expr.Get get) {
        List<Expr> arguments = args();

        List<ClassReference> givenTypes = arguments.stream().map(this.finder::findRetType).toList();
        ClassReference targetClass = this.finder.findRetType(get.object);

        int ordinal = targetClass.get().getMethodOrdinal(get.name.lexeme(), givenTypes);
        if (ordinal == -1) {
            error(get.name, "unknown symbol");
            consumeBracketClose("arguments");
            return new Expr.InstCall(get.object, get.name, ordinal, arguments);
        }
        ScriptedCallable callable = targetClass.get().getMethodByOrdinal(get.name.lexeme(), ordinal);

        checkArguments(arguments, callable, get.name);

        consumeBracketClose("arguments");

        return new Expr.InstCall(get.object, get.name, ordinal, arguments);
    }

    private Expr statics() {
        ClassReference target = consumeVarType();
        Token name = previous();
        if (check(BRACKET_O)) return staticCall(target, name);
        if (match(ASSIGN) || match(OPERATION_ASSIGN)) return staticAssign(target, name);
        if (match(GROW, SHRINK)) return staticSpecialAssign(target, name);
        return new Expr.StaticGet(target, name);
    }

    private Expr staticCall(ClassReference target, Token name) {
        consumeBracketOpen("static call");
        List<Expr> args = args();


        int ordinal = target.get().getStaticMethodOrdinal(name.lexeme(), argTypes(args));
        if (ordinal == -1) {
            error(name, "unknown symbol");
            consumeBracketClose("static call");
            return new Expr.StaticCall(target, name, ordinal, args);
        }

        ScriptedCallable callable = target.get().getStaticMethodByOrdinal(name.lexeme(), ordinal);
        checkArguments(args, callable, name);

        consumeBracketClose("static call");

        return new Expr.StaticCall(target, name, ordinal, args);
    }

    private Expr staticAssign(ClassReference target, Token name) {
        Token type = previous();
        Expr value = expression();
        Pair<ClassReference, Operand> executor = getExecutor(target.get().getStaticFieldType(name.lexeme()), type, value);
        return new Expr.StaticSet(target, name, value, type, executor.left(), executor.right());
    }

    private Expr staticSpecialAssign(ClassReference target, Token name) {
        return new Expr.StaticSpecial(target, name, previous());
    }

    public List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments;
    }

    public List<ClassReference> argTypes(List<Expr> args) {
        return args.stream().map(this.finder::findRetType).toList();
    }


    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(S_BRACKET_O)) {
                Token bracketO = previous();
                Expr index = expression();
                consume(S_BRACKET_C, "']' expected");
                if (!finder.findRetType(expr).get().isArray()) error(bracketO, "array type expected");
                expr = new Expr.ArrayGet(expr, index);
            } else if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get) expr = finishInstCall(get);
                else error(locFinder.find(expr), "obj expected");
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                ScriptedClass targetType = finder.findRetType(expr).get();
                if (!check(BRACKET_O)) { //ensure not to check for field if it's a method
                    if (!targetType.hasField(name.lexeme())) error(name, "unknown symbol");
                } else
                    if (!targetType.hasMethod(name.lexeme())) error(name, "unknown symbol");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    public void checkArguments(List<Expr> args, ScriptedCallable target, Token loc) {
        List<ClassReference> expectedTypes = target.argTypes();
        List<ClassReference> givenTypes = argTypes(args);
        if (expectedTypes.size() != givenTypes.size()) {
            errorLogger.errorF(loc, "constructor for %s cannot be applied to given types;", loc.lexeme());

            errorLogger.logError("required: " + Util.getDescriptor(expectedTypes));
            errorLogger.logError("found:    " + Util.getDescriptor(givenTypes));
            errorLogger.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < givenTypes.size(); i++) {
                expectType(locFinder.find(args.get(i)), givenTypes.get(i), expectedTypes.get(i));
            }
        }

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
            ClassReference loxClass = consumeVarType();
            Token loc = previous();
            consumeBracketOpen("constructors");
            List<Expr> args = args();
            consumeBracketClose("constructors");
            MethodContainer constructorContainer = loxClass.get().getConstructor();
            int ordinal = constructorContainer.getMethodOrdinal(argTypes(args));
            ScriptedCallable callable = constructorContainer.getMethodByOrdinal(ordinal);

            checkArguments(args, callable, loc);

            return new Expr.Constructor(loc, loxClass, args, ordinal);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous().literal());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (!varAnalyser.has(previous.lexeme())) {
                current--;
                return statics();
            }
            checkVarExistence(previous, true, true);
            return new Expr.VarRef(previous);
        }

        if (match(THIS)) return new Expr.VarRef(previous());

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Illegal start of expression");
    }
}
