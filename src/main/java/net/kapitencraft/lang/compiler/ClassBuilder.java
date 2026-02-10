package net.kapitencraft.lang.compiler;

import net.kapitencraft.tool.ByteBuilder;

public class ClassBuilder extends ByteBuilder {
    private final ConstantPoolBuilder cp;
    private final Synthesizer synthesizer;

    public ClassBuilder() {
        super(128);
        this.cp = new ConstantPoolBuilder();
        this.synthesizer = new Synthesizer(cp);
    }

    public ConstantPoolBuilder getCp() {
        return cp;
    }
}
