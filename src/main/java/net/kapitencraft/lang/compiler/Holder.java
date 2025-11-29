package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.parser.StmtParser;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.lang.holder.baked.BakedClass;
import net.kapitencraft.lang.holder.baked.BakedInterface;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.tool.Util;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.NotNull;
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
                        EnumConstant[] enumConstants
    ) implements Validateable {
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            if (parent != null) parent.validate(logger);
            validateNullable(interfaces, logger);
            validateNullable(constructors, logger);
            for (Method method : methods) method.validate(logger);
            validateNullable(fields, logger);
        }

        private ClassReference[] extractInterfaces() {
            return Arrays.stream(interfaces).map(SourceClassReference::getReference).toArray(ClassReference[]::new);
        }

        private String[] extractInterfacesToString() {
            return Arrays.stream(interfaces).map(SourceClassReference::get).map(VarTypeManager::getClassName).toArray(String[]::new);
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

        public BakedClass constructEnum(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {

            List<Stmt> statics = new ArrayList<>();

            Map<Token, CompileField> fields = new HashMap<>();

            for (EnumConstant decl : enumConstants()) {
                Expr[] args;
                if (decl.arguments.length == 0) {
                    args = new Expr[0];
                    stmtParser.apply(new Token[0], parser);

                } else {
                    stmtParser.apply(decl.arguments, parser);
                    args = stmtParser.args();
                }

                ScriptedCallable callable = Util.getClosest(target.get(), "<init>", stmtParser.argTypes(args));

                stmtParser.checkArguments(args, callable, null, decl.name());
                statics.add(new Stmt.Expression(new Expr.StaticSet(
                        target, decl.name,
                        new Expr.Constructor(decl.name, target, args, VarTypeManager.getMethodSignature(target.get(), "<init>", stmtParser.argTypes(args))),
                        new Token(TokenType.ASSIGN, "=", LiteralHolder.EMPTY, -1, 0),
                        target
                )));
            }

            //region $VALUES
            int length = enumConstants().length;
            Token values = Token.createNative("$VALUES");
            fields.put(values, new CompileField(target.array(), Modifiers.pack(true, true, false), new Annotation[0]));
            Expr[] constants = new Expr[length];
            for (int i = 0; i < enumConstants.length; i++) {
                EnumConstant constant = enumConstants[i];
                constants[i] = new Expr.StaticGet(target, constant.name); //getting statics to store into the array
            }
            statics.add(new Stmt.Expression(new Expr.StaticSet(
                    target, values,
                    new Expr.ArrayConstructor(Token.createNative("new"), target, new Expr.Literal(new Token(
                            TokenType.NUM,
                            String.valueOf(length),
                            new LiteralHolder(length, VarTypeManager.INTEGER),
                            -1,
                            0
                            )),
                            constants
                    ),
                    new Token(TokenType.ASSIGN, "=", LiteralHolder.EMPTY, -1, 0),
                    target
            )));
            //endregion

            List<String> finalFields = new ArrayList<>();
            for (Field field : fields()) {
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    Expr initializer = stmtParser.expression();
                    if (Modifiers.isStatic(field.modifiers)) {
                        statics.add(new Stmt.Expression(new Expr.StaticSet(target, field.name, initializer, field.assign, target)));
                    }
                } else if (Modifiers.isFinal(field.modifiers)) finalFields.add(field.name().lexeme());
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                short mods = Modifiers.fromJavaMods(field.modifiers);
                CompileField fieldDecl = new CompileField(field.type().getReference(), mods, annotations.toArray(Annotation[]::new));
                fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            for (Method method : this.methods()) {
                Stmt[] body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    if (Modifiers.isStatic(method.modifiers))
                        stmtParser.applyStaticMethod(method.params, method.type.getReference(), method.generics);
                    else
                        stmtParser.applyMethod(method.params(), target(), VarTypeManager.ENUM, method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod(method.closeBracket);
                }

                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(
                        method.type().getReference(),
                        method.extractParams(),
                        body, method.modifiers, annotations.toArray(Annotation[]::new)
                );
                methods.add(Pair.of(method.name(), methodDecl));
            }

            statics.add(new Stmt.Return(Token.createNative("return"), null));
            methods.add(Pair.of(Token.createNative("<clinit>"),
                    new CompileCallable(VarTypeManager.VOID.reference(),
                            List.of(),
                            statics.toArray(new Stmt[0]),
                            Modifiers.pack(true, true, false),
                            new Annotation[0]
                    )
            ));

            //region #values
            methods.add(Pair.of(Token.createNative("values"),
                    new CompileCallable(
                            target.array(),
                            List.of(),
                            new Stmt[]{
                                    new Stmt.Return(
                                            Token.createNative("return"),
                                            new Expr.StaticGet(
                                                    target,
                                                    values
                                            )
                                    )
                            },
                            Modifiers.pack(false, true, false),
                            new Annotation[0]
                    )
            ));
            //endregion

            List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
            for (Constructor method : this.constructors()) {
                stmtParser.apply(method.body(), parser);
                stmtParser.applyMethod(method.params(), target(), ClassReference.of(VarTypeManager.VOID), method.generics);
                Stmt[] body = stmtParser.parse();
                this.checkFinalsPopulated(body, finalFields);
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }


                CompileCallable constDecl = new CompileCallable(target, method.extractParams(), body, (short) 0, annotations.toArray(Annotation[]::new));
                stmtParser.popMethod(method.closeBracket);
                constructors.add(Pair.of(method.name(), constDecl));
            }

            return new BakedClass(
                    logger,
                    new Generics(new Generic[0]),
                    target(),
                    methods.toArray(Pair[]::new),
                    constructors.toArray(Pair[]::new),
                    fields,
                    VarTypeManager.ENUM,
                    name(),
                    pck(),
                    extractInterfaces(),
                    Modifiers.pack(true, true, false),
                    parseAnnotations(stmtParser, parser)
            );
        }

        private Annotation[] parseAnnotations(StmtParser stmtParser, VarTypeParser parser) {
            List<Annotation> annotations = new ArrayList<>();
            for (AnnotationObj obj : this.annotations()) {
                annotations.add(stmtParser.parseAnnotation(obj, parser));
            }
            return annotations.toArray(Annotation[]::new);
        }

        private void checkFinalsPopulated(Stmt[] body, List<String> finalFields) {

        }

        public BakedClass constructClass(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<Token, CompileField> fields = new HashMap<>();
            List<Stmt> statics = new ArrayList<>();
            for (Field field : fields()) {
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    Expr initializer = stmtParser.expression();
                    statics.add(new Stmt.Expression(new Expr.StaticSet(target, field.name, initializer, field.assign, target)));
                }
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                short mods = Modifiers.fromJavaMods(field.modifiers);
                CompileField fieldDecl = new CompileField(field.type().getReference(), mods, annotations.toArray(Annotation[]::new));
                fields.put(field.name, fieldDecl);
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            for (Method method : this.methods()) {
                Stmt[] body = new Stmt[0];
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    if (Modifiers.isStatic(method.modifiers))
                        stmtParser.applyStaticMethod(method.params, method.type().getReference(), method.generics);
                    else
                        stmtParser.applyMethod(method.params(), target(), method.type().getReference(), method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod(method.closeBracket);
                }
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(method.type().getReference(), method.extractParams(), body, method.modifiers, annotations.toArray(Annotation[]::new));
                methods.add(Pair.of(method.name(), methodDecl));
            }

            if (!statics.isEmpty()) {
                statics.add(new Stmt.Return(Token.createNative("return"), null));
                methods.add(Pair.of( //add <clinit> method
                        Token.createNative("<clinit>"),
                        new CompileCallable(
                                VarTypeManager.VOID.reference(),
                                List.of(),
                                statics.toArray(new Stmt[0]),
                                Modifiers.pack(true, true, false),
                                new Annotation[0]
                        )
                ));
            }

            List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
            for (Constructor constructor : this.constructors()) {
                stmtParser.apply(constructor.body(), parser);
                stmtParser.applyMethod(constructor.params(), target(), ClassReference.of(VarTypeManager.VOID), constructor.generics);
                Stmt[] body = stmtParser.parse();

                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : constructor.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable constDecl = new CompileCallable(target, constructor.extractParams(), body, (short) 0, annotations.toArray(Annotation[]::new));
                stmtParser.popMethod(constructor.closeBracket);
                constructors.add(Pair.of(constructor.name(), constDecl));
            }

            List<Annotation> annotations = new ArrayList<>();
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
                    this.parent.getReference(),
                    this.name(),
                    this.pck(),
                    this.extractInterfaces(),
                    this.modifiers,
                    annotations.toArray(Annotation[]::new)
            );
        }

        private static @NotNull List<String> getFinalFields(List<Holder.Field> fields) {
            List<String> finalFields = new ArrayList<>();
            for (Holder.Field field : fields) {
                if (Modifiers.isFinal(field.modifiers()) && field.body() == null) finalFields.add(field.name().lexeme());
            }
            return finalFields;
        }

        public BakedInterface constructInterface(StmtParser stmtParser, VarTypeParser parser, Compiler.ErrorLogger logger) {
            Map<String, CompileField> staticFields = new HashMap<>();
            List<Stmt> statics = new ArrayList<>();
            for (Field field : fields()) {
                if (field.body() != null) {
                    stmtParser.apply(field.body(), parser);
                    Expr initializer = stmtParser.expression();
                    statics.add(new Stmt.Expression(new Expr.StaticSet(target, field.name, initializer, field.assign, target)));
                }
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : field.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                short mods = Modifiers.fromJavaMods(field.modifiers);
                CompileField fieldDecl = new CompileField(field.type().getReference(), mods, annotations.toArray(Annotation[]::new));
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name.lexeme(), fieldDecl);
                else logger.error(field.name, "fields on interfaces must be static");
            }

            List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
            for (Method method : this.methods()) {
                Stmt[] body = null;
                if (!Modifiers.isAbstract(method.modifiers)) {
                    stmtParser.apply(method.body(), parser);
                    if (Modifiers.isStatic(method.modifiers))
                        stmtParser.applyStaticMethod(method.params, method.type.getReference(), method.generics);
                    else
                        stmtParser.applyMethod(method.params, target(), method.type().getReference(), method.generics);
                    body = stmtParser.parse();
                    stmtParser.popMethod(method.closeBracket);
                }
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                CompileCallable methodDecl = new CompileCallable(method.type().getReference(), method.extractParams(), body, method.modifiers, annotations.toArray(Annotation[]::new));
                methods.add(Pair.of(method.name(), methodDecl));
            }

            if (!statics.isEmpty()) {
                statics.add(new Stmt.Return(Token.createNative("return"), null));
                methods.add(Pair.of( //add <clinit> method
                        Token.createNative("<clinit>"),
                        new CompileCallable(
                                VarTypeManager.VOID.reference(),
                                List.of(),
                                statics.toArray(new Stmt[0]),
                                Modifiers.pack(true, true, false),
                                new Annotation[0]
                        )
                ));
            }

            List<Annotation> annotations = new ArrayList<>();
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
                    annotations.toArray(Annotation[]::new)
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
                List<Annotation> annotations = new ArrayList<>();
                for (AnnotationObj obj : method.annotations()) {
                    annotations.add(stmtParser.parseAnnotation(obj, parser));
                }

                methods.put(method.name().lexeme(), new MethodWrapper(val, method.type.getReference(), annotations.toArray(Annotation[]::new), method.modifiers()));
            }

            return new BakedAnnotation(
                    this.target(),
                    this.name(),
                    this.pck(),
                    methods.build(),
                    parseAnnotations(stmtParser, parser)
            );

        }

        public void applySkeleton(Compiler.ErrorLogger logger) {
            ScriptedClass skeleton = createSkeleton(logger);
            this.target.setTarget(skeleton);
        }

        public ScriptedClass createSkeleton(Compiler.ErrorLogger logger) {
            return switch (this.type) {
                case INTERFACE -> createInterfaceSkeleton(logger);
                case ENUM, CLASS -> createClassSkeleton(logger);
                case ANNOTATION -> createAnnotationSkeleton(logger);
            };
        }

        public ScriptedClass createInterfaceSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
            for (Field field : this.fields()) {
                if (Modifiers.isStatic(field.modifiers)) staticFields.put(field.name().lexeme(), new SkeletonField(field.type().getReference(), Modifiers.fromJavaMods(field.modifiers)));
                else {
                    logger.error(field.name(), "fields inside Interfaces must always be static");
                }
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
                    this.extractInterfacesToString(),
                    staticFields.build(),
                    this.generics(),
                    DataMethodContainer.bakeBuilders(methods)
            );
        }

        public ScriptedClass createClassSkeleton(Compiler.ErrorLogger logger) {

            //fields
            ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
            List<Token> finalFields = new ArrayList<>();
            for (Field field : this.fields()) {
                SkeletonField skeletonField = new SkeletonField(field.type().getReference(), Modifiers.fromJavaMods(field.modifiers));
                fields.put(field.name().lexeme(), skeletonField);
                if (skeletonField.isFinal() && field.body() == null) //add non-defaulted final fields to extra list to check constructors init
                    finalFields.add(field.name());
            }

            //methods
            Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
            for (Method method : this.methods()) {
                methods.putIfAbsent(method.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get(method.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(method), method.name());
            }
            methods.computeIfAbsent("values", s -> new DataMethodContainer.Builder(this.name()))
                            .addMethod(logger, new SkeletonMethod(new ClassReference[0], target.array(), Modifiers.pack(false, true, false)), Token.createNative("values"));

            //constructors
            for (Constructor constructor : this.constructors()) {
                methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get("<init>");
                builder.addMethod(logger, SkeletonMethod.create(constructor, this.target), constructor.name());
            }

            return new SkeletonClass(
                    this.generics,
                    this.name().lexeme(),
                    this.pck(), VarTypeManager.getClassName(this.parent.getReference()),
                    fields.build(),
                    DataMethodContainer.bakeBuilders(methods),
                    this.modifiers,
                    Arrays.stream(this.interfaces).map(SourceClassReference::getReference).map(ClassReference::get).map(VarTypeManager::getClassName).toArray(String[]::new)
            );
        }

        public record MethodWrapper(@Nullable Expr val, ClassReference retType, Annotation[] annotations, short modifiers) implements ScriptedCallable {

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

        //annotations are not available at skeleton construction
        public ScriptedClass createAnnotationSkeleton(Compiler.ErrorLogger logger) {

            ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();
            for (Method method : methods()) {
                methods.put(method.name().lexeme(), new SkeletonAnnotationMethod(method.type.getReference(), method.body().length > 0));
            }

            return new SkeletonAnnotation(
                    this.name().lexeme(),
                    this.pck(),
                    methods.build()
            );
        }
    }

    public record Constructor(AnnotationObj[] annotations, Generics generics, Token name, Token closeBracket, List<Pair<SourceClassReference, String>> params, Token[] body) implements Validateable {
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

    public record Field(short modifiers, AnnotationObj[] annotations, SourceClassReference type, Token name, Token assign, Token[] body) implements Validateable {
        @Override
        public void validate(Compiler.ErrorLogger logger) {
            validateNullable(annotations, logger);
            type.validate(logger);
        }
    }

    public record Method(short modifiers, AnnotationObj[] annotations, Generics generics, SourceClassReference type, Token name, Token closeBracket, List<? extends Pair<SourceClassReference, String>> params, Token[] body) {
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
