package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.Chunk;

public class CodeAttributeInfo implements AttributeInfo {
    short maxStack;
    short maxLocals; //why is this a short?
    public Chunk chunk;

    public static CodeAttributeInfo create(Chunk chunk) {
        CodeAttributeInfo info = new CodeAttributeInfo();
        info.chunk = chunk;
        return info;
    }

    @Override
    public String name() {
        return "Code";
    }

    @Override
    public int length() {
        return 4 + chunk.code().length + chunk.handlers().length * 8;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
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

    public static AttributeInfo read(BytecodeReader reader) {
        CodeAttributeInfo info = new CodeAttributeInfo();
        info.maxStack = (short) reader.read2b();
        info.maxLocals = (short) reader.read2b();
        byte[] code = reader.readArray(reader.read2b());
        int exceptionHandlerCount = reader.read2b();
        Chunk.ExceptionHandler[] handlers = new Chunk.ExceptionHandler[exceptionHandlerCount];
        for (int i = 0; i < exceptionHandlerCount; i++) {
            handlers[i] = Chunk.ExceptionHandler.read(reader);
        }
        Chunk chunk = new Chunk(code, handlers)

        return new CodeAttributeInfo();
    }
}
