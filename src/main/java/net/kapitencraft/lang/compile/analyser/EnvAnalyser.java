package net.kapitencraft.lang.compile.analyser;

public class EnvAnalyser {
    private final MethodAnalyser methodAnalyser;
    private final VarAnalyser varAnalyser;
    private int loopIndex = 0;

    public EnvAnalyser() {
        this.methodAnalyser = new MethodAnalyser();
        this.varAnalyser = new VarAnalyser();
    }

    public void push() {
        methodAnalyser.push();
        varAnalyser.push();
    }

    public void pop() {
        methodAnalyser.pop();
        varAnalyser.pop();
    }

    public void pushLoop() {
        loopIndex++;
    }

    public void popLoop() {
        loopIndex--;
    }

    public boolean inLoop() {
        return loopIndex > 0;
    }

    public boolean hasVar(String name) {
        return varAnalyser.has(name);
    }

    public boolean hasVarValue(String name) {
        return varAnalyser.has(name) && varAnalyser.hasValue(name);
    }

    public void setHasVarValue(String name) {
        varAnalyser.setHasValue(name);
    }

    public boolean addVar(String name, String type, boolean value) {
        return varAnalyser.add(name, type, value);
    }

    public boolean hasMethod(String name) {
        return methodAnalyser.has(name);
    }

    public boolean addMethod(String name, Class<?> retType) {
        return methodAnalyser.add(name, retType);
    }

    public Class<?> getVarType(String name) {
        return varAnalyser.getType(name);
    }

    public Class<?> getMethodType(String lexeme) {
        return methodAnalyser.type(lexeme);
    }
}
