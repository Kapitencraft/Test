package net.kapitencraft.lang.compiler.exe.pipeline;

import net.kapitencraft.lang.compiler.exe.CompileEnvironment;
import net.kapitencraft.lang.compiler.exe.FileInfo;

import java.util.concurrent.CompletableFuture;

public class PythonCompilePipeline extends CompilePipeline {
    protected PythonCompilePipeline() {
        super("pscr");
    }

    @Override
    public CompletableFuture<?> createProcessor(CompletableFuture<FileInfo> fileSource, CompileEnvironment environment) {
        return null;
    }
}
