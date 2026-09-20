package net.kapitencraft.lang.compiler.exe.text;

import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.compiler.VarTypeContainer;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.holder.token.Token;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class TextBasedCompilePipeline implements CompilePipeline<TextBasedCompilePipeline.Source> {

    private static final Map<CompileStage, CompileStageExecutor<Source>> STAGES = new EnumMap<>(Map.of(
            CompileStage.PARSE_SOURCE, Source::parseSource,
            CompileStage.CREATE_SKELETON, Source::applySkeleton,
            CompileStage.VALIDATE, Source::validate,
            CompileStage.SYNTAX_ANALYSIS, Source::analyseSyntax,
            CompileStage.SEMANTIC_ANALYSIS, Source::analyseSemantics,
            CompileStage.FINALIZE_LOAD, Source::finalizeLoad,
            CompileStage.OPTIMIZE, Source::optimize,
            CompileStage.CACHING, Source::cache
    ));

    @Override
    public Source createSource(File source, String name, String pck, SourceTree.DirectoryNode owner) {
        return new Source(name, pck, source, owner);
    }

    public CompileSource createSource(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
        return new Source(name, pck, holder, storage, parser, declaring);
    }

    @Override
    public CompileStageExecutor<Source> getExecutor(CompileStage stage) {
        return STAGES.get(stage);
    }

    protected abstract Lexer createLexer(String content, ErrorStorage storage);

    protected abstract HolderParser createHolderParser(ErrorStorage storage, SourceTree tree, CompileSource source);

    protected abstract StmtParser createStmtParser(ErrorStorage storage, SourceTree sourceTree, CompileSource source);

    public class Source extends CompileSource {

        private Source(String name, String pck, File file, SourceTree.DirectoryNode declaring) {
            super(name, pck, file, declaring);
        }

        private Source(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
            super(name, pck, holder, storage, parser, declaring);
        }

        @Override
        public CompilePipeline<?> getPipeline() {
            return TextBasedCompilePipeline.this;
        }

        public void parseSource(SourceTree sourceTree) {
            if (this.holder != null) return; //only parse source if holder wasn't created
            Lexer javaLexer = createLexer(content, storage);
            List<Token> tokens = javaLexer.scanTokens();

            String fileName = file.getName().replace("." + TextBasedCompilePipeline.this.getFileExtension(), "");
            HolderParser parser = createHolderParser(storage, sourceTree, this);
            parser.apply(tokens.toArray(new Token[0]), varTypeContainer);

            ClassConstructor decl = parser.parseFile(fileName, pck);

            if (decl == null) return;

            holder = decl;
        }

        public void analyseSyntax(SourceTree sourceTree) {
            StmtParser javaStmtParser = TextBasedCompilePipeline.this.createStmtParser(this.storage, sourceTree, this);

            javaStmtParser.pushFallback(this.holder.target());
            builder = holder.construct(javaStmtParser, this.varTypeContainer, this.storage);
            javaStmtParser.popFallback();
        }
    }
}
