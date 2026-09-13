package net.kapitencraft.lang.compiler.exe.pipeline;

import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.lang.exe.load.CompileSource;

import java.io.File;

public interface CompilePipeline<T extends CompileSource> {

    CompileStageExecutor<T> getExecutor(CompileStage stage);

    T createSource(File source);

    String getFileExtension();
}
