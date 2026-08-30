package net.kapitencraft.lang.compiler.exe.pipeline;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.analyser.FinalsPopulatedAnalyser;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompileEnvironment;
import net.kapitencraft.lang.compiler.exe.CompilerStage;
import net.kapitencraft.lang.compiler.exe.FileInfo;
import net.kapitencraft.lang.compiler.parser.HolderParser;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.compiler.parser.VarTypeContainer;
import net.kapitencraft.lang.exe.load.ClassLoader;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JavaCompilePipeline extends CompilePipeline {

    public JavaCompilePipeline() {
        super("scr");
    }

    @Override
    public CompletableFuture<?> createProcessor(CompletableFuture<FileInfo> fileSource, CompileEnvironment environment, Executor executor) {
        return fileSource
                .thenApplyAsync(JavaCompilePipeline::parseSource, executor)
                .thenCompose(environment.waitFor(CompilerStage.PARSE_SOURCE))
                .thenApplyAsync(JavaCompilePipeline::applySkeleton, executor)
                .thenCompose(environment.waitFor(CompilerStage.CREATE_SKELETON))
                .thenApplyAsync(JavaCompilePipeline::validate, executor)
                .thenCompose(environment.waitFor(CompilerStage.VALIDATE))
                .thenApplyAsync(JavaCompilePipeline::construct, executor)
                .thenCompose(environment.waitFor(CompilerStage.SYNTAX_ANALYSIS))
                .thenApplyAsync(JavaCompilePipeline::analyse, executor)
                .thenCompose(environment.waitFor(CompilerStage.SEMANTIC_ANALYSIS))
                .thenApplyAsync(JavaCompilePipeline::generate, executor)
                .thenCompose(environment.waitFor(CompilerStage.GENERATE))
                .thenApplyAsync(JavaCompilePipeline::optimize, executor)
                .thenCompose(environment.waitFor(CompilerStage.OPTIMIZE))
                .thenAcceptAsync(JavaCompilePipeline::cache, executor);
    }

    //region parse source
    private static ParseResult parseSource(FileInfo info) {
        Lexer lexer = new Lexer(info.content(), info.errorStorage());
        List<Token> tokens = lexer.scanTokens();
        HolderParser parser = new HolderParser(info.errorStorage());
        VarTypeContainer container = new VarTypeContainer();
        parser.apply(tokens.toArray(new Token[0]), container);

        ClassConstructor decl = parser.parseFile(info.fileName(), info.pck());
        return new ParseResult(info.errorStorage(), container, decl);
    }

    private record ParseResult(ErrorStorage storage, VarTypeContainer container, ClassConstructor constructor) {
    }
    //endregion

    private static ParseResult applySkeleton(ParseResult result) {
        result.constructor.applySkeleton(result.storage);
        return result;
    }

    private static ParseResult validate(ParseResult result) {
        result.container.validate(result.storage);
        result.constructor.validate(result.storage);
        return result;
    }

    //region analyse syntax
    private static ConstructResult construct(ParseResult parseResult) {
        StmtParser stmtParser = new StmtParser(parseResult.storage);

        stmtParser.pushFallback(parseResult.constructor.target());
        Compiler.ClassBuilder builder = parseResult.constructor.construct(stmtParser, parseResult.container, parseResult.storage);

        return new ConstructResult(parseResult.storage, builder);
    }

    private record ConstructResult(ErrorStorage storage, Compiler.ClassBuilder builder) {
    }
    //endregion

    private static ConstructResult analyse(ConstructResult result) {
        result.builder.analyse();
        return result;
    }

    private static GenerateResult generate(ConstructResult result) {
        Compiler.ClassBuilder builder = result.builder;
        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(result.storage, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinalMethods(result.storage, builder.methods());
            }
        }
        //TODO get access to final fields
        FinalsPopulatedAnalyser analyser = new FinalsPopulatedAnalyser(result.storage);

        CacheableClass target = builder.build();
        //result.holder.target().setTarget((ScriptedClass) target);
        return new GenerateResult(target);
    }

    private record GenerateResult(CacheableClass target) {
    }

    private static GenerateResult optimize(GenerateResult result) {
        result.target.optimize();
        return result;
    }

    private static void cache(GenerateResult generateResult) {
        CacheableClass target = generateResult.target;
        try {
            Compiler.cache(
                    ClassLoader.cacheLoc,
                    new CacheBuilder(), //MUST under ALL CIRCUMSTANCES be thread-save. create a new object per class
                    target.pck().replace(".", "/"),
                    target,
                    target.name()
            );
        } catch (IOException e) {
            System.err.println("Error saving class '" + target.absoluteName() + "': " + e.getMessage());
        }
    }
}
