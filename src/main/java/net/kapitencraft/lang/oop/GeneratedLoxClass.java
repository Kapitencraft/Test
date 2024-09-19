package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.LoxFunction;
import net.kapitencraft.lang.holder.ast.Stmt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeneratedLoxClass extends LoxClass {

    public GeneratedLoxClass(Stmt.Class stmt) {
        super(
                stmt.methods.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, LoxFunction::new)),
                List.of(),
                stmt.fields.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, GeneratedField::new)),
                stmt.superClass,
                stmt.name.lexeme
        );
    }
}
