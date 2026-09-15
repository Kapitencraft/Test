package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.lang.compiler.exe.source.SourceTree;

public interface CompileStageExecutor<T extends CompileSource> {

    void process(T value, SourceTree source);
}
