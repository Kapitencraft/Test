package net.kapitencraft.scripted.lang.exe.lambda;

import net.kapitencraft.scripted.lang.oop.clazz.generated.RuntimeClass;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record ScriptedCallSite(ScriptedMethodHandle handle, RuntimeClass type, Object[] args) {

    @Override
    public @NotNull String toString() {
        return "ScriptedCallSite{" + handle.signature() + ", " + Arrays.deepToString(args) + "}";
    }
}
