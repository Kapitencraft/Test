package net.kapitencraft.lang.oop.clazz.generated;

import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.bytecode.FieldInfo;
import net.kapitencraft.lang.holder.bytecode.MethodInfo;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.bytecode.attributes.*;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.field.CompileField;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CompileClass implements CacheableClass, ScriptedClass {
    private final GeneratedMethodMap methods;
    private final Map<String, DataMethodContainer> allMethods;

    private final Map<String, CompileField> allFields;

    private final ClassReference superclass;
    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final short modifiers;

    private final Annotation[] annotations;

    public CompileClass(Map<String, DataMethodContainer> methods,
                        Map<String, CompileField> fields,
                        ClassReference superclass, String name, String packageRepresentation,
                        ClassReference[] implemented,
                        short modifiers, Annotation[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allMethods = methods;
        this.allFields = fields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    @Override
    public ClassReference reference() {
        return CacheableClass.super.reference();
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + allMethods + ", " +
                "fields=" + allFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object setStaticField(String name, Object val) {
        return null;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String absoluteName() {
        return CacheableClass.super.absoluteName();
    }

    @Override
    public String pck() {
        return this.packageRepresentation;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return this.superclass;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return null;
    }

    @Override
    public AbstractMethodMap getMethods() {
        return methods;
    }

    @Override
    public Annotation[] annotations() {
        return this.annotations;
    }

    @Override
    public short getModifiers() {
        return 0;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    public FieldInfo[] extractFields() {
        FieldInfo[] infos = new FieldInfo[this.allFields.size()];
        int[] i = {0};
        this.allFields.forEach((s, compileField) -> {
            List<AttributeInfo> attributes = new ArrayList<>();

            if (compileField.getInit() instanceof Expr.Literal literal) {
                attributes.add(new ConstantValueAttributeInfo(literal.literal.literal().toBytecode()));
            }

            attributes.add(new SignatureAttributeInfo(VarTypeManager.getClassName(this)));

            //ConstantValue, Synthetic, Signature, Deprecated, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

            infos[i[0]++] = new FieldInfo(
                    compileField.modifiers(),
                    s,
                    VarTypeManager.getClassName(compileField.getType()),
                    attributes.toArray(AttributeInfo[]::new)
            );
        });
        return infos;
    }

    public MethodInfo[] extractMethods(CacheBuilder builder) {
        MethodInfo[] infos = new MethodInfo[this.allMethods.values().stream()
                .mapToInt(DataMethodContainer::size)
                .sum()
                ];
        int[] i = {0};
        this.allMethods.forEach((s, container) -> {

            for (ScriptedCallable method : container.methods()) {
                ((CompileCallable) method).build(builder);

                //Code, Exceptions, Synthetic, Signature, Deprecated, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
                //RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault
                List<AttributeInfo> attributes = new ArrayList<>();
                if (!method.isAbstract()) {
                    attributes.add(CodeAttributeInfo.create(method.getChunk()));
                }
                attributes.add(SignatureAttributeInfo.create(method.getMethodTypeSignature()));
                attributes.add(ExceptionsAttributeInfo.create(method.thrown()));

                infos[i[0]++] = new MethodInfo(
                        method.modifiers(),
                        s,
                        VarTypeManager.getClassName(method.retType()),
                        attributes.toArray(new AttributeInfo[0])
                );
            }
        });
        return infos;
    }
}