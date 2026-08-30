package net.kapitencraft.lang.compiler.exe.pipeline;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompileEnvironment;
import net.kapitencraft.lang.compiler.exe.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class CompilePipeline {
    private final String fileExtension;

    protected CompilePipeline(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public abstract CompletableFuture<?> createProcessor(CompletableFuture<FileInfo> fileSource, CompileEnvironment environment, Executor executor);

    public FileInfo createInfo(File file) {
        String fileName = file.getName().replace("." + fileExtension, "");
        String content;
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ErrorStorage storage = new ErrorStorage(
                content.split("\n", Integer.MAX_VALUE), //second param required to not skip empty lines
                file.getAbsolutePath().replace(".\\", "") //remove '\.\'
        );

        String rootPath = Compiler.source.getAbsolutePath();
        String path = file.getParentFile().getAbsolutePath().substring(rootPath.length() + 1).replace("." + fileExtension, "");
        String pck = path.replace('\\', '.');
        return new FileInfo(content, fileName, pck, storage);
    }

    public String getExtension() {
        return fileExtension;
    }
}
