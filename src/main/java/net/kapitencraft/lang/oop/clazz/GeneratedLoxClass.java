package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.compile.parser.SkeletonParser;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.LoxFunction;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.oop.GeneratedField;
import net.kapitencraft.lang.oop.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.*;
import java.util.stream.Collectors;

public record GeneratedLoxClass(List<Stmt.FuncDecl> methods, List<Stmt.FuncDecl> staticMethods,
                                List<Stmt.VarDecl> fields, List<Stmt.VarDecl> staticFields,
                                LoxClass superclass, String name, List<GeneratedLoxClass> enclosing) implements LoxClass {

    private static Map<String, LoxCallable> getMethods(List<Stmt.FuncDecl> methods) {
        return methods.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, LoxFunction::new));
    }

    private static Map<String, LoxField> getFields(List<Stmt.VarDecl> fields) {
        return fields.stream().collect(Collectors.toMap(dec -> dec.name.lexeme, GeneratedField::new));
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(LoxField::getType).orElse(LoxClass.super.getFieldType(name));
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return getFields(this.staticFields).get(name).getType();
    }

    @Override
    public boolean hasField(String name) {
        return getFields().containsKey(name) || LoxClass.super.hasField(name);
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return getMethods(this.staticMethods).get(name).type();
    }

    @Override
    public LoxClass getMethodType(String name) {
        return (getMethods(this.methods).get(name)).type();
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return getMethods(this.staticMethods).get(name);
    }

    @Override
    public LoxCallable getMethod(String name) {
        return Objects.requireNonNullElse(getMethods(this.methods).get(name), LoxClass.super.getMethod(name));
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return getMethods(this.staticMethods).containsKey(name);
    }

    @Override
    public boolean hasMethod(String name) {
        return getMethods(this.methods).containsKey(name) || LoxClass.super.hasMethod(name);
    }

    @Override
    public Map<String, LoxField> getFields() {
        Map<String, LoxField> parentFields = new HashMap<>(superclass().getFields());
        parentFields.putAll(getFields(this.fields)); //replace parent fields
        return Map.copyOf(parentFields);
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {

    }
}
