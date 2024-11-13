package net.kapitencraft.lang.holder.decl;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedEnum;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public record EnumDecl(VarTypeParser parser, Compiler.ErrorLogger logger, PreviewClass target, Token name, String pck, LoxClass[] interfaces, SkeletonParser.EnumConstDecl[] constants, SkeletonParser.MethodDecl[] constructors, SkeletonParser.MethodDecl[] methods, SkeletonParser.FieldDecl[] fields, SkeletonParser.ClassConstructor<?>[] enclosed) implements SkeletonParser.ClassConstructor<BakedEnum> {

    @Override
    public BakedEnum construct(StmtParser stmtParser, ExprParser exprParser) {
        List<Stmt.VarDecl> fields = new ArrayList<>();
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), parser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            if (field.isStatic()) staticFields.add(fieldDecl);
            else fields.add(fieldDecl);
        }

        List<Compiler.ClassBuilder> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassConstructor<?> classDecl : enclosed()) {
            enclosed.add(classDecl.construct(stmtParser, exprParser));
        }

        List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
        List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : this.methods()) {
            List<Stmt> body = null;
            if (!method.isAbstract()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, method.type());
                body = stmtParser.parse();
                stmtParser.popMethod();
            }
            GeneratedCallable methodDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(Pair.of(method.name(), methodDecl));
            else methods.add(Pair.of(method.name(), methodDecl));
        }

        List<Pair<Token, GeneratedCallable>> constructors = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : this.constructors()) {
            stmtParser.apply(method.body(), parser);
            stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, VarTypeManager.VOID);
            List<Stmt> body = stmtParser.parse();
            GeneratedCallable constDecl = new GeneratedCallable(method.type(), method.params(), body, method.isFinal(), method.isAbstract());
            stmtParser.popMethod();
            constructors.add(Pair.of(method.name(), constDecl));
        }

        return new BakedEnum(
                logger(),
                target(),
                constructors.toArray(Pair[]::new),
                methods.toArray(Pair[]::new),
                staticMethods.toArray(Pair[]::new),
                fields.toArray(Stmt.VarDecl[]::new),
                staticFields.toArray(Stmt.VarDecl[]::new),
                interfaces(),
                name(),
                pck(),
                enclosed.toArray(new Compiler.ClassBuilder[0])
        );
    }

    @Override
    public LoxClass createSkeleton() {
        return null;
    }
}