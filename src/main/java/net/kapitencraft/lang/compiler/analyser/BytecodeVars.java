package net.kapitencraft.lang.compiler.analyser;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BytecodeVars {
    private final Local[] locals = new Local[256];
    private byte localCount, scopeDepth;

    public FetchResult get(String name) {
        for (int i = localCount - 1; i >= 0; i--) {
            Local local = locals[i];
            if (Objects.equals(local.name, name)) return new FetchResult((byte) i, local.canAssign, local.assigned, local.type);
        }
        return FetchResult.FAIL;
    }

    public byte add(String name, ClassReference type, boolean canAssign, boolean assigned) {
        if (get(name) == FetchResult.FAIL) {
            locals[localCount] = new Local(name, scopeDepth, type, canAssign, assigned);
            return localCount++;
        }
        return -1;
    }

    public void push() {
        scopeDepth++;
    }

    public void pop() {
        int i = localCount - 1;
        while (i >= 0 && locals[i].depth >= scopeDepth) {
            localCount--; i--;
        }
        scopeDepth--;
    }

    public ClassReference getType(String lexeme) {
        FetchResult result = get(lexeme);
        if (result == FetchResult.FAIL) return VarTypeManager.VOID.reference();
        return result.type;
    }

    public void setHasValue(byte ordinal) {
        locals[ordinal & 255].assigned = true;
    }

    private static final class Local {
        private final String name;
        private final int depth;
        private final ClassReference type;
        private final boolean canAssign;
        private boolean assigned;

        private Local(String name, int depth, ClassReference type, boolean canAssign, boolean assigned) {
            this.name = name;
            this.depth = depth;
            this.type = type;
            this.canAssign = canAssign;
            this.assigned = assigned;
        }

        public String name() {
            return name;
        }

        public int depth() {
            return depth;
        }

        public ClassReference type() {
            return type;
        }

        public boolean canAssign() {
            return canAssign;
        }
    }

    public record FetchResult(byte ordinal, boolean canAssign, boolean assigned, ClassReference type) {
        public static final FetchResult FAIL = new FetchResult((byte) -1, false, true, VarTypeManager.VOID.reference());
    }
}
