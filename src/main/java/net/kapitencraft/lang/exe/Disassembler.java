package net.kapitencraft.lang.exe;

import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.holder.bytecode.attributes.LocalVariableTableAttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.*;
import net.kapitencraft.lang.oop.clazz.generated.RuntimeClass;
import net.kapitencraft.lang.oop.method.RuntimeCallable;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Disassembler {

    @SuppressWarnings("StatementWithEmptyBody")
    public static void disassemble(RuntimeCallable methodOwner, RuntimeClass classOwner, String name) {
        System.out.printf("== %s ==\n", name);
        Chunk chunk = methodOwner.getChunk();
        if (chunk == null) {
            System.out.println("<abstract>");
            return;
        }

        byte[] code = chunk.code();
        for (int offset = 0; offset < code.length; offset = disassembleInstruction(code, offset, methodOwner, classOwner)) {
        }
    }

    private static int disassembleInstruction(byte[] code, int offset, RuntimeCallable methodOwner, RuntimeClass classOwner) {
        System.out.printf("%04d ", offset);

        Opcode opcode = Opcode.byId(code[offset] & 255);
        return switch (opcode) {
            case TRACE -> debugTrace(code, offset, classOwner, methodOwner);
            case POP, POP_2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
                 RETURN, RETURN_ARG, THROW,
                 I_NEGATION, I_ADD, I_SUB, I_MUL, I_DIV, I_POW, I_MOD,
                 D_NEGATION, D_ADD, D_SUB, D_MUL, D_DIV, D_POW, D_MOD,
                 F_NEGATION, F_ADD, F_SUB, F_MUL, F_DIV, F_POW, F_MOD,
                 ASSIGN_0, ASSIGN_1, ASSIGN_2,
                 NULL, TRUE, FALSE, NOT, CONCENTRATION,
                 I_M1, I_0, I_1, I_2, I_3, I_4, I_5,
                 D_M1, D_1,
                 F_M1, F_1,
                 I_SH_L, I_SH_R,
                 D2F, ARRAY_LENGTH,
                 EQUAL, NEQUAL,
                 I_LESSER, D_LESSER, F_LESSER,
                 I_GREATER, D_GREATER, F_GREATER,
                 I_LEQUAL, D_LEQUAL, F_LEQUAL,
                 I_GEQUAL, D_GEQUAL, F_GEQUAL,
                 IA_STORE, DA_STORE, CA_STORE, FA_STORE, RA_STORE,
                 IA_LOAD, DA_LOAD, CA_LOAD, FA_LOAD, RA_LOAD,
                 SLICE, CA_NEW, DA_NEW, FA_NEW, IA_NEW, RA_NEW -> simpleInstruction(opcode, offset);
            case GET, ASSIGN -> var(opcode, code, offset, methodOwner);
            case GET_0 -> defVar(opcode, offset, 0, methodOwner);
            case GET_1 -> defVar(opcode, offset, 1, methodOwner);
            case GET_2 -> defVar(opcode, offset, 2, methodOwner);
            case I_CONST -> intConstInstruction(opcode, code, offset, classOwner);
            case D_CONST -> doubleConstInstruction(opcode, code, offset, classOwner);
            case F_CONST -> floatConstInstruction(opcode, code, offset, classOwner);
            case S_CONST -> stringConstInstruction(opcode, code, offset, classOwner);
            case NEW, INSTANCEOF -> classReferenceInstruction(opcode, code, offset, classOwner);
            case INVOKE_STATIC, INVOKE_VIRTUAL -> invoke(opcode, code, offset, classOwner);
            case JUMP, JUMP_IF_FALSE -> jump(opcode, code, offset);
            case SWITCH -> switchInstruction(opcode, code, offset);
            case GET_FIELD, PUT_FIELD -> fieldOp(opcode, code, offset, classOwner);
            case GET_STATIC, PUT_STATIC -> staticFieldOp(opcode, code, offset, classOwner);
            case IIRC -> intIncrement(opcode, code, offset);
            //case RA_NEW -> newArray(opcode, chunk, offset);
        };
    }

    private static int classReferenceInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass classOwner) {
        int idx = read2b(code, offset + 1);
        ConstantClassInfo info = classOwner.getConstant(idx);
        System.out.printf("%-16s type=%s\n", opcode, info.getValue());
        return offset + 3;
    }

    private static int intIncrement(Opcode opcode, byte[] code, int offset) {
        int ordinal = code[offset + 1];
        int val = code[offset + 2];
        System.out.printf("%-16s idx=%3d, val=%3d\n", opcode, ordinal, val);
        return offset + 3;
    }

    private static int switchInstruction(Opcode opcode, byte[] code, int offset) {
        int defaulted = read2b(code, offset + 1);
        int size = read2b(code, offset + 3);
        System.out.printf("%-16s size=%4d, default=%4d\n", opcode, size, defaulted);
        for (int i = 0; i < size; i++) {
            System.out.printf("\t%4d -> %4d\n",
                    read4b(code, offset + 5 + i * 6),
                    read2b(code, offset + 9 + i * 6)
            );
        }
        return offset + 5 + size * 6;
    }

    private static int debugTrace(byte[] code, int offset, RuntimeClass classOwner, RuntimeCallable methodOwner) {
        int pos = read2b(code, offset + 1);
        byte[] data = classOwner.<ConstantTraceLocalsInfo>getConstant(pos).data();
        List<String> values = new ArrayList<>();
        for (byte b : data) {
            Pair<String, String> local = local(methodOwner, offset, b);
            values.add(local.getFirst() + ": " + local.getSecond());
        }
        String log = values.stream().collect(Collectors.joining(", ", "[", "]"));
        System.out.printf("%-16s %s\n", "DEBUG_TRACE", log);
        return offset + 3;
    }

    private static Pair<String, String> local(RuntimeCallable owner, int pc, int i) {
        return owner.<LocalVariableTableAttributeInfo>getSingleAttribute("LocalVariableTable").table().get(pc, i);
    }

    private static int newArray(Opcode opcode, Chunk chunk, int offset, RuntimeClass owner) {
        int pos = read2b(chunk.code(), offset + 1);
        String string = Disassembler.constString(owner, pos);
        System.out.printf("%-16s %s\n", opcode, string);
        return offset + 1;
    }

    private static int jump(Opcode opcode, byte[] code, int offset) {
        int jump = read2b(code, offset + 1);
        System.out.printf("%-16s %4d -> %d\n", opcode, offset, jump);
        return offset + 3;
    }

    private static int var(Opcode opcode, byte[] code, int offset, RuntimeCallable owner) {
        int ordinal = code[offset + 1];
        defVar(opcode, offset, ordinal, owner);
        return offset + 2;
    }

    private static int defVar(Opcode opcode, int offset, int ordinal, RuntimeCallable owner) {
        Pair<String, String> pair = local(owner, offset, ordinal);
        System.out.printf("%-16s %4d: \"%s\" -> %s\n", opcode, ordinal, pair.getFirst(), pair.getSecond());
        return offset + 1;
    }

    private static int invoke(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        int id = read2b(code, offset + 1);
        ConstantObjRefInfo constant = owner.getConstant(id);
        String signature = constant.nameAndType.descriptor().value();
        System.out.printf("%-16s %4d:  %s#%s\n", opcode, id, constant.clazz.getValue(), signature);
        return offset + 3;
    }

    private static int fieldOp(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        int id = read2b(code, offset + 1);
        ConstantObjRefInfo constant = owner.getConstant(id);
        System.out.printf("%-16s %4d '%s'\n", opcode, id, constant.nameAndType.name().value());
        return offset + 3;
    }

    private static int staticFieldOp(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        int id = read2b(code, offset + 1);
        ConstantObjRefInfo constant = owner.getConstant(id);
        System.out.printf("%-16s %4d '%s': '%s'\n", opcode, id, constant.clazz.getValue(), constant.nameAndType.name().value());
        return offset + 3;
    }

    private static int stringConstInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        return constInstruction(opcode, code, offset, owner, Disassembler::constString);
    }

    private static int doubleConstInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        return constInstruction(opcode, code, offset, owner, Disassembler::constDouble);
    }

    private static int floatConstInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        return constInstruction(opcode, code, offset, owner, Disassembler::constFloat);
    }

    private static int intConstInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass owner) {
        return constInstruction(opcode, code, offset, owner, Disassembler::constInt);
    }

    private static int constInstruction(Opcode opcode, byte[] code, int offset, RuntimeClass owner, BiFunction<RuntimeClass, Integer, Object> getter) {
        int pos = read2b(code, offset + 1);
        System.out.printf("%-16s %4d '%s'\n", opcode.name(), pos, getter.apply(owner, pos));
        return offset + 3;
    }

    private static int simpleInstruction(Opcode opcode, int offset) {
        System.out.println(opcode.name());
        return offset + 1;
    }

    private static int read2b(byte[] code, int index) {
        return ((code[index++] & 255) << 8) | (code[index] & 255);
    }

    private static int read4b(byte[] code, int index) {
        return ((((code[index++] & 255) << 8) | (code[index++] & 255) << 8) | (code[index++] & 255) << 8) | (code[index] & 255);
    }

    //region constants
    public static String constString(RuntimeClass owner, int index) {
        return owner.<ConstantStringInfo>getConstant(index).string.value();
    }

    @Contract(pure = true)
    public static double constDouble(RuntimeClass owner, int index) {
        return owner.<ConstantDoubleInfo>getConstant(index).value();
    }

    public static int constInt(RuntimeClass owner, int index) {
        return owner.<ConstantIntegerInfo>getConstant(index).value();
    }

    public static float constFloat(RuntimeClass owner, int index) {
        return owner.<ConstantFloatInfo>getConstant(index).value();
    }
    //endregion
}
