package net.kapitencraft.lang.run.load;

import net.kapitencraft.lang.compiler.*;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.HolderParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class CompilerLoaderHolder extends ClassLoaderHolder {
    private final String content;
    private final Compiler.ErrorLogger logger;
    private Holder.Class holder;
    private Compiler.ClassBuilder builder;
    private CacheableClass target;
    private final VarTypeParser varTypeParser = new VarTypeParser();

    public CompilerLoaderHolder(File file, ClassLoaderHolder[] children) {
        super(file, children);
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.logger = new Compiler.ErrorLogger(
                content.split("\n", Integer.MAX_VALUE), //second param required to not skip empty lines
                file.getAbsolutePath().replace(".\\", "") //remove '\.\'
        );
    }

    @Override
    protected ClassType getType() {
        return null;
    }

    public void applyConstructor() {
        Lexer lexer = new Lexer(content, logger);
        List<Token> tokens = lexer.scanTokens();
        String fileName = file.getName().replace(".scr", "");
        HolderParser parser = new HolderParser(logger);
        parser.apply(tokens.toArray(new Token[0]), varTypeParser);

        Holder.Class decl = parser.parseFile(fileName);

        if (decl == null) return;

        String path = file.getParentFile().getPath().substring(10).replace(".scr", "");
        String pck = path.replace('\\', '.');
        String declPck = decl.pck();
        if (!Objects.equals(declPck, pck)) {
            logger.errorF(
                    tokens.get(0),
                    "package path '%s' does not match file path '%s'", declPck, pck);
        }

        holder = decl;
    }

    public void construct() {
        if (!checkHolderCreated()) return;
        ExprParser exprParser = new ExprParser(this.logger);
        StmtParser stmtParser = new StmtParser(this.logger);

        builder = holder.construct(stmtParser, exprParser, this.varTypeParser, this.logger);
    }

    public void cache(CacheBuilder builder) {
        try {
            Compiler.cache(
                    ClassLoader.cacheLoc,
                    builder,
                    target.pck().replace(".", "/"),
                    target,
                    target.name()
            );
        } catch (IOException e) {
            System.err.println("Error saving class '" + target.absoluteName() + "': " + e.getMessage());
        }
    }

    protected boolean checkHolderCreated() {
        return holder != null;
    }

    @Override
    public void applySkeleton() {
        if (checkHolderCreated()) this.holder.applySkeleton(logger);
    }

    @Override
    public ScriptedClass loadClass() {
        if (!checkHolderCreated()) return null;

        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(logger, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinal(logger, builder.methods());
            }
        }
        return target = builder.build();
    }

    public void validate() {
        if (!checkHolderCreated()) return;
        this.varTypeParser.validate(this.logger);
        this.holder.validate(this.logger);
    }
}
