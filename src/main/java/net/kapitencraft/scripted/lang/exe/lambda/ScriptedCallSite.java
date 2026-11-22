package net.kapitencraft.scripted.lang.exe.lambda;

import net.kapitencraft.scripted.lang.oop.clazz.generated.RuntimeClass;

public record ScriptedCallSite(ScriptedMethodHandle handle, RuntimeClass type, Object[] args) {
}
