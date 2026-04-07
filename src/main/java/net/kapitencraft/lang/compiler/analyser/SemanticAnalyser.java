package net.kapitencraft.lang.compiler.analyser;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.ElifBranch;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.lang.tool.Util;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SemanticAnalyser implements Stmt.Visitor<Void>, Expr.Visitor<ClassReference> {

    //region expect
    private final ArrayDeque<Set<ClassReference>> activeArgs = new ArrayDeque<>();
    private ClassReference methodReturnType = VarTypeManager.VOID.reference();

    private boolean checkActive(ClassReference reference) {
        return !activeArgs.isEmpty() && activeArgs.peek().contains(reference);
    }

    private void pushExpected(Set<ClassReference> references) {
        activeArgs.push(references);
    }

    private void pushExpected(ClassReference... references) {
        pushExpected(Set.of(references));
    }

    private void popExpected() {
        activeArgs.pop();
    }

    protected ClassReference expectType(Token errorLoc, ClassReference gotten, ClassReference expected) {
        if (expected == VarTypeManager.OBJECT) return gotten;
        if (!gotten.get().isChildOf(expected.get()))
            errorStorage.errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        if (gotten instanceof AppliedGenericsReference reference) {
            if (expected instanceof AppliedGenericsReference reference1) {
                Holder.AppliedGenerics gottenAppliedGenerics = reference.getApplied();
                Holder.AppliedGenerics expectedAppliedGenerics = reference1.getApplied();
                ClassReference[] expectedGenerics = expectedAppliedGenerics.references();
                ClassReference[] gottenGenerics = gottenAppliedGenerics.references();

                if (expectedGenerics.length != gottenGenerics.length) {
                    errorStorage.errorF(gottenAppliedGenerics.reference(), "Wrong number of type arguments: %s; required: %s", gottenGenerics.length, expectedGenerics.length);
                } else {
                    for (int i = 0; i < expectedGenerics.length; i++) {
                        if (!expectedGenerics[i].get().isChildOf(gottenGenerics[i].get())) {
                            String name = reference1.getGenerics().variables()[i].name().lexeme();
                            errorStorage.errorF(reference.getApplied().reference(), "incompatible types: inference variable %s has incompatible bounds", name);

                            errorStorage.logError("gotten: " + gottenGenerics[i].name());
                            errorStorage.logError("lower bounds: " + expectedGenerics[i].name());
                        }
                    }
                }
            } else {
                errorStorage.errorF(reference.getApplied().reference(), "Type '%s' does not have type parameters", expected.absoluteName());
            }
        }
        return gotten;
    }

    //endregion

    //region executor
    private OperationInfo getOperationInfo(ClassReference left, Token operator, ClassReference right) {
        OperationType operation = OperationType.of(operator.type());
        assert operation != null;
        ScriptedClass result = VarTypeManager.VOID;
        if (left.get() instanceof PrimitiveClass || left.is(VarTypeManager.STRING.get()) || right.is(VarTypeManager.STRING.get())) {
            result = left.get().checkOperation(operation, right);
        }
        //search for overloads
        if (result == VarTypeManager.VOID && operation.getMethodName() != null) {
            String signature = operation.getMethodName() + "(" + VarTypeManager.getClassName(right) + ")";
            ScriptedCallable method = left.get().getMethod(signature);
            if (method != null && !method.isStatic()) {
                signature = VarTypeManager.getClassName(left) + signature;
                return new OperationInfo(left, method.retType(), signature);
            }
        }
        if (result == VarTypeManager.VOID) {
            errorStorage.errorF(operator, "operator '%s' not possible for argument types %s and %s", operator.lexeme(), left.absoluteName(), right.absoluteName());
            return OperationInfo.UNKNOWN;
        }
        return new OperationInfo(left, result.reference(), null);
    }

    private OperationInfo getOperationInfo(Expr leftArg, Token operator, Expr rightArg) {
        return getOperationInfo(analyseExpr(leftArg), operator, analyseExpr(rightArg));
    }

    private OperationInfo getOperationInfo(ClassReference left, Token operator, Expr rightArg) {
        return getOperationInfo(left, operator, analyseExpr(rightArg));
    }

    private OperationInfo getOperationInfo(Token operator, ClassReference reference) {
        if (reference.get() instanceof PrimitiveClass) {
            return new OperationInfo(reference, reference, null);
        }
        String methodSig = OperationType.of(operator.type()).getMethodName() + "()";

        ScriptedCallable method = reference.get().getMethod(methodSig);

        if (method != null && !method.isStatic()) {
            return new OperationInfo(reference, method.retType(), VarTypeManager.getClassName(reference) + methodSig);
        } else {
            errorF(operator, "operation '%s' not applicable for argument type %s", OperationType.of(operator.type()).getMethodName(), reference.absoluteName());
        }

        return OperationInfo.UNKNOWN;
    }

    public void analyseBody(Stmt[] body, ClassReference retType, List<? extends Pair<SourceClassReference, String>> params, @Nullable ClassReference targetClass) {
        this.methodReturnType = retType;
        if (targetClass != null) this.varAnalyser.add("this", targetClass, false, true);
        for (Pair<SourceClassReference, String> param : params) {
            varAnalyser.add(param.getSecond(), param.getFirst().getReference(), true, true);
        }
        for (Stmt stmt : body) {
            this.analyseStmt(stmt);
        }
    }

    private void analyseCall(Token name, ClassReference objType, @Nullable Expr obj, Expr[] args) {
        ClassReference[] argTypes = args(args);
        ScriptedClass targetClass = objType.get();

        if (!targetClass.hasMethod(name.lexeme())) {
            errorF(name, "unknown method '%s' in class %s", name.lexeme(), objType.absoluteName());
        }
        ScriptedCallable callable = Util.getVirtualMethod(targetClass, name.lexeme(), argTypes);
        ClassReference retType = VarTypeManager.VOID.reference();
        String signature = null;
        if (callable != null) {
            retType = checkArguments(args, argTypes, callable, objType, name);
            signature = VarTypeManager.getMethodSignature(targetClass, name.lexeme(), callable.argTypes());
        }
    }

    private ScriptedCallable tryGetConstructorMethod(Expr[] args, ClassReference type, ScriptedClass scriptedClass, Token loc) {
        DataMethodContainer container = scriptedClass.getMethods().get("<init>");
        ClassReference[] argTypes = this.args(args);
        if (container == null) {
            if (args.length > 0) {
                errorStorage.errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

                errorStorage.logError("required: ");
                errorStorage.logError("found:    " + Util.getDescriptor(argTypes));
                errorStorage.logError("reason: actual and formal argument lists differ in length");
            }
            return null;
        }

        return Util.getVirtualMethod(scriptedClass, "<init>", argTypes);
    }

    private record OperationInfo(ClassReference executor, ClassReference result,
                                 @Nullable String methodSignature) {
        private static final OperationInfo UNKNOWN = new OperationInfo(VarTypeManager.VOID.reference(), VarTypeManager.VOID.reference(), null);
    }

    //endregion

    //region var & scope analysis
    protected byte tryCreateVar(Token name, ClassReference type, boolean hasValue, boolean isFinal) {
        byte ordinal = varAnalyser.add(name.lexeme(), type, !isFinal, hasValue);
        if (ordinal == -1)
            errorStorage.errorF(name, "Variable '%s' already defined in current scope", name.lexeme());
        return ordinal;
    }

    protected BytecodeVars.FetchResult checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        BytecodeVars.FetchResult result = varAnalyser.get(varName);
        if (result == BytecodeVars.FetchResult.FAIL) {
            error(name, "cannot find symbol");
        } else if (requireValue && !result.assigned()) {
            error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
        } else if (!mayBeFinal && !result.canAssign()) {
            error(name, "Can not assign to final variable");
        }
        return result;
    }

    private void pushScope() {
        varAnalyser.push();
    }

    private int popScope() {
        return varAnalyser.pop();
    }

    private final BytecodeVars varAnalyser = new BytecodeVars();
    //endregion

    private final LocationAnalyser locFinder = new LocationAnalyser();

    //region error
    private void errorIncompatibleTypes(Expr expr, ClassReference expected, ClassReference gotten) {
        errorStorage.errorF(expr, "incompatible types: %s cannot be converted to %s", gotten, expected);
    }

    private void error(Token loc, String msg) {
        errorStorage.error(loc, msg);
    }

    private void errorF(Token loc, String formatMsg, Object... args) {
        errorStorage.errorF(loc, formatMsg, args);
    }

    private final Compiler.ErrorStorage errorStorage;
    //endregion

    public SemanticAnalyser(Compiler.ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

    public ClassReference analyseExpr(Expr expr) {
        return expr.accept(this);
    }

    public ClassReference[] args(Expr[] args) {
        return Arrays.stream(args).map(this::analyseExpr).toArray(ClassReference[]::new);
    }

    public ClassReference checkArguments(Expr[] args, ClassReference[] argTypes, @Nullable ScriptedCallable target, @Nullable ClassReference obj, Token loc) {
        ClassReference[] expectedTypes = target == null ? new ClassReference[0] : target.argTypes();
        if (expectedTypes.length != argTypes.length) {
            errorStorage.errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

            errorStorage.logError("required: " + Util.getDescriptor(expectedTypes));
            errorStorage.logError("found:    " + Util.getDescriptor(argTypes));
            errorStorage.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < argTypes.length; i++) {
                expectType(locFinder.find(args[i]), argTypes[i], expectedTypes[i]);
            }
        }

        ClassReference type = target == null ? VarTypeManager.VOID.reference() : target.retType();
        //TODO figure out how to extract gotten generics
        if (type instanceof GenericClassReference genericClassReference) {
            GenericStack genericStack = new GenericStack();
            if (obj instanceof AppliedGenericsReference reference) {
                reference.push(genericStack, errorStorage);
            }

            Map<String, ClassReference> types = new HashMap<>();
            for (int i = 0; i < expectedTypes.length; i++) {
                if (expectedTypes[i] instanceof GenericClassReference gCR) {
                    types.put(gCR.getTypeName(), argTypes[i]);
                }
            }
            if (!types.isEmpty()) genericStack.push(types);


            return genericClassReference.unwrap(genericStack);
        }

        return type;
    }


    private void analyseStmt(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public ClassReference visitVarRefExpr(Expr.VarRef expr) {
        BytecodeVars.FetchResult fetchResult = varAnalyser.get(expr.name.lexeme());
        if (fetchResult == BytecodeVars.FetchResult.FAIL)
            errorStorage.error(expr.name, "unknown local variable: " + expr.name.lexeme());
        expr.retType = fetchResult.type();

        return fetchResult.type();
    }

    @Override
    public ClassReference visitSetExpr(Expr.Set expr) {
        ClassReference objType = analyseExpr(expr.object);

        String fieldName = expr.name.lexeme();
        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (objType.get().hasField(fieldName)) {
            fieldType = objType.get().getFieldType(expr.name.lexeme());
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        ClassReference valType = analyseExpr(expr.value);

        if (!fieldType.is(valType)) {
            errorIncompatibleTypes(expr.value, fieldType, valType);
        }

        OperationInfo operationInfo;
        if (expr.assignType.type() == TokenType.ASSIGN) {
            operationInfo = OperationInfo.UNKNOWN;
        } else
            operationInfo = getOperationInfo(varAnalyser.getType(expr.name.lexeme()), expr.assignType, expr.value);
        expr.executor = operationInfo.executor;

        expr.retType = fieldType;

        return fieldType;
    }

    @Override
    public ClassReference visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(locFinder.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        OperationInfo info = getOperationInfo(expr.assignType, component.reference());

        expr.executor = info.executor;

        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitInstCallExpr(Expr.InstCall expr) {
        return null;
    }

    @Override
    public ClassReference visitSuperCallExpr(Expr.SuperCall expr) {
        return null;
    }

    @Override
    public ClassReference visitLogicalExpr(Expr.Logical expr) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(expr.left);
        analyseExpr(expr.right);
        popExpected();

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitComparisonChainExpr(Expr.ComparisonChain expr) {
        expr.dataType = analyseExpr(expr.entries[0]);
        if (!expr.dataType.get().isChildOf(VarTypeManager.NUMBER)) {
            error(locFinder.find(expr.entries[0]), "number expected");
        }

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitCastCheckExpr(Expr.CastCheck expr) {
        if (expr.patternVarName != null) {
            tryCreateVar(expr.patternVarName, expr.targetType, true, true);
        }

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitArrayGetExpr(Expr.ArrayGet expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(locFinder.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        ClassReference indexType = analyseExpr(expr.index);
        if (!indexType.is(VarTypeManager.INTEGER)) {
            errorF(locFinder.find(expr.index), "integer expected; found '%s'", indexType.absoluteName());
        }

        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitLiteralExpr(Expr.Literal expr) {
        ScriptedClass type = expr.literal.literal().type();
        return expr.retType = type.reference();
    }

    @Override
    public ClassReference visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        pushExpected(VarTypeManager.INTEGER.reference());
        analyseExpr(expr.size);
        popExpected();

        if (expr.obj != null) {
            for (Expr val : expr.obj) {
                ClassReference type = analyseExpr(val);
                if (!expr.compoundType.is(type)) {
                    errorIncompatibleTypes(val, expr.compoundType, type);
                }
            }
        }

        return expr.retType = expr.compoundType.array();
    }

    @Override
    public ClassReference visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (expr.target.get().hasField(fieldName)) {
            fieldType = expr.target.get().getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        OperationInfo operationInfo = getOperationInfo(expr.assignType, fieldType);

        expr.executor = operationInfo.executor;

        return expr.retType = operationInfo.result;
    }

    @Override
    public ClassReference visitSpecialSetExpr(Expr.SpecialSet expr) {
        ClassReference objType = analyseExpr(expr.callee);

        String fieldName = expr.name.lexeme();
        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (objType.get().hasField(fieldName)) {
            fieldType = objType.get().getFieldType(expr.name.lexeme());
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        OperationInfo info = getOperationInfo(expr.assignType, fieldType);

        expr.retType = fieldType;

        return fieldType;
    }

    @Override
    public ClassReference visitArraySetExpr(Expr.ArraySet expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(locFinder.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        ClassReference indexType = analyseExpr(expr.index);
        if (!indexType.is(VarTypeManager.INTEGER)) {
            errorF(locFinder.find(expr.index), "integer expected; found '%s'", indexType.absoluteName());
        }

        ClassReference valType = analyseExpr(expr.value);
        OperationInfo info = getOperationInfo(component.reference(), expr.assignType, valType);

        expr.executor = info.executor;

        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        BytecodeVars.FetchResult result = checkVarExistence(expr.name, true, false);
        if (result != BytecodeVars.FetchResult.FAIL) {
            OperationInfo info = getOperationInfo(expr.assignType, result.type());
            expr.executor = info.executor;
        }
        return expr.retType = result.type();
    }

    @Override
    public ClassReference visitConstructorExpr(Expr.Constructor expr) {
        return null;
    }

    @Override
    public ClassReference visitStaticSetExpr(Expr.StaticSet expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (expr.target.get().hasField(fieldName)) {
            fieldType = expr.target.get().getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        ClassReference valType = analyseExpr(expr.value);

        OperationInfo info = getOperationInfo(fieldType, expr.assignType, valType);

        expr.executor = info.executor;

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitGroupingExpr(Expr.Grouping expr) {
        //no need to analyse this, it's no longer used
        return null;
    }

    @Override
    public ClassReference visitUnaryExpr(Expr.Unary expr) {
        ClassReference objType = analyseExpr(expr.right);

        OperationInfo info = getOperationInfo(expr.operator, objType);

        expr.executor = info.executor;

        return expr.retType = info.result;
    }

    @Override
    public ClassReference visitWhenExpr(Expr.When expr) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(expr.condition);
        popExpected();

        ClassReference ifTrueType = analyseExpr(expr.ifTrue);
        ClassReference ifFalseClass = analyseExpr(expr.ifFalse);
        return null;
    }

    @Override
    public ClassReference visitStaticGetExpr(Expr.StaticGet expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (expr.target.get().hasField(fieldName)) {
            fieldType = expr.target.get().getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitSwitchExpr(Expr.Switch expr) {
        pushExpected(VarTypeManager.ENUM, VarTypeManager.STRING, VarTypeManager.INTEGER.reference(), VarTypeManager.DOUBLE.reference(), VarTypeManager.FLOAT.reference(), VarTypeManager.CHAR.reference());
        return null;
    }

    @Override
    public ClassReference visitSliceExpr(Expr.Slice expr) {
        ClassReference objType = analyseExpr(expr.object);

        if (!objType.get().isArray()) {
            errorF(locFinder.find(expr), "Array type expected; found '%s'", objType.absoluteName());
        }

        if (expr.start != null) {
            ClassReference reference = analyseExpr(expr.start);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(locFinder.find(expr.start), "integer expected; found '%s'" + reference.absoluteName());
            }
        }
        if (expr.end != null) {
            ClassReference reference = analyseExpr(expr.end);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(locFinder.find(expr.end), "integer expected; found '%s'" + reference.absoluteName());
            }
        }
        if (expr.interval != null) {
            ClassReference reference = analyseExpr(expr.interval);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(locFinder.find(expr.interval), "integer expected; found '%s'" + reference.absoluteName());
            }
        }

        return objType;
    }

    @Override
    public ClassReference visitGetExpr(Expr.Get expr) {
        ClassReference objType = analyseExpr(expr.object);

        String fieldName = expr.name.lexeme();

        if (objType.get().isArray() && "values".equals(fieldName)) {
            return expr.retType = VarTypeManager.INTEGER.reference();
        }

        ClassReference fieldType = VarTypeManager.VOID.reference();
        if (objType.get().hasField(fieldName)) {
            fieldType = objType.get().getFieldType(fieldName);
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitAssignExpr(Expr.Assign expr) {
        BytecodeVars.FetchResult result = checkVarExistence(expr.name, expr.type.type() != TokenType.ASSIGN, false);
        if (result != BytecodeVars.FetchResult.FAIL) {
            ClassReference type = analyseExpr(expr.value);
            if (type.is(result.type())) {
                OperationInfo operationInfo;
                if (expr.type.type() == TokenType.ASSIGN) {
                    varAnalyser.setHasValue(result.ordinal());
                    operationInfo = OperationInfo.UNKNOWN;
                } else
                    operationInfo = getOperationInfo(varAnalyser.getType(expr.name.lexeme()), expr.type, expr.value);
                expr.executor = operationInfo.executor;
            }
        }
        return expr.retType = result.type();
    }

    @Override
    public ClassReference visitStaticCallExpr(Expr.StaticCall expr) {
        return null;
    }

    @Override
    public ClassReference visitBinaryExpr(Expr.Binary expr) {

        ClassReference leftType = analyseExpr(expr.left);
        ClassReference rightType = analyseExpr(expr.right);

        OperationInfo info = getOperationInfo(leftType, expr.operator, rightType);

        expr.executor = info.executor;

        return expr.retType = info.result;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null) {
            if (!methodReturnType.is(VarTypeManager.VOID)) {
                error(stmt.keyword, "missing return value");
            }
        } else {
            if (methodReturnType.is(VarTypeManager.VOID)) {
                error(stmt.keyword, "cannot return value from method with void return type");
            } else {
                ClassReference retType = analyseExpr(stmt.value);
                if (!retType.is(methodReturnType)) {
                    errorIncompatibleTypes(stmt.value, methodReturnType, retType);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        pushScope();
        analyseStmt(stmt.body);
        popScope();

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        pushScope();
        analyseStmt(stmt.body);
        popScope();
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        ClassReference arrayType = stmt.type.array();
        pushExpected(arrayType);
        analyseExpr(stmt.initializer);
        popExpected();

        pushScope();
        //add 2 synthetic vars
        int baseVar = varAnalyser.add("?", arrayType, false, true); //array variable
        varAnalyser.add("?", VarTypeManager.INTEGER.reference(), false, true); //iteration variable

        varAnalyser.add(stmt.name.lexeme(), stmt.type, true, true); //named variable from sourcecode

        analyseStmt(stmt.body);
        popScope();

        stmt.baseVar = baseVar;

        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        Token[] localNames = stmt.localNames;
        byte[] localOrdinals = new byte[localNames.length];

        for (int i = 0; i < localNames.length; i++) {
            Token localName = localNames[i];
            BytecodeVars.FetchResult fetchResult = varAnalyser.get(localName.lexeme());
            if (fetchResult == BytecodeVars.FetchResult.FAIL) {
                errorF(localName, "unknown local variable '%s'", localName.lexeme());
            }
            localOrdinals[i] = fetchResult.ordinal();
        }
        stmt.localOrdinals = localOrdinals;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        analyseExpr(stmt.expression);

        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        pushExpected(VarTypeManager.THROWABLE);
        analyseExpr(stmt.value);
        popExpected();
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            analyseStmt(statement);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        pushScope();
        analyseStmt(stmt.body);
        popScope();

        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches) {
            pushScope();
            tryCreateVar(aCatch.getFirst().getSecond(), VarTypeManager.THROWABLE, true, false);
            analyseStmt(aCatch.getSecond());
            popScope();
        }

        if (stmt.finale != null) {
            pushScope();
            analyseStmt(stmt.finale);
            popScope();
        }

        return null;
    }

    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        stmt.amount = this.varAnalyser.pop();

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        pushScope();
        analyseStmt(stmt.thenBranch);
        popScope();
        for (ElifBranch branch : stmt.elifs) {
            pushExpected(VarTypeManager.BOOLEAN.reference());
            analyseExpr(branch.condition);
            popExpected();

            pushScope();
            analyseStmt(branch.body);
            popScope();
        }
        if (stmt.elseBranch != null) {
            pushScope();
            analyseStmt(stmt.elseBranch);
            popScope();
        }

        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return null;
    }
}
