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
                getMethods(stmt.methods),
                getMethods(stmt.staticMethods),
                null,
                List.of(),
                getFields(stmt.fields),
                getFields(stmt.staticFields),
                stmt.superclass,
                stmt.name.lexeme
        );
    }

    private static Map<String, LoxCallable> getMethods(List<Stmt.FuncDecl> methods) {
        return methods.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, LoxFunction::new));
    }

    private static Map<String, LoxField> getFields(List<Stmt.VarDecl> fields) {
        return fields.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, GeneratedField::new));
    }
}
