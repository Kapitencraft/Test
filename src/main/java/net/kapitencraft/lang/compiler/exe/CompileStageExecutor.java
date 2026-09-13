package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.exe.load.CompileSource;

public interface CompileStageExecutor<T extends CompileSource> {

    void process(T value);
}
