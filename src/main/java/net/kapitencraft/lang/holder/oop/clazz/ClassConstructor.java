package net.kapitencraft.lang.holder.oop.clazz;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.lang.compiler.java.parser.JavaStmtParser;
import net.kapitencraft.lang.compiler.VarTypeContainer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.oop.attribute.FieldHolder;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface ClassConstructor extends Validatable {

    AnnotationObj[] annotations();

    ClassReference target();

    Compiler.ClassBuilder construct(StmtParser javaStmtParser, VarTypeContainer parser, ErrorStorage logger);

    ScriptedClass createSkeleton(ErrorStorage logger);

    Token name();

    String pck();

    default @NotNull Expr getFieldBody(StmtParser javaStmtParser, VarTypeContainer parser, FieldHolder fieldHolder, List<Stmt> statics) {
        javaStmtParser.apply(fieldHolder.body(), parser);
        Expr initializer = javaStmtParser.expression();
        if (Modifiers.isStatic(fieldHolder.modifiers())) {
            Stmt.Expression stmt1 = new Stmt.Expression();
            {
                Expr.StaticSet staticSet = new Expr.StaticSet();
                staticSet.target = target();
                staticSet.name = fieldHolder.name();
                staticSet.value = initializer;
                staticSet.assignType = fieldHolder.assign();
                staticSet.executor = target();
                stmt1.expression = staticSet;
            }
            statics.add(stmt1);
        }
        return initializer;
    }

    default void checkFinalsPopulated(List<Stmt> body, List<String> finalFields) {

    }

    default Annotation[] parseAnnotations(StmtParser stmtParser, VarTypeContainer parser) {
        List<Annotation> annotations = new ArrayList<>();
        for (AnnotationObj obj : this.annotations()) {
            annotations.add(stmtParser.parseAnnotation(obj, parser));
        }
        return annotations.toArray(Annotation[]::new);
    }

    static void addClinit(List<Stmt> statics, List<Pair<Token, CompileCallable>> methods) {
        Stmt.Return aReturn = new Stmt.Return();
        aReturn.keyword = Token.createNative("return");
        statics.add(aReturn);
        methods.add(Pair.of( //add <clinit> method
                Token.createNative("<clinit>"),
                new CompileCallable(
                        VarTypeManager.VOID.reference(),
                        List.of(),
                        new ClassReference[0],
                        statics,
                        Modifiers.pack(true, true, false),
                        new Annotation[0]
                )
        ));
    }
}
