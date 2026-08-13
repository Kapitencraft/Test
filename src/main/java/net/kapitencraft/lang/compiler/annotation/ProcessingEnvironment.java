package net.kapitencraft.lang.compiler.annotation;

import net.kapitencraft.lang.compiler.Compiler;

public class ProcessingEnvironment {
    private final Compiler.ErrorStorage error;

    public ProcessingEnvironment(Compiler.ErrorStorage error) {
        this.error = error;
    }

    public void error(String msg) {

    }
}
