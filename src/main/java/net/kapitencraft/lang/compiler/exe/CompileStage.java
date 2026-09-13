package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.exe.load.CompileSource;

import java.util.function.Consumer;

public enum CompileStage {
    PARSE_SOURCE(CompileSource::parseSource),
    CREATE_SKELETON(CompileSource::applySkeleton),
    VALIDATE(CompileSource::validate),
    SYNTAX_ANALYSIS(CompileSource::construct),
    SEMANTIC_ANALYSIS(CompileSource::analyse),
    FINALIZE_LOAD(CompileSource::finalizeLoad),
    OPTIMIZE(CompileSource::optimize),
    CACHING(CompileSource::cache);

    private final Consumer<CompileSource> action;

    CompileStage(Consumer<CompileSource> action) {
        this.action = action;
    }
}