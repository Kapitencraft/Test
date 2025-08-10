package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.baked.BakedEnum;
import net.kapitencraft.lang.holder.baked.BakedInterface;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonEnum;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.field.CompileEnumConstant;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Holder {
    private static <T extends Validateable> void validateNullable(T[] validateable, Compiler.ErrorLogger logger) {
        if (validateable != null) for (T obj : validateable) obj.validate(logger);
    }

    public interface Validateable {
        void validate(Compiler.ErrorLogger logger);
    }

    public record AnnotationObj(SourceClassReference type, Token[] properties) implements Validateable {
        public void validate(Compiler.ErrorLogger logger) {
            this.type.validate(logger);
        }
    }

    public record Class(ClassType type, ClassReference target, short modifiers,
                        AnnotationObj[] annotations, Generics generics, String pck, Token name, SourceClassReference parent,
                        SourceClassReference[] interfaces,
                        Constructor[] constructors,
                        Method[] methods,
                        Field[] fields,
                        EnumConstant[] enumConstants,
                        Class[] enclosed
    ) implements Validateable {
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            if (parent != null) parent.validate(logger);
            validateNullable(interfaces, logger);
            validateNullable(constructors, logger);
            for (Method method : methods) method.validate(logger);
            validateNullable(fields, logger);
            validateNullable(enclosed, logger);
        }

        public ClassReference[] extractInterfaces() {
            return Arrays.stream(interfaces).map(SourceClassReference::getReference).toArray(ClassReference[]::new);
        }

        public Compiler.ClassBuilder construct(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            stmtParser.pushFallback(this.target);
            try {
                return switch (this.type) {
                    case ENUM -> constructEnum(stmtParser, parser, logger);
                    case INTERFACE -> constructInterface(stmtParser, parser, logger);
                    case CLASS -> constructClass(stmtParser, parser, logger);
                    case ANNOTATION -> constructAnnotation(stmtParser, parser, logger);
                };
            } finally {
                stmtParser.popFallback();
            }
        }

        public BakedEnum constructEnum(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<Token, CompileField> fields = new HashMap<>();
            Map<String, CompileField> staticFields = new HashMap<>();

            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileField fieldDecl = new CompileField(field.type().getReference(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new CompileAnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                    else fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            List<Pair<Token, CompileCallable>> staticMethods = new ArrayList<>();
            for (Method method : this.methods()) {
                CompileStmt[] body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, method.type().getReference(), method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }

                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(
                        method.type().getReference(),
                        method.extractParams(),
                        body, method.modifiers, annotations.toArray(new CompileAnnotationClassInstance[0])
                );
                if (Modifiers.isStatic(method.modifiers)) staticMethods.add(Pair.of(method.name(), methodDecl));
                else methods.add(Pair.of(method.name(), methodDecl));
            }

            List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
            for (Constructor method : this.constructors()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, ClassReference.of(VarTypeManager.VOID), method.generics);
                CompileStmt[] body = stmtParser.parse();
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }


                CompileCallable constDecl = new CompileCallable(target, method.extractParams(), body, (short) 0, annotations.toArray(new CompileAnnotationClassInstance[0]));
                stmtParser.popMethod();
                constructors.add(Pair.of(method.name(), constDecl));
            }

            ImmutableMap.Builder<String, CompileEnumConstant> enumConstants = new ImmutableMap.Builder<>();
            for (EnumConstant decl : enumConstants()) {
                CompileExpr[] args;
                if (decl.arguments.length == 0) {
                    args = new CompileExpr[0];
                    stmtParser.apply(new Token[0], parser);
                } else {
                    stmtParser.apply(decl.arguments, parser);
                    args = stmtParser.args();
                }

                int ordinal = target.get().getConstructor().getMethodOrdinal(stmtParser.argTypes(args));
                ScriptedCallable callable = target.get().getConstructor().getMethodByOrdinal(ordinal);

                stmtParser.checkArguments(args, callable, null, decl.name());

                enumConstants.put(decl.name().lexeme(), new CompileEnumConstant(decl.ordinal(), decl.name().lexeme(), ordinal, args));
            }

            List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }

            return new BakedEnum(
                    logger,
                    target(),
                    constructors.toArray(Pair[]::new),
                    methods.toArray(Pair[]::new),
                    staticMethods.toArray(Pair[]::new),
                    extractInterfaces(),
                    enumConstants.build(),
                    fields,
                    staticFields,
                    name(),
                    pck(),
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    annotations.toArray(new CompileAnnotationClassInstance[0])
            );

        }

        public BakedInterface constructInterface(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<String, CompileField> staticFields = new HashMap<>();
            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileField fieldDecl = new CompileField(field.type().getReference(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new CompileAnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                else logger.error(field.name, "fields on interfaces must be static");
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            for (Method method : this.methods()) {
                CompileStmt[] body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    stmtParser.applyMethod(method.params, target(), null, method.type().getReference(), method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(method.type().getReference(), method.extractParams(), body, method.modifiers, annotations.toArray(new CompileAnnotationClassInstance[0]));
                methods.add(Pair.of(method.name(), methodDecl));
            }

            List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }


            return new BakedInterface(
                    logger, generics, target,
                    methods.toArray(new Pair[0]),
                    staticFields,
                    extractInterfaces(),
                    name,
                    pck,
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    annotations.toArray(new CompileAnnotationClassInstance[0])
            );

        }

        public BakedClass constructClass(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<Token, CompileField> fields = new HashMap<>();
            Map<String, CompileField> staticFields = new HashMap<>();
            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileField fieldDecl = new CompileField(field.type().getReference(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new CompileAnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                else fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            for (Method method : this.methods()) {
                CompileStmt[] body = new CompileStmt[0];
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    if (Modifiers.isStatic(method.modifiers))
                        stmtParser.applyStaticMethod(method.extractParams(), method.type().getReference(), method.generics);
                    else
                        stmtParser.applyMethod(method.params(), target(), parent.getReference(), method.type().getReference(), method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(method.type().getReference(), method.extractParams(), body, method.modifiers, annotations.toArray(new CompileAnnotationClassInstance[0]));
                methods.add(Pair.of(method.name(), methodDecl));
            }

            List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
            for (Constructor constructor : this.constructors()) {
                stmtParser.apply(constructor.body(), parser);
                stmtParser.applyMethod(constructor.params(), target(), parent.getReference(), ClassReference.of(VarTypeManager.VOID), constructor.generics);
                CompileStmt[] body = stmtParser.parse();

                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : constructor.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable constDecl = new CompileCallable(target, constructor.extractParams(), body, (short) 0, annotations.toArray(new CompileAnnotationClassInstance[0]));
                stmtParser.popMethod();
                constructors.add(Pair.of(constructor.name(), constDecl));
            }

            List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }

            return new BakedClass(
                    logger,
                    generics,
                    this.target(),
                    methods.toArray(new Pair[0]),
                    constructors.toArray(new Pair[0]),
                    fields,
                    staticFields,
                    this.parent.getReference(),
                    this.name(),
                    this.pck(),
                    this.extractInterfaces(),
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    this.modifiers,
                    annotations.toArray(new CompileAnnotationClassInstance[0])
            );
        }

        public BakedAnnotation constructAnnotation(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            ImmutableMap.Builder<String, MethodWrapper> methods = new ImmutableMap.Builder<>();
            for (Method method : methods()) {
                CompileExpr val = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    val = stmtParser.literalOrReference();
                }
                List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                methods.put(method.name().lexeme(), new MethodWrapper(val, method.type.getReference(), annotations.toArray(new CompileAnnotationClassInstance[0]), method.modifiers()));
            }

            List<CompileAnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }

            return new BakedAnnotation(
                    this.target(),
                    this.name(),
                    this.pck(),
                    methods.build(),
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    annotations.toArray(new CompileAnnotationClassInstance[0])
            );

        }

        public void applySkeleton(Compiler.ErrorLogger logger) {
            ScriptedClass skeleton = createSkeleton(logger);
            this.target.setTarget(skeleton);
        }

        public ScriptedClass createSkeleton(Compiler.ErrorLogger logger) {
            return switch (this.type) {
                case ENUM -> createEnumSkeleton(logger);
                case INTERFACE -> createInterfaceSkeleton(logger);
                case CLASS -> createClassSkeleton(logger);
                case ANNOTATION -> createAnnotationSkeleton(logger);
            };
        }

        public ScriptedClass createEnumSkeleton(Compiler.ErrorLogger logger) {
            ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            List<Token> finalFields = new ArrayList<>();
            for (Field field : this.fields()) {
                SkeletonField skeletonField = new SkeletonField(field.type().getReference(), Modifiers.isFinal(field.modifiers));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name().lexeme(), skeletonField);
                else {
                    fields.put(field.name().lexeme(), skeletonField);
                    if (skeletonField.isFinal() && field.body() == null) //add non-defaulted final fields to extra list to check constructors init
                        finalFields.add(field.name());
                }
            }

            //enclosed classes
            ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
            for (Class enclosedDecl : this.enclosed()) {
                ScriptedClass generated = enclosedDecl.createSkeleton(logger);
                enclosedDecl.target().setTarget(generated);
                enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
            }

            //methods
            Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
            for (Method method : this.methods()) {
                methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            }

            //constructors
            ConstructorContainer.Builder constructorBuilder = new ConstructorContainer.Builder(finalFields, this.name(), logger);
            for (Constructor constructor : this.constructors()) {
                constructorBuilder.addMethod(
                        logger,
                        SkeletonMethod.create(constructor, this.target),
                        constructor.name()
                );
            }


            return new SkeletonEnum(
                    name().lexeme(), pck(),
                    staticFields.build(), fields.build(),
                    enclosed.build(),
                    DataMethodContainer.bakeBuilders(methods),
                    constructorBuilder
            );
        }

        public ScriptedClass createInterfaceSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            for (Field field : this.fields()) {
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name().lexeme(), new SkeletonField(field.type().getReference(), Modifiers.isFinal(field.modifiers)));
                else {
                    logger.error(field.name(), "fields inside Interfaces must always be static");
                }
            }

            //enclosed classes
            ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
            for (Class enclosedDecl : this.enclosed()) {
                ScriptedClass generated = enclosedDecl.createSkeleton(logger);
                enclosedDecl.target().setTarget(generated);
                enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
            }

            //methods
            Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
            Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
            for (Method method : this.methods()) {
                if (Modifiers.isStatic(method.modifiers)) {
                    staticMethods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                    DataMethodContainer.Builder builder = staticMethods.get(method.name().lexeme());
                    builder.addMethod(logger, SkeletonMethod.create(method), method.name());
                } else {
                    methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                    DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                    builder.addMethod(logger, SkeletonMethod.create(method), method.name());
                }
            }

            return new SkeletonInterface(
                    this.name().lexeme(),
                    this.pck(),
                    this.extractInterfaces(),
                    staticFields.build(),
                    this.generics(),
                    enclosed.build(),
                    DataMethodContainer.bakeBuilders(methods)
            );

        }

        public ScriptedClass createClassSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            List<Token> finalFields = new ArrayList<>();
            for (Field field : this.fields()) {
                SkeletonField skeletonField = new SkeletonField(field.type().getReference(), Modifiers.isFinal(field.modifiers));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name().lexeme(), skeletonField);
                else {
                    fields.put(field.name().lexeme(), skeletonField);
                    if (skeletonField.isFinal() && field.body() == null) //add non-defaulted final fields to extra list to check constructors init
                        finalFields.add(field.name());
                }
            }

            //enclosed classes
            ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
            for (Class enclosedDecl : this.enclosed()) {
                ScriptedClass generated = enclosedDecl.createSkeleton(logger);
                enclosedDecl.target().setTarget(generated);
                enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
            }

            //methods
            Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
            for (Method method : this.methods()) {
                methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            }

            //constructors
            ConstructorContainer.Builder constructorBuilder = new ConstructorContainer.Builder(finalFields, this.name(), logger);
            for (Constructor constructor : this.constructors()) {
                constructorBuilder.addMethod(
                        logger,
                        SkeletonMethod.create(constructor, this.target),
                        constructor.name()
                );
            }

            return new SkeletonClass(
                    this.generics,
                    this.name().lexeme(),
                    this.pck(), this.parent.getReference(),
                    staticFields.build(),
                    fields.build(),
                    enclosed.build(),
                    DataMethodContainer.bakeBuilders(methods),
                    constructorBuilder,
                    this.modifiers,
                    Arrays.stream(this.interfaces).map(SourceClassReference::getReference).toArray(ClassReference[]::new)
            );
        }

        public record MethodWrapper(@Nullable CompileExpr val, ClassReference type, CompileAnnotationClassInstance[] annotations, short modifiers) implements ScriptedCallable {

            @Override
            public ClassReference[] argTypes() {
                return new ClassReference[0];
            }

            @Override
            public Object call(Object[] arguments) {
                throw new IllegalAccessError("can not call method wrapper!");
            }

            @Override
            public boolean isAbstract() {
                return val == null;
            }

            @Override
            public boolean isFinal() {
                return false;
            }

            @Override
            public boolean isStatic() {
                return Modifiers.isStatic(this.modifiers);
            }
        }

        public ScriptedClass createAnnotationSkeleton(Compiler.ErrorLogger logger) {
            //enclosed classes
            ImmutableMap.Builder<String, ClassReference> enclosed = new ImmutableMap.Builder<>();
            for (Class enclosedDecl : this.enclosed()) {
                ScriptedClass generated = enclosedDecl.createSkeleton(logger);
                enclosedDecl.target().setTarget(generated);
                enclosed.put(enclosedDecl.name().lexeme(), enclosedDecl.target());
            }

            ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();
            for (Method method : methods()) {
                methods.put(method.name().lexeme(), new SkeletonAnnotationMethod(method.type.getReference(), method.body().length > 0));
            }

            return new SkeletonAnnotation(
                    this.name().lexeme(),
                    this.pck(),
                    enclosed.build(),
                    methods.build()
            );
        }
    }

    public record Constructor(AnnotationObj[] annotations, Generics generics, Token name, List<Pair<SourceClassReference, String>> params, Token[] body) implements Validateable {
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            if (annotations != null) for (AnnotationObj obj : annotations) obj.validate(logger);
            params.forEach(p -> p.left().validate(logger));
        }

        public List<? extends Pair<ClassReference, String>> extractParams() {
            return params.stream().map(p -> p.mapFirst(SourceClassReference::getReference)).toList();
        }
    }

    public record EnumConstant(Token name, int ordinal, Token[] arguments) {
    }

    public record Field(short modifiers, AnnotationObj[] annotations, SourceClassReference type, Token name, Token[] body) implements Validateable {
        @Override
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            type.validate(logger);
        }
    }

    public record Method(short modifiers, AnnotationObj[] annotations, Generics generics, SourceClassReference type, Token name, List<? extends Pair<SourceClassReference, String>> params, Token[] body) {
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            type.validate(logger);
            params.forEach(p -> p.left().validate(logger));
        }

        public List<? extends Pair<ClassReference, String>> extractParams() {
            return params.stream().map(p -> p.mapFirst(SourceClassReference::getReference)).toList();
        }
    }

    public record Generics(Generic[] variables) implements Validateable {
        public void pushToStack(GenericStack stack) {
            Map<String, ClassReference> map = new HashMap<>();
            for (Generic generic : variables) map.put(generic.name.lexeme(), generic.reference);
            stack.push(map);
        }

        @Override
        public void validate(Compiler.ErrorLogger logger) {
            for (Generic  generic : variables) generic.validate(logger);
        }

        public boolean hasGeneric(String name) {
            for (Generic generic : variables) {
                if (name.equals(generic.name.lexeme())) return true;
            }
            return false;
        }

        public ClassReference getReference(String name) {
            for (Generic generic : variables) if (name.equals(generic.name.lexeme())) return generic.reference;
            return null;
        }
    }

    public record Generic(Token name, SourceClassReference lowerBound, SourceClassReference upperBound, GenericClassReference reference) implements Validateable {

        public Generic(Token name, SourceClassReference lowerBound, SourceClassReference upperBound) {
            this(name, lowerBound, upperBound,
                    new GenericClassReference(name.lexeme(),
                            Optional.ofNullable(lowerBound).map(SourceClassReference::getReference).orElse(null),
                            Optional.ofNullable(upperBound).map(SourceClassReference::getReference).orElse(null)
                    )
            );
        }

        @Override
        public void validate(Compiler.ErrorLogger logger) {
            if (lowerBound != null) lowerBound.validate(logger);
            if (upperBound != null) upperBound.validate(logger);
        }
    }

    public record AppliedGenerics(Token reference, ClassReference[] references) {

        public void applyToStack(GenericStack stack, Generics reference, Compiler.ErrorLogger logger) {
            if (reference.variables.length != this.references.length) {
                logger.error(this.reference, "Wrong number of type arguments: " + this.references.length + "; required: "+ reference.variables.length);
            }
            Map<String, ClassReference> referenceMap = new HashMap<>();
            for (int i = 0; i < reference.variables.length; i++) {
                referenceMap.put(reference.variables[i].name.lexeme(), references[i]);
            }
            stack.push(referenceMap);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AppliedGenerics appliedGenerics && referencesEqual(references, appliedGenerics.references);
        }

        private boolean referencesEqual(ClassReference[] expected, ClassReference[] gotten) {
            if (expected.length != gotten.length) return false;
            for (int i = 0; i < expected.length; i++) {
                if (!expected[i].get().isChildOf(gotten[i].get())) {
                    return false;
                }
            }
            return true;
        }
    }
}
