package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;

public class StaticFieldAccessInstruction extends CodeInstruction {
    private final String className, fieldName;

    public StaticFieldAccessInstruction(Opcode opcode, String className, String fieldName) {
        super(opcode);
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(className);
        builder.injectString(fieldName);
    }

    @Override
    public int length() {
        return 5;
    }
}
