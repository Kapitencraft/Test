package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MethodLookup {
    private final List<Pair<LoxClass, AbstractMethodMap>> lookup;
    private final AbstractMethodMap exposed; //TODO make a new one for method lookup k

    public MethodLookup(List<Pair<LoxClass, AbstractMethodMap>> lookup) {
        this.lookup = lookup;
        exposed = this.createExposed();
    }

    public void checkFinal(Compiler.ErrorLogger logger, Pair<Token, GeneratedCallable>[] map) {
        for (Pair<Token, GeneratedCallable> pair : map) {
            for (Pair<LoxClass, AbstractMethodMap> lookupElement : lookup) {
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

    public void checkAbstract(Compiler.ErrorLogger logger, Token className, Pair<Token, GeneratedCallable>[] map) {
        Map<String, List<Pair<LoxClass, ScriptedCallable>>> abstracts = new HashMap<>();
        for (Pair<LoxClass, AbstractMethodMap> methods : lookup) {
            methods.right().asMap().forEach((s, dataMethodContainer) -> {
                a: for (ScriptedCallable method : dataMethodContainer.getMethods()) {
                    List<Pair<LoxClass, ScriptedCallable>> classData = abstracts.computeIfAbsent(s, k -> new ArrayList<>());
                    if (method.isAbstract()) {
                        for (Pair<LoxClass, ScriptedCallable> pair : classData) {
                            if (Util.matchArgs(pair.right().argTypes(), method.argTypes())) continue a;
                        }
                        classData.add(Pair.of(methods.left(), method));
                    } else {
                        for (int i = 0; i < classData.size(); i++) {
                            Pair<LoxClass, ScriptedCallable> pair = classData.get(i);
                            if (Util.matchArgs(pair.right().argTypes(), method.argTypes())) {
                                classData.remove(i);
                                continue a;
                            }
                        }
                    }
                }
            });
        }
        for (Pair<Token, GeneratedCallable> pair : map) {
            List<Pair<LoxClass, ScriptedCallable>> methods = abstracts.get(pair.left().lexeme());
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

    public static MethodLookup createFromClass(LoxClass loxClass, ClassReference... interfaces) {
        List<LoxClass> parentMap = createParentMap(loxClass);
        List<LoxClass> allParents = new ArrayList<>();
        for (ClassReference i : interfaces) {
            addInterfaces(i.get(), allParents::add);
            allParents.add(i.get());
        }
        for (LoxClass parent : parentMap) {
            addInterfaces(parent, allParents::add);
            allParents.add(parent);
        }
        List<Pair<LoxClass, AbstractMethodMap>> lookup = allParents.stream().collect(Util.toPairList(Function.identity(), LoxClass::getMethods));
        return new MethodLookup(lookup);
    }

    private static void addInterfaces(LoxClass target, Consumer<LoxClass> sink) {
        if (target.interfaces() != null) for (ClassReference anInterface : target.interfaces()) {
            addInterfaces(anInterface.get(), sink);
            sink.accept(anInterface.get());
        }
    }

    private static List<LoxClass> createParentMap(LoxClass loxClass) {
        Objects.requireNonNull(loxClass, "Can not create parent map for null class!");
        List<LoxClass> parents = new ArrayList<>();
        if (loxClass.superclass() != null) {
            do {
                parents.add(loxClass);
                loxClass = loxClass.superclass().get();
            } while (loxClass != null && loxClass.superclass() != null);
        }
        return Util.invert(parents);
    }

    private GeneratedMethodMap createExposed() {
        Map<String, DataMethodContainer> map = new HashMap<>();
        for (Pair<LoxClass, AbstractMethodMap> pair : lookup) {
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

    public int getMethodOrdinal(String name, List<ClassReference> args) {
        return exposed.getMethodOrdinal(name, args);
    }

    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return exposed.getMethodByOrdinal(name, ordinal);
    }
}
