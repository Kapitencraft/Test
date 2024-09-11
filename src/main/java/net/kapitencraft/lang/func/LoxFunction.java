package net.kapitencraft.lang.func;

import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.ast.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        Environment environment = new Environment();
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.defineVar(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body);
        } catch (CancelBlock returnValue) {
            return returnValue.value;
        }
        return null;
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