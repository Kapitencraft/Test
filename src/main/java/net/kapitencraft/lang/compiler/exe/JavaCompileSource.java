package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.pipeline.CompilePipeline;
import net.kapitencraft.lang.compiler.exe.pipeline.JavaCompilePipeline;
import net.kapitencraft.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.lang.compiler.parser.HolderParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.compiler.parser.VarTypeContainer;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.holder.token.Token;

import java.io.File;
import java.util.List;

public class JavaCompileSource extends CompileSource {
    private final SourceTree.DirectoryNode declaring;

    public JavaCompileSource(File file, String name, String pck, SourceTree.DirectoryNode declaring) {
        super(name, pck, file);
        this.declaring = declaring;
    }

    public JavaCompileSource(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
        super(name, pck, holder, storage, parser);
        this.declaring = declaring;
    }

    public void parseSource(SourceTree sourceTree) {
        if (this.holder != null) return; //only parse source if holder wasn't created
        Lexer lexer = new Lexer(content, storage);
        List<Token> tokens = lexer.scanTokens();

        String fileName = file.getName().replace(".scr", "");
        HolderParser parser = new HolderParser(storage, sourceTree, this);
        parser.apply(tokens.toArray(new Token[0]), varTypeContainer);

        ClassConstructor decl = parser.parseFile(fileName, pck);

        if (decl == null) return;

        holder = decl;
    }

    public void validate(SourceTree sourceTree) {
        this.varTypeContainer.validate(this.storage);
        this.holder.validate(this.storage);
    }

    public void analyseSyntax(SourceTree sourceTree) {
        StmtParser stmtParser = new StmtParser(this.storage, sourceTree, this);

        stmtParser.pushFallback(this.holder.target());
        builder = holder.construct(stmtParser, this.varTypeContainer, this.storage);
        stmtParser.popFallback();
    }

    public void analyseSemantics(SourceTree sourceTree) {
        if (builder != null)
            builder.analyse();
    }

    @Override
    public CompilePipeline<?> getPipeline() {
        return JavaCompilePipeline.INSTANCE;
    }

    public SourceTree.DirectoryNode getDeclaring() {
        return declaring;
    }

    @Override
    public String toString() {
        return "JavaSource{name=" + name + ", pck=" + pck + ", file=" + file + '}';
    }
}
