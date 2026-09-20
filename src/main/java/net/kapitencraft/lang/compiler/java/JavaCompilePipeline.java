package net.kapitencraft.lang.compiler.java;

import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.lang.compiler.exe.text.HolderParser;
import net.kapitencraft.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.lang.compiler.exe.text.TextBasedCompilePipeline;
import net.kapitencraft.lang.compiler.java.parser.JavaHolderParser;
import net.kapitencraft.lang.compiler.java.parser.JavaStmtParser;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class JavaCompilePipeline extends TextBasedCompilePipeline {
    public static final JavaCompilePipeline INSTANCE = new JavaCompilePipeline();

    @Override
    protected Lexer createLexer(String content, ErrorStorage storage) {
        return new JavaLexer(content, storage);
    }

    @Override
    protected HolderParser createHolderParser(ErrorStorage storage, SourceTree tree, CompileSource source) {
        return new JavaHolderParser(storage, tree, source);
    }

    @Override
    protected StmtParser createStmtParser(ErrorStorage storage, SourceTree sourceTree, CompileSource source) {
        return new JavaStmtParser(storage, sourceTree, source);
    }

    @Override
    public String getFileExtension() {
        return "scr";
    }
}
