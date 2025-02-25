package net.kapitencraft.lang.run.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ClassLoader {
    public static final File cacheLoc = new File("./run/cache");

    public static void main(String[] args) throws IOException {
        loadClasses();
        System.out.println("Loading complete.");
        Interpreter interpreter = Interpreter.INSTANCE;
        Scanner scanner = new Scanner(System.in);
        //testClass(interpreter, "test.LoopTest", "a b c d e f");
        //executeTest(interpreter);
        String line = "";
        boolean profiling = false;
        while (!"!exit".equals(line)) {
            if (line != null) {
                if (line.startsWith("!run ")) {
                    String data = line.substring(5);
                    String classRef;
                    if (data.contains(" ")) classRef = data.substring(0, data.indexOf(' '));
                    else classRef = data;
                    ClassReference target = VarTypeManager.getClassForName(classRef);
                    if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
                    else {
                        if (data.contains(" ")) data = data.substring(data.indexOf(' ') + 1);
                        else data = "";
                        interpreter.runMainMethod(target.get(), data, profiling, true);
                    }
                } else if ("!test".equals(line)) executeTest(interpreter);
                else if (line.startsWith("!profiler ")) {
                    switch (line.substring(10)) {
                        case "start" -> {
                            profiling = true;
                            System.out.println("started profiler");
                        }
                        case "end" -> {
                            profiling = false;
                            System.out.println("stopped profiler");
                        }
                        case "toggle" -> {
                            profiling = !profiling;
                            System.out.println("toggled profiler. now: " + profiling);
                        }
                        default -> System.err.println("unknown profiler operation : \"" + line.substring(10) + "\"");
                    }
                } else if (!line.isEmpty()) System.err.println("unknown command: \"" + line + "\"");
            }
            line = scanner.nextLine();
        }
    }

    public static void loadClasses() {
        PackageHolder<VMLoaderHolder> pckSkeleton = load(cacheLoc, ".scrc", VMLoaderHolder::new);
        useClasses(pckSkeleton, (classes, pck) -> classes.forEach((name, vmLoaderHolder) -> {
            loadHolderReference(pck, vmLoaderHolder);
        }));
        generateSkeletons(pckSkeleton);
        generateClasses(pckSkeleton);
    }

    private static void executeTest(Interpreter interpreter) {
        testClass(interpreter, "test.ArrayTest", "a b c d e f g h");
        testClass(interpreter, "test.EnumTest", "");
        testClass(interpreter, "test.InheritanceTest", "");
        testClass(interpreter, "test.IntersectionTest", "");
        testClass(interpreter, "test.LoopTest", "a b c d e f g h");
        testClass(interpreter, "test.MultipleMethodsTest", "");
        testClass(interpreter, "test.TimeTest", "");
        //testClass(interpreter, "test.OverflowTest", "");
        //testClass(interpreter, "test.ThrowTest", "");
    }

    private static void testClass(Interpreter interpreter, String className, String data) {
        try {
            System.out.println("testing '" + className + "':");
            interpreter.runMainMethod(VarTypeManager.getClassForName(className).get(), data, false, true);
        } catch (NullPointerException e) {
            System.out.println("could not find class: '" + className + "'");
        }
    }

    private static void loadHolderReference(Package pck, VMLoaderHolder holder) {
        pck.addClass(holder.name, holder.reference);
        if (holder.children.length > 0) {
            Package internal = pck.getOrCreatePackage(holder.name);
            for (VMLoaderHolder child : ((VMLoaderHolder[]) holder.children)) {
                loadHolderReference(internal, child);
            }
        }
    }

    public static <T extends ClassLoaderHolder> PackageHolder<T> load(File fileLoc, String end, BiFunction<File, List<T>, T> constructor) {
        PackageHolder<T> root = new PackageHolder<>();
        List<Pair<File, PackageHolder<T>>> pckLoader = new ArrayList<>();
        pckLoader.add(Pair.of(fileLoc, root));
        while (!pckLoader.isEmpty()) {
            Pair<File, PackageHolder<T>> pck = pckLoader.get(0);
            File file = pck.left();
            PackageHolder<T> holder = pck.right();
            File[] files = file.listFiles();
            if (files == null) {
                pckLoader.remove(0);
                continue;
            }
            Map<String, EnclosedClassLoader<T>> enclosedLoaderMap = new HashMap<>();
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    PackageHolder<T> child = new PackageHolder<>();
                    holder.packages.put(file1.getName(), child);
                    pckLoader.add(Pair.of(file1, child));
                } else {
                    String name = file1.getName().replace(end, "");
                    String[] enclosedSplit = name.split("\\$");
                    if (enclosedSplit.length > 1) {
                        if (!enclosedLoaderMap.containsKey(enclosedSplit[0])) {
                            enclosedLoaderMap.put(enclosedSplit[0], new EnclosedClassLoader<>());
                        }
                        EnclosedClassLoader<T> loader = enclosedLoaderMap.get(enclosedSplit[0]);
                        for (int i = 1; i < enclosedSplit.length; i++) {
                            loader = loader.addOrGetEnclosed(enclosedSplit[i]);
                        }
                        loader.applyFile(file1);
                    } else {
                        if (!enclosedLoaderMap.containsKey(name)) enclosedLoaderMap.put(name, new EnclosedClassLoader<>());
                        enclosedLoaderMap.get(name).applyFile(file1);
                    }
                }
            }
            enclosedLoaderMap.forEach((name, enclosedClassLoader) ->
                    holder.classes.put(name, enclosedClassLoader.toHolder(constructor))
            );
            pckLoader.remove(0);
        }
        return root;
    }

    public static void generateSkeletons(PackageHolder<?> root) {
        useClasses(root, (classes, pck) -> classes.forEach((s, classLoaderHolder) -> classLoaderHolder.applySkeleton()));
    }

    public static void generateClasses(PackageHolder<?> root) {
        useClasses(root, (classes, pck) -> classes.forEach((name, holder1) -> pck.addNullableClass(name, holder1.loadClass())));
    }


    //how should I name this...
    public static <T extends ClassLoaderHolder> void useClasses(PackageHolder<T> root, BiConsumer<Map<String, T>, Package> consumer) {
        List<Pair<PackageHolder<T>, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder<T>, Package> data = packageData.get(0);
            PackageHolder<T> holder = data.left();
            Package pck = data.right();
            consumer.accept(holder.classes, pck);
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
    }

    public static <T extends ClassLoaderHolder> void useHolders(PackageHolder<T> root, BiConsumer<String, T> consumer) {
        List<Pair<PackageHolder<T>, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder<T>, Package> data = packageData.get(0);
            PackageHolder<T> holder = data.left();
            Package pck = data.right();
            holder.classes.forEach(consumer);
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
    }

    public static ClassReference loadClassReference(JsonObject object, String elementName) {
        return VarTypeManager.getClassOrError(GsonHelper.getAsString(object, elementName));
    }

    public static ClassReference[] loadInterfaces(JsonObject data) {
        return GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassOrError).toArray(ClassReference[]::new);
    }

    public static class PackageHolder<T extends ClassLoaderHolder> {
        private final Map<String, PackageHolder<T>> packages = new HashMap<>();
        private final Map<String, T> classes = new HashMap<>();

    }

    public static String pck(File file) {
        String path = file.getPath().replace(cacheLoc.getPath(), "").replace(".scrc", "");
        List<String> pckData = new ArrayList<>(List.of(path.split("\\\\")));
        pckData = pckData.subList(1, pckData.size()-1);
        return String.join(".", pckData);
    }


    private static class EnclosedClassLoader<T extends ClassLoaderHolder> {
        private File file;
        private final Map<String, EnclosedClassLoader<T>> enclosed = new HashMap<>();

        public void applyFile(File file) {
            this.file = file;
        }

        public EnclosedClassLoader<T> addEnclosed(String name) {
            EnclosedClassLoader<T> loader = new EnclosedClassLoader<>();
            enclosed.put(name, loader);
            return loader;
        }

        public EnclosedClassLoader<T> addOrGetEnclosed(String s) {
            if (enclosed.containsKey(s)) return enclosed.get(s);
            return addEnclosed(s);
        }

        public T toHolder(BiFunction<File, List<T>, T> constructor) {
            return constructor.apply(file,
                    enclosed.values().stream()
                    .map(enclosedClassLoader -> enclosedClassLoader.toHolder(constructor))
                    .toList()
            );
        }
    }
}
