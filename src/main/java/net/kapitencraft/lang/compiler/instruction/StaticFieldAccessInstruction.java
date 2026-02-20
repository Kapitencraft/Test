package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class StaticFieldAccessInstruction extends SimpleInstruction {
    private final String className, fieldName;

    public StaticFieldAccessInstruction(Opcode opcode, String className, String fieldName) {
        super(opcode);
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        super.save(builder, instStartIndexes);
        builder.injectString(className);
        builder.injectString(fieldName);
    }

    @Override
    public int length() {
        return 5;
    }
}
