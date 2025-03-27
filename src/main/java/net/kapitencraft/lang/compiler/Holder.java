package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
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
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonEnum;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.lang.oop.field.GeneratedEnumConstant;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
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
            Map<Token, GeneratedField> fields = new HashMap<>();
            Map<String, GeneratedField> staticFields = new HashMap<>();

            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedField fieldDecl = null;// new GeneratedField(field.type(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                    else fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
            List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
            for (Method method : this.methods()) {
                List<CompileStmt> body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, method.type(), method.generics);
                    stmtParser.pushGenerics(method.generics());
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }

                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedCallable methodDecl = null;// new GeneratedCallable(method.type(), method.params(), body, method.modifiers, annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(method.modifiers)) staticMethods.add(Pair.of(method.name(), methodDecl));
                else methods.add(Pair.of(method.name(), methodDecl));
            }

            List<Pair<Token, GeneratedCallable>> constructors = new ArrayList<>();
            for (Constructor method : this.constructors()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, ClassReference.of(VarTypeManager.VOID), method.generics);
                stmtParser.pushGenerics(method.generics());
                List<CompileStmt> body = stmtParser.parse();
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }


                GeneratedCallable constDecl = null;// new GeneratedCallable(target, method.params(), body, (short) 0, annotations.toArray(new AnnotationClassInstance[0]));
                stmtParser.popMethod();
                constructors.add(Pair.of(method.name(), constDecl));
            }


            ImmutableMap.Builder<String, GeneratedEnumConstant> enumConstants = new ImmutableMap.Builder<>();
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

                stmtParser.checkArguments(args, callable, decl.name());

                //enumConstants.put(decl.name().lexeme(), new GeneratedEnumConstant(target.get(), decl.ordinal(), decl.name().lexeme(), ordinal, args));
            }

            List<AnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }

            return new BakedEnum(
                    logger,
                    target(),
                    constructors.toArray(Pair[]::new),
                    methods.toArray(Pair[]::new),
                    staticMethods.toArray(Pair[]::new),
                    interfaces(),
                    enumConstants.build(),
                    fields,
                    staticFields,
                    name(),
                    pck(),
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    annotations.toArray(new AnnotationClassInstance[0])
            );

        }

        public BakedInterface constructInterface(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {

            Map<String, GeneratedField> staticFields = new HashMap<>();
            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedField fieldDecl = null;// new GeneratedField(field.type(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                else logger.error(field.name, "fields on interfaces must be static");
            }

            List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
            List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
            for (Method method : this.methods()) {
                List<CompileStmt> body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    stmtParser.applyMethod(method.params, target(), null, method.type(), method.generics);
                    stmtParser.pushGenerics(method.generics());
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedCallable methodDecl = null;// new GeneratedCallable(method.type(), method.params, body, method.modifiers, annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(method.modifiers)) staticMethods.add(Pair.of(method.name(), methodDecl));
                else methods.add(Pair.of(method.name(), methodDecl));
            }

            List<AnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }


            return new BakedInterface(
                    logger, target,
                    methods.toArray(new Pair[0]),
                    staticMethods.toArray(new Pair[0]),
                    staticFields,
                    interfaces,
                    name,
                    pck,
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    annotations.toArray(new AnnotationClassInstance[0])
            );

        }

        public BakedClass constructClass(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<Token, GeneratedField> fields = new HashMap<>();
            Map<String, GeneratedField> staticFields = new HashMap<>();
            for (Field field : fields()) {
                CompileExpr initializer = null;
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    initializer = stmtParser.expression();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedField fieldDecl = null; // new GeneratedField(field.type(), initializer, Modifiers.isFinal(field.modifiers), annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                else fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, GeneratedCallable>> methods = new ArrayList<>();
            List<Pair<Token, GeneratedCallable>> staticMethods = new ArrayList<>();
            for (Method method : this.methods()) {
                List<CompileStmt> body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    if (Modifiers.isStatic(method.modifiers))
                        stmtParser.applyStaticMethod(method.params(), method.type());
                    else
                        stmtParser.applyMethod(method.params(), target(), parent, method.type(), method.generics);
                    stmtParser.pushGenerics(method.generics());
                    body = stmtParser.parse();
                    stmtParser.popMethod();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedCallable methodDecl = null;// new GeneratedCallable(method.type(), method.params(), body, method.modifiers, annotations.toArray(new AnnotationClassInstance[0]));
                if (Modifiers.isStatic(method.modifiers)) staticMethods.add(Pair.of(method.name(), methodDecl));
                else methods.add(Pair.of(method.name(), methodDecl));
            }

            List<Pair<Token, GeneratedCallable>> constructors = new ArrayList<>();
            for (Constructor constructor : this.constructors()) {
                stmtParser.apply(constructor.body(), parser);
                stmtParser.applyMethod(constructor.params(), target(), parent, ClassReference.of(VarTypeManager.VOID), constructor.generics);
                stmtParser.pushGenerics(constructor.generics());
                List<CompileStmt> body = stmtParser.parse();

                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : constructor.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                GeneratedCallable constDecl = null;// new GeneratedCallable(target, constructor.params(), body, (short) 0, annotations.toArray(new AnnotationClassInstance[0]));
                stmtParser.popMethod();
                constructors.add(Pair.of(constructor.name(), constDecl));
            }

            List<AnnotationClassInstance> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }

            return new BakedClass(
                    logger,
                    this.target(),
                    methods.toArray(new Pair[0]),
                    staticMethods.toArray(new Pair[0]),
                    constructors.toArray(new Pair[0]),
                    fields,
                    staticFields,
                    this.parent,
                    this.name(),
                    this.pck(),
                    this.interfaces,
                    Arrays.stream(enclosed)
                            .map(classConstructor -> classConstructor.construct(stmtParser, parser, logger))
                            .toArray(Compiler.ClassBuilder[]::new),
                    this.modifiers,
                    annotations.toArray(new AnnotationClassInstance[0])
            );
        }

        public BakedAnnotation constructAnnotation(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            ImmutableMap.Builder<String, MethodWrapper> methods = new ImmutableMap.Builder<>();
            for (Method method : methods()) {
                Expr val = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    val = stmtParser.literalOrReference();
                }
                List<AnnotationClassInstance> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                methods.put(method.name().lexeme(), new MethodWrapper(val, method.type, annotations.toArray(new AnnotationClassInstance[0])));
            }

            List<AnnotationClassInstance> annotations = new ArrayList<>();
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
                    annotations.toArray(new AnnotationClassInstance[0])
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
                SkeletonField skeletonField = new SkeletonField(field.type(), Modifiers.isFinal(field.modifiers));
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
                    DataMethodContainer.bakeBuilders(staticMethods),
                    constructorBuilder
            );
        }

        public ScriptedClass createInterfaceSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            for (Field field : this.fields()) {
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name().lexeme(), new SkeletonField(field.type(), Modifiers.isFinal(field.modifiers)));
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
                    this.interfaces,
                    staticFields.build(),
                    enclosed.build(),
                    DataMethodContainer.bakeBuilders(methods),
                    DataMethodContainer.bakeBuilders(staticMethods)
            );

        }

        public ScriptedClass createClassSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            List<Token> finalFields = new ArrayList<>();
            for (Field field : this.fields()) {
                SkeletonField skeletonField = new SkeletonField(field.type(), Modifiers.isFinal(field.modifiers));
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
                    this.name().lexeme(),
                    this.pck(), this.parent,
                    staticFields.build(),
                    fields.build(),
                    enclosed.build(),
                    DataMethodContainer.bakeBuilders(methods),
                    DataMethodContainer.bakeBuilders(staticMethods),
                    constructorBuilder,
                    this.modifiers,
                    this.interfaces
            );
        }

        public record MethodWrapper(@Nullable Expr val, ClassReference type, AnnotationClassInstance[] annotations) implements ScriptedCallable {

            @Override
            public ClassReference[] argTypes() {
                return new ClassReference[0];
            }

            @Override
            public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                return null; //val == null ? null : interpreter.evaluate(val);
            }

            @Override
            public boolean isAbstract() {
                return val == null;
            }

            @Override
            public boolean isFinal() {
                return false;
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
                methods.put(method.name().lexeme(), new SkeletonAnnotationMethod(method.type, method.body().length > 0));
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
            this(name, lowerBound, upperBound, new GenericClassReference(name.lexeme(), lowerBound, upperBound));
        }

        @Override
        public void validate(Compiler.ErrorLogger logger) {
            if (lowerBound != null) lowerBound.validate(logger);
            if (upperBound != null) upperBound.validate(logger);
        }
    }
}
