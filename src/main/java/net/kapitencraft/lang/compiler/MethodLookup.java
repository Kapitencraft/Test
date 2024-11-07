package net.kapitencraft.lang.compiler;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.load.CompilerHolder;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MethodLookup {
    private final List<Pair<LoxClass, MethodMap>> lookup;
    private final MethodMap exposed; //TODO make a new one for method lookup k

    public MethodLookup(List<Pair<LoxClass, MethodMap>> lookup) {
        this.lookup = lookup;
        exposed = this.createExposed();
    }

    public void checkFinal(Compiler.ErrorLogger logger, Pair<Token, GeneratedCallable>[] map) {
        for (Pair<Token, GeneratedCallable> pair : map) {
            for (Pair<LoxClass, MethodMap> lookupElement : lookup) {
                Map<String, DataMethodContainer> methodMap = lookupElement.right().asMap();
                for (ScriptedCallable method : methodMap.get(pair.left().lexeme()).getMethods()) {
                    if (!method.isFinal()) continue;
                    if (Util.matchArgs(method.argTypes(), pair.right().argTypes())) {
                        logger.errorF(pair.left(), "method '%s' can not override final method from class '%s'");
                    }
                }
            }
        }
    }

    public void checkAbstract(Compiler.ErrorLogger logger, Token className, Pair<Token, GeneratedCallable>[] map) {
        Map<String, List<Pair<LoxClass, ScriptedCallable>>> abstracts = new HashMap<>();
        for (Pair<LoxClass, MethodMap> methods : lookup) {
            methods.right().asMap().forEach((s, dataMethodContainer) -> {
                a: for (ScriptedCallable method : dataMethodContainer.getMethods()) {
                    List<Pair<LoxClass, ScriptedCallable>> classData = abstracts.get(s);
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
            for (int i = 0; i < methods.size(); i++) {
                if (Util.matchArgs(pair.right().argTypes(), methods.get(i).right().argTypes())) {
                    methods.remove(i);
                }
            }
        }
        abstracts.forEach((string, pairs) -> {
            pairs.forEach(pair -> {
                if (pair.left().isInterface())
                    logger.errorF(className, "class %s must either be declared abstract or override method '%s' from interface %s", className.lexeme(), string, pair.left().name());
            });
        });
    }

    public static MethodLookup createFromClass(LoxClass loxClass) {
        List<LoxClass> parentMap = createParentMap(loxClass);
        List<LoxClass> allParents = new ArrayList<>();
        for (LoxClass parent : parentMap) {
            addInterfaces(parent, allParents::add);
        }
        List<Pair<LoxClass, MethodMap>> lookup = allParents.stream().collect(Util.toPairList(Function.identity(), LoxClass::getMethods));
        return new MethodLookup(lookup);
    }

    private static void addInterfaces(LoxClass target, Consumer<LoxClass> sink) {
        if (target.interfaces() != null) for (LoxClass anInterface : target.interfaces()) {
            addInterfaces(anInterface, sink);
            sink.accept(anInterface);
        }
    }

    private static List<LoxClass> createParentMap(LoxClass loxClass) {
        List<LoxClass> parents = new ArrayList<>();
        do {
            parents.add(loxClass);
            loxClass = loxClass.superclass();
        } while (loxClass != null);
        return Util.invert(parents);
    }

    private MethodMap createExposed() {
        Map<String, DataMethodContainer> map = new HashMap<>();
        for (Pair<LoxClass, MethodMap> pair : lookup) {
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
        return new MethodMap(ImmutableMap.copyOf(map));
    }

    public int getMethodOrdinal(String name, List<? extends LoxClass> args) {
        return exposed.getMethodOrdinal(name, args);
    }

    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return exposed.getMethodByOrdinal(name, ordinal);
    }
}
