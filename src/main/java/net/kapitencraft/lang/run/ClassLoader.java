package net.kapitencraft.lang.run;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.GeneratedLoxClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.SkeletonClass;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassLoader {
    public static final File cacheLoc = new File("./run/cache");

    public static void main(String[] args) {
        PackageHolder pckSkeleton = load(cacheLoc, ".scrc");
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

    public static PackageHolder load(File fileLoc, String end) {
        PackageHolder root = new PackageHolder();
        List<Pair<File, PackageHolder>> pckLoader = new ArrayList<>();
        pckLoader.add(Pair.of(fileLoc, root));
        while (!pckLoader.isEmpty()) {
            Pair<File, PackageHolder> pck = pckLoader.get(0);
            File file = pck.left();
            PackageHolder holder = pck.right();
            File[] files = file.listFiles();
            if (files == null) continue;
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
                    holder.classes.put(name, enclosedClassLoader.toHolder())
            );
            pckLoader.remove(0);
        }
        return root;
    }

    public static void applyPreviews(PackageHolder root) {
        useClasses(root, (classes, pck) -> classes.forEach((name, classHolder) -> pck.addClass(name, classHolder.previewClass)));
    }

    private static void generateSkeletons(PackageHolder root) {
        useClasses(root, (classes, pck) -> classes.forEach((s, classHolder) -> pck.addClass(s, classHolder.createSkeleton())));
    }

    private static void generateClasses(PackageHolder root) {
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

    public static GeneratedLoxClass load(JsonObject object, List<GeneratedLoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(object, "name");
        LoxClass superclass = loadClassReference(object, "superclass");
        ImmutableMap.Builder<String, DataMethodContainer> methods = new ImmutableMap.Builder<>();
        {
            JsonObject methodData = GsonHelper.getAsJsonObject(object, "methods");
            methodData.asMap().forEach((name1, element) -> {
                try {
                    DataMethodContainer container = new DataMethodContainer(element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).toArray(LoxCallable[]::new));
                    methods.put(name1, container);
                } catch (Exception e) {
                    System.err.printf("error loading method '%s' inside class '%s': %s%n", name1, name, e.getMessage());
                }
            });
        }
        ImmutableMap.Builder<String, DataMethodContainer> staticMethods = new ImmutableMap.Builder<>();
        {
            JsonObject methodData = GsonHelper.getAsJsonObject(object, "staticMethods");
            methodData.asMap().forEach((name1, element) -> {
                try {
                    DataMethodContainer container = new DataMethodContainer(element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).toArray(LoxCallable[]::new));
                    staticMethods.put(name1, container);
                } catch (Exception e) {
                    System.err.printf("error loading static method '%s' inside class '%s': %s%n", name1, name, e.getMessage());
                }
            });
        }
        List<LoxCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(object, "constructor").asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).forEach(constructorData::add);

        ImmutableMap.Builder<String, GeneratedField> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(object, "fields");
            fieldData.asMap().forEach((name1, element) -> {
                fields.put(name1, GeneratedField.fromJson(element.getAsJsonObject()));
            });
        }

        ImmutableMap.Builder<String, GeneratedField> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(object, "staticFields");
            fieldData.asMap().forEach((name1, element) -> {
                staticFields.put(name1, GeneratedField.fromJson(element.getAsJsonObject()));
            });
        }

        List<String> flags = GsonHelper.getAsJsonArray(object, "flags").asList().stream().map(JsonElement::getAsString).toList();

        Map<String, LoxClass> enclosedClasses = enclosed.stream().collect(Collectors.toMap(GeneratedLoxClass::name, Function.identity()));

        return new GeneratedLoxClass(
                methods.build(), staticMethods.build(), constructorData,
                fields.build(), staticFields.build(),
                superclass, name, pck,
                enclosedClasses,
                flags.contains("isAbstract"), flags.contains("isFinal")
        );
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

    private static String pck(File file) {
        String path = file.getPath().replace(cacheLoc.getPath(), "").replace(".scrc", "");
        List<String> pckData = new ArrayList<>(List.of(path.split("\\\\")));
        pckData = pckData.subList(1, pckData.size()-1);
        return String.join(".", pckData);
    }

    public static final class ClassHolder {
        private final File file;
        private final ClassHolder[] children;
        private final PreviewClass previewClass;

        public ClassHolder(File file, ClassHolder[] children) {
            this.file = file;
            this.children = children;
            String[] enclosed = file.getName().replace(".scrc", "").split("\\$");
            this.previewClass = new PreviewClass(enclosed[enclosed.length-1]);
        }

            private String pck() {
                return ClassLoader.pck(file);
            }

            public SkeletonClass createSkeleton() {
                List<PreviewClass> enclosed = Arrays.stream(children).map(classHolder -> {
                    classHolder.previewClass.apply(classHolder.createSkeleton());
                    return classHolder.previewClass;
                }).toList();
                try {
                    return SkeletonClass.fromCache(Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject(), pck(),
                            enclosed.toArray(new PreviewClass[0])
                    );
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException("unable to find file: " + file);
                }
            }

            public GeneratedLoxClass loadClass() {
                List<GeneratedLoxClass> enclosed = Arrays.stream(children).map(ClassHolder::loadClass).toList();
                try {
                    GeneratedLoxClass target = ClassLoader.load(Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject(), enclosed, pck());
                    this.previewClass.apply(target);
                    return target;
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException("unable to find file: " + file);
                }
            }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ClassHolder) obj;
            return Objects.equals(this.file, that.file) &&
                    Arrays.equals(this.children, that.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, Arrays.hashCode(children));
        }

        @Override
        public String toString() {
            return "ClassHolder[" +
                    "file=" + file + ", " +
                    "children=" + Arrays.toString(children) + ']';
        }

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

        public ClassHolder toHolder() {
            return new ClassHolder(file, enclosed.values()
                    .stream()
                    .map(EnclosedClassLoader::toHolder)
                    .toArray(ClassHolder[]::new)
            );
        }
    }
}
