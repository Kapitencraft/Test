package net.kapitencraft.lang.compiler.exe.text;

import net.kapitencraft.lang.compiler.VarTypeContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;

import java.util.List;

public interface StmtParser {

    void pushFallback(ClassReference classReference);
    void popFallback();

    Expr expression();

    Annotation[] parseAnnotations(AnnotationObj[] annotations, VarTypeContainer parser);

    void apply(Token[] body, VarTypeContainer parser);

    void applyStaticMethod(ClassReference retType, Generics generics);

    void applyMethod(ClassReference retType, Generics generics);

    void applyOperation(OperationHolder holder, VarTypeContainer parser);

    List<Stmt> parse();

    void popMethod(Token token);

    Expr[] args();

    Annotation parseAnnotation(AnnotationObj obj, VarTypeContainer parser);

    Expr literalOrReference();
}
