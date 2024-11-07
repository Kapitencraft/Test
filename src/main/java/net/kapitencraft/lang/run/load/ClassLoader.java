package net.kapitencraft.lang.run.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.oop.clazz.*;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ClassLoader {
    public static final File cacheLoc = new File("./run/cache");

    public static void main(String[] args) {
        PackageHolder pckSkeleton = load(cacheLoc, ".scrc", VMHolder::new);
        applyPreviews(pckSkeleton);
        generateSkeletons(pckSkeleton);
        generateClasses(pckSkeleton);
        System.out.println("Loading complete.");
        Interpreter interpreter = Interpreter.INSTANCE;
        Scanner scanner = Interpreter.in;
        String line = "";
        while (!line.equals("!exit")) {
            if (line.startsWith("!run ")) {
                String data = line.substring(5);
                String classRef;
                if (data.contains(" ")) classRef = data.substring(0, data.indexOf(' '));
                else classRef = data;
                LoxClass target = VarTypeManager.getClassForName(classRef);
                if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
                else {
                    interpreter.runMainMethod(target, data.substring(data.indexOf(' ') + 1));
                }
            }
            line = scanner.nextLine();
        }
    }

    public static <T extends ClassHolder> PackageHolder load(File fileLoc, String end, BiFunction<File, ClassHolder[], T> constructor) {
        PackageHolder root = new PackageHolder();
        List<Pair<File, PackageHolder>> pckLoader = new ArrayList<>();
        pckLoader.add(Pair.of(fileLoc, root));
        while (!pckLoader.isEmpty()) {
            Pair<File, PackageHolder> pck = pckLoader.get(0);
            File file = pck.left();
            PackageHolder holder = pck.right();
            File[] files = file.listFiles();
            if (files == null) {
                pckLoader.remove(0);
                continue;
            }
            Map<String, EnclosedClassLoader> enclosedLoaderMap = new HashMap<>();
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    PackageHolder child = new PackageHolder();
                    holder.packages.put(file1.getName(), child);
                    pckLoader.add(Pair.of(file1, child));
                } else {
                    String name = file1.getName().replace(end, "");
                    String[] enclosedSplit = name.split("\\$");
                    if (enclosedSplit.length > 1) {
                        if (!enclosedLoaderMap.containsKey(enclosedSplit[0])) {
                            enclosedLoaderMap.put(enclosedSplit[0], new EnclosedClassLoader());
                        }
                        EnclosedClassLoader loader = enclosedLoaderMap.get(enclosedSplit[0]);
                        for (int i = 1; i < enclosedSplit.length; i++) {
                            loader = loader.addOrGetEnclosed(enclosedSplit[i]);
                        }
                        loader.applyFile(file1);
                    } else {
                        if (!enclosedLoaderMap.containsKey(name)) enclosedLoaderMap.put(name, new EnclosedClassLoader());
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

    public static void applyPreviews(PackageHolder root) {
        useClasses(root, (classes, pck) -> classes.forEach((name, classHolder) -> pck.addClass(name, classHolder.previewClass)));
    }

    public static void generateSkeletons(PackageHolder root) {
        useClasses(root, (classes, pck) -> classes.forEach((s, classHolder) -> pck.addClass(s, classHolder.createSkeleton())));
    }

    public static void generateClasses(PackageHolder root) {
        useClasses(root, (classes, pck) -> classes.forEach((name, holder1) -> pck.addClass(name, holder1.loadClass())));
    }


    //how should I name this...
    public static void useClasses(PackageHolder root, BiConsumer<Map<String, ClassHolder>, Package> consumer) {
        List<Pair<PackageHolder, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder, Package> data = packageData.get(0);
            PackageHolder holder = data.left();
            Package pck = data.right();
            consumer.accept(holder.classes, pck);
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
    }

    public static void useHolders(PackageHolder root, BiConsumer<String, ClassHolder> consumer) {
        List<Pair<PackageHolder, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder, Package> data = packageData.get(0);
            PackageHolder holder = data.left();
            Package pck = data.right();
            holder.classes.forEach(consumer);
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
    }

    public static LoxClass loadClassReference(JsonObject object, String elementName) {
        return VarTypeManager.getClassForName(GsonHelper.getAsString(object, elementName));
    }

    public static List<String> readFlags(JsonObject data) {
        return GsonHelper.getAsJsonArray(data, "flags").asList().stream().map(JsonElement::getAsString).toList();
    }

    public static class PackageHolder {
        private final Map<String, PackageHolder> packages = new HashMap<>();
        private final Map<String, ClassHolder> classes = new HashMap<>();

    }

    public static String pck(File file) {
        String path = file.getPath().replace(cacheLoc.getPath(), "").replace(".scrc", "");
        List<String> pckData = new ArrayList<>(List.of(path.split("\\\\")));
        pckData = pckData.subList(1, pckData.size()-1);
        return String.join(".", pckData);
    }


    private static class EnclosedClassLoader {
        private File file;
        private final Map<String, EnclosedClassLoader> enclosed = new HashMap<>();

        public void applyFile(File file) {
            this.file = file;
        }

        public EnclosedClassLoader addEnclosed(String name) {
            EnclosedClassLoader loader = new EnclosedClassLoader();
            enclosed.put(name, loader);
            return loader;
        }

        public EnclosedClassLoader addOrGetEnclosed(String s) {
            if (enclosed.containsKey(s)) return enclosed.get(s);
            return addEnclosed(s);
        }

        public <T extends ClassHolder> T toHolder(BiFunction<File, ClassHolder[], T> constructor) {
            return constructor.apply(file, enclosed.values()
                    .stream()
                    .map(enclosedClassLoader -> enclosedClassLoader.toHolder(constructor))
                    .toArray(ClassHolder[]::new)
            );
        }
    }
}
