package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class CodeAttributeInfo implements AttributeInfo {
    short maxStack;
    short maxLocals; //why is this a short?
    Chunk chunk;

    @Override
    public String name() {
        return "Code";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeShort(maxStack);
        buffer.writeShort(maxLocals);
        byte[] code = chunk.code();
        buffer.writeInt(code.length);
        buffer.writeArray(code);
        Chunk.ExceptionHandler[] handlers = chunk.handlers();
        buffer.writeShort(handlers.length);
        for (Chunk.ExceptionHandler handler : handlers) {
            handler.toStream(buffer);
        }
    }
}
