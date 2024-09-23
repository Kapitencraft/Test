package net.kapitencraft.lang.func;

import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.FuncDecl declaration;

    public LoxFunction(Stmt.FuncDecl declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.defineVar(declaration.params.get(i).right().lexeme, arguments.get(i));
        }

        try {
            interpreter.interpret(declaration.body, environment);
        } catch (CancelBlock returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public LoxClass type() {
        return declaration.retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return declaration.params.stream().map(Pair::left).toList();
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<declared fn#" + declaration.name.lexeme + ">";
    }
}