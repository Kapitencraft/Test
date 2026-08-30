package net.kapitencraft.lang.compiler.exe;

public enum CompilerStage {
    PARSE_SOURCE,
    CREATE_SKELETON,
    VALIDATE,
    SYNTAX_ANALYSIS,
    SEMANTIC_ANALYSIS,
    GENERATE,
    OPTIMIZE,
    CACHING
}
