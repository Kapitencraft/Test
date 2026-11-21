package net.kapitencraft.lang.exe.lambda;

import net.kapitencraft.lang.func.ScriptedCallable;

public record ScriptedMethodHandle(ScriptedCallable exe, String signature, int kind) {
}
