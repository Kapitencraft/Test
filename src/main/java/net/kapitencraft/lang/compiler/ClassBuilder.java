package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.ConstantPoolBuilder;
import net.kapitencraft.tool.ByteBuilder;

public class ClassBuilder extends ByteBuilder {
    private final BytecodeBuilder cp;
    private final Synthesizer synthesizer;

    public ClassBuilder() {
        super(128);
        this.cp = new BytecodeBuilder();
        this.synthesizer = new Synthesizer(cp);
    }

    public BytecodeBuilder getCp() {
        return cp;
    }
}
