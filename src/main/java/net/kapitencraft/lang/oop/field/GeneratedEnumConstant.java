package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public class GeneratedEnumConstant extends LoxField {

    private final LoxClass target;
    private final int ordinal;
    private final String name;
    private final int constructorOrdinal;
    private final List<Expr> args;

    public GeneratedEnumConstant(LoxClass target, int ordinal, String name, int constructorOrdinal, List<Expr> args) {
        this.target = target;
        this.ordinal = ordinal;
        this.name = name;
        this.constructorOrdinal = constructorOrdinal;
        this.args = args;
    }


    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        List<Object> args = interpreter.visitArgs(this.args);
        ClassInstance constant = ((ClassInstance) environment.getThis());
        constant.assignField("ordinal", ordinal);
        constant.assignField("name", name);
        return target.getConstructor().getMethodByOrdinal(constructorOrdinal).call(environment, interpreter, args);
    }

    @Override
    public LoxClass getType() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }
}
