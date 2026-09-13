package net.kapitencraft.lang.exe.load;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.analyser.FinalsPopulatedAnalyser;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.compiler.exe.CompileStage;
import net.kapitencraft.lang.compiler.exe.pipeline.CompilePipeline;
import net.kapitencraft.lang.compiler.parser.VarTypeContainer;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class CompileSource extends ClassLoaderHolder<CompileSource> {
    protected final String content;
    protected final ErrorStorage storage;
    protected final VarTypeContainer varTypeContainer;
    protected ClassConstructor holder;
    protected Compiler.ClassBuilder builder;
    protected CacheableClass target;

    public CompileSource(File file) {
        super(file);
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.storage = new ErrorStorage(
                content.split("\n", Integer.MAX_VALUE), //second param required to not skip empty lines
                file.getAbsolutePath().replace(".\\", "") //remove '\.\'
        );
        this.varTypeContainer = new VarTypeContainer();
    }

    public CompileSource(ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser) {
        super(null);
        this.content = null; //not necessary with the holder already present
        this.storage = storage;
        this.holder = holder;
        this.varTypeContainer = parser;
    }

    public final void cache() {
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

    @Override
    public void applySkeleton() {
        this.holder.applySkeleton(storage);
    }

    public void finalizeLoad() {

        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(storage, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinalMethods(storage, builder.methods());
            }
        }
        //TODO get access to final fields
        FinalsPopulatedAnalyser analyser = new FinalsPopulatedAnalyser(this.storage);

        target = builder.build();
        this.holder.target().setTarget((ScriptedClass) target);
    }

    public void printErrors() {
        this.storage.printAll();
    }

    public ErrorStorage getErrorInfo() {
        return storage;
    }

    public void optimize() {
        this.target.optimize();
    }

    public abstract CompilePipeline<?> getPipeline();

    public <T extends CompileSource> void process(CompileStage activeStage) {
        ((CompilePipeline<T>) getPipeline()).getExecutor(activeStage).process((T) this);
    }
}
