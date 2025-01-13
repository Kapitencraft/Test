package net.kapitencraft.lang.run.load;

import net.kapitencraft.lang.compiler.*;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.parser.ExprParser;
import net.kapitencraft.lang.compiler.parser.SkeletonParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class CompilerHolder extends ClassHolder {
    private final String content;
    private final Compiler.ErrorLogger logger;
    private SkeletonParser.ClassConstructor<?> constructor;
    private Compiler.ClassBuilder builder;
    private CacheableClass target;

    public CompilerHolder(File file, ClassHolder[] children) {
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

    public void applyConstructor() {
        VarTypeParser varTypeParser = new VarTypeParser();
        Lexer lexer = new Lexer(content, logger);
        List<Token> tokens = lexer.scanTokens();
        String fileName = file.getName().replace(".scr", "");
        SkeletonParser parser = new SkeletonParser(logger, fileName);
        parser.apply(tokens.toArray(new Token[0]), varTypeParser);

        SkeletonParser.ClassConstructor<?> decl = parser.parse(this.reference);

        if (decl == null) return;

        String path = file.getParentFile().getPath().substring(10).replace(".scr", "");
        String pck = path.replace('\\', '.');
        String declPck = decl.pck().substring(0, decl.pck().length()-1);
        if (!Objects.equals(declPck, pck)) {
            logger.errorF(
                    tokens.get(0),
                    "package path '%s' does not match file path '%s'", declPck, pck);
        }

        constructor = decl;
    }

    public void construct() {
        if (!checkConstructorCreated()) return;
        ExprParser exprParser = new ExprParser(this.logger);
        StmtParser stmtParser = new StmtParser(this.logger);
        builder = constructor.construct(stmtParser, exprParser);
    }

    public void cache(CacheBuilder builder) {
        try {
            Compiler.cache(
                    ClassLoader.cacheLoc,
                    builder,
                    target.packageRepresentation().replace(".", "/"),
                    target,
                    target.name()
            );
        } catch (IOException e) {
            System.err.println("Fehler beim Laden: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean isInterface() {
        return false;
    }

    protected boolean checkConstructorCreated() {
        return constructor != null;
    }

    @Override
    public ScriptedClass createSkeleton() {
        if (!checkConstructorCreated()) return null;
        return constructor.applySkeleton();
    }

    @Override
    public ScriptedClass loadClass() {
        if (!checkConstructorCreated()) return null;

        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(logger, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinal(logger, builder.methods());
            }
        }
        return target = builder.build();
    }
}
