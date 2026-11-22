package net.kapitencraft.scripted.lang.exe.lambda;

import net.kapitencraft.scripted.lang.func.ScriptedCallable;

public record ScriptedMethodHandle(ScriptedCallable exe, String signature, int kind) {
}
