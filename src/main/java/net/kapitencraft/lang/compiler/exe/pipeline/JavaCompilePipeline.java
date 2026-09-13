package net.kapitencraft.lang.compiler.exe.pipeline;

import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.lang.compiler.exe.JavaCompileSource;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class JavaCompilePipeline implements CompilePipeline<JavaCompileSource> {
    public static final JavaCompilePipeline INSTANCE = new JavaCompilePipeline();

    Map<CompileStage, CompileStageExecutor<JavaCompileSource>> STAGES = new EnumMap<>(Map.of(
            CompileStage.PARSE_SOURCE, JavaCompileSource::parseSource,
            CompileStage.CREATE_SKELETON, JavaCompileSource::applySkeleton,
            CompileStage.VALIDATE, JavaCompileSource::validate,
            CompileStage.SYNTAX_ANALYSIS, JavaCompileSource::analyseSyntax,
            CompileStage.SEMANTIC_ANALYSIS, JavaCompileSource::analyseSemantics,
            CompileStage.FINALIZE_LOAD, JavaCompileSource::finalizeLoad,
            CompileStage.OPTIMIZE, JavaCompileSource::optimize,
            CompileStage.CACHING, JavaCompileSource::cache
    ));

    @Override
    public CompileStageExecutor<JavaCompileSource> getExecutor(CompileStage stage) {
        return STAGES.get(stage);
    }

    @Override
    public JavaCompileSource createSource(File source) {
        return new JavaCompileSource(source);
    }

    @Override
    public String getFileExtension() {
        return "scr";
    }
}
