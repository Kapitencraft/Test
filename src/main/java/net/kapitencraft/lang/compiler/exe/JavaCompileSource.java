package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.pipeline.CompilePipeline;
import net.kapitencraft.lang.compiler.exe.pipeline.JavaCompilePipeline;
import net.kapitencraft.lang.compiler.parser.HolderParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.compiler.parser.VarTypeContainer;
import net.kapitencraft.lang.exe.load.CompileSource;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.holder.token.Token;

import java.io.File;
import java.util.List;

public class JavaCompileSource extends CompileSource {

    public JavaCompileSource(File file) {
        super(file);
    }

    public JavaCompileSource(ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser) {
        super(holder, storage, parser);
    }

    public void parseSource() {
        if (this.holder != null) return; //only parse source if holder wasn't created
        Lexer lexer = new Lexer(content, storage);
        List<Token> tokens = lexer.scanTokens();
        String fileName = file.getName().replace(".scr", "");
        HolderParser parser = new HolderParser(storage);
        parser.apply(tokens.toArray(new Token[0]), varTypeContainer);

        String rootPath = Compiler.source.getAbsolutePath();
        String path = file.getParentFile().getAbsolutePath().substring(rootPath.length() + 1).replace(".scr", "");
        String pck = path.replace('\\', '.');
        ClassConstructor decl = parser.parseFile(fileName, pck);

        if (decl == null) return;

        holder = decl;
    }

    public void validate() {
        this.varTypeContainer.validate(this.storage);
        this.holder.validate(this.storage);
    }

    public void analyseSyntax() {
        StmtParser stmtParser = new StmtParser(this.storage);

        stmtParser.pushFallback(this.holder.target());
        builder = holder.construct(stmtParser, this.varTypeContainer, this.storage);
        stmtParser.popFallback();
    }

    public void analyseSemantics() {
        if (builder != null)
            builder.analyse();
    }

    @Override
    public CompilePipeline<?> getPipeline() {
        return JavaCompilePipeline.INSTANCE;
    }
}
