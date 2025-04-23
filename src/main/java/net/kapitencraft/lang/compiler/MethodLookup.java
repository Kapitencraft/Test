package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.method.CompileCallable;
import net.kapitencraft.lang.oop.method.RuntimeCallable;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MethodLookup {
    private final List<Pair<ScriptedClass, AbstractMethodMap>> lookup;
    private final AbstractMethodMap exposed;

    public MethodLookup(List<Pair<ScriptedClass, AbstractMethodMap>> lookup) {
        this.lookup = lookup;
        exposed = this.createExposed();
    }

    public void checkFinal(Compiler.ErrorLogger logger, Pair<Token, CompileCallable>[] map) {
        for (Pair<Token, CompileCallable> pair : map) {
            for (Pair<ScriptedClass, AbstractMethodMap> lookupElement : lookup) {
                Map<String, DataMethodContainer> methodMap = lookupElement.right().asMap();
                if (!methodMap.containsKey(pair.left().lexeme())) continue; //no method with name found, continuing
                for (ScriptedCallable method : methodMap.get(pair.left().lexeme()).getMethods()) {
                    if (!method.isFinal()) continue;
                    if (Util.matchArgs(method.argTypes(), pair.right().argTypes())) {
                        logger.errorF(pair.left(), "method '%s(%s)' can not override final method from class '%s'", pair.left().lexeme(), Util.getDescriptor(pair.right().argTypes()), lookupElement.left().name());
                    }
                }
            }
        }
    }

    public void checkAbstract(Compiler.ErrorLogger logger, Token className, Pair<Token, CompileCallable>[] map) {
        Map<String, List<Pair<ScriptedClass, ScriptedCallable>>> abstracts = new HashMap<>();
        for (Pair<ScriptedClass, AbstractMethodMap> methods : lookup) {
            methods.right().asMap().forEach((s, dataMethodContainer) -> {
                a: for (ScriptedCallable method : dataMethodContainer.getMethods()) {
                    List<Pair<ScriptedClass, ScriptedCallable>> classData = abstracts.computeIfAbsent(s, k -> new ArrayList<>());
                    if (method.isAbstract()) {
                        for (Pair<ScriptedClass, ScriptedCallable> pair : classData) {
                            if (Util.matchArgs(pair.right().argTypes(), method.argTypes())) continue a;
                        }
                        classData.add(Pair.of(methods.left(), method));
                    } else {
                        for (int i = 0; i < classData.size(); i++) {
                            Pair<ScriptedClass, ScriptedCallable> pair = classData.get(i);
                            if (Util.matchArgs(pair.right().argTypes(), method.argTypes())) {
                                classData.remove(i);
                                continue a;
                            }
                        }
                    }
                }
            });
        }
        for (Pair<Token, CompileCallable> pair : map) {
            List<Pair<ScriptedClass, ScriptedCallable>> methods = abstracts.get(pair.left().lexeme());
            if (methods == null) continue; //no abstract method for that name, continuing
            methods.removeIf(callablePair -> Util.matchArgs(pair.right().argTypes(), callablePair.right().argTypes()));
        }
        abstracts.forEach((string, pairs) -> {
            pairs.forEach(pair -> {
                String errorMsg = pair.left().isInterface() ?
                        "class %s must either be declared abstract or override abstract method '%s(%s)' from interface %s" :
                        "class %s must either be declared abstract or override abstract method '%s(%s)' from class %s";
                logger.errorF(className, errorMsg, className.lexeme(), string, Util.getDescriptor(pair.right().argTypes()), pair.left().name());
            });
        });
    }

    public static MethodLookup createFromClass(ScriptedClass scriptedClass, ClassReference... interfaces) {
        List<ScriptedClass> parentMap = createParentMap(scriptedClass);
        List<ScriptedClass> allParents = new ArrayList<>();
        for (ClassReference i : interfaces) {
            addInterfaces(i.get(), allParents::add);
            allParents.add(i.get());
        }
        for (ScriptedClass parent : parentMap) {
            addInterfaces(parent, allParents::add);
            allParents.add(parent);
        }
        List<Pair<ScriptedClass, AbstractMethodMap>> lookup = allParents.stream().collect(Util.toPairList(Function.identity(), ScriptedClass::getMethods));
        return new MethodLookup(lookup);
    }

    private static void addInterfaces(ScriptedClass target, Consumer<ScriptedClass> sink) {
        if (target.interfaces() != null) for (ClassReference anInterface : target.interfaces()) {
            addInterfaces(anInterface.get(), sink);
            sink.accept(anInterface.get());
        }
    }

    private static List<ScriptedClass> createParentMap(ScriptedClass scriptedClass) {
        Objects.requireNonNull(scriptedClass, "Can not create target map for null class!");
        List<ScriptedClass> parents = new ArrayList<>();
        if (scriptedClass.superclass() != null) {
            do {
                parents.add(scriptedClass);
                scriptedClass = scriptedClass.superclass().get();
            } while (scriptedClass != null && scriptedClass.superclass() != null);
        }
        return Util.invert(parents);
    }

    private GeneratedMethodMap createExposed() {
        Map<String, DataMethodContainer> map = new HashMap<>();
        for (Pair<ScriptedClass, AbstractMethodMap> pair : lookup) {
            pair.right().asMap().forEach((s, dataMethodContainer) -> {
                if (!map.containsKey(s)) map.put(s, dataMethodContainer);
                else {
                    DataMethodContainer val = map.get(s);
                    ScriptedCallable[] valMethods = val.getMethods();
                    ScriptedCallable[] subMethods = dataMethodContainer.getMethods();
                    List<ScriptedCallable> newMethods = new ArrayList<>();
                    a:  for (ScriptedCallable valMethod : valMethods) {
                        for (ScriptedCallable subMethod : subMethods) {
                            if (Util.matchArgs(valMethod.argTypes(), subMethod.argTypes())) {
                                newMethods.add(subMethod);
                                continue a;
                            }
                        }
                        newMethods.add(valMethod);
                    }
                    map.put(s, new DataMethodContainer(newMethods.toArray(new ScriptedCallable[0])));
                }
            });
        }
        return new GeneratedMethodMap(ImmutableMap.copyOf(map));
    }

    public int getMethodOrdinal(String name, ClassReference[] args) {
        return exposed.getMethodOrdinal(name, args);
    }

    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return exposed.getMethodByOrdinal(name, ordinal);
    }
}
