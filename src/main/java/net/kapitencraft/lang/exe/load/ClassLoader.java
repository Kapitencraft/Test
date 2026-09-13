package net.kapitencraft.lang.exe.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.PackageHolder;
import net.kapitencraft.lang.exe.Disassembler;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.VirtualMachine;
import net.kapitencraft.lang.exe.test.VMTestLoader;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClassLoader {

    public static final File cacheLoc = new File("./run/cache");

    public static void main(String[] args) throws IOException {
        loadClasses();
        System.out.println("Loading complete.");
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        String line = "";
        boolean profiling = false;
        while (!"exit".equals(line)) {
            if (line != null) {
                if (line.startsWith("run ")) {
                    String data = line.substring(4);
                    String classRef;
                    if (data.contains(" ")) classRef = data.substring(0, data.indexOf(' '));
                    else classRef = data;
                    ClassReference target = VarTypeManager.getClassForName(classRef);
                    if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
                    else {
                        if (data.contains(" ")) data = data.substring(data.indexOf(' ') + 1);
                        else data = "";
                        VirtualMachine.runMainMethod(target.get(), data, profiling, true);
                    }
                } else if (line.startsWith("profiler")) {
                    if (line.length() < 9) {
                        System.err.println("missing parameter for profiler command. allowed values:");
                        System.err.println("\tstart\n\tend\n\ttoggle");
                    }
                    switch (line.substring(9)) {
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
                } else if (line.startsWith("debug")) {
                    VirtualMachine.DEBUG = switch (line.substring(6)) {
                        case "operations" -> VirtualMachine.DebugType.OPERATIONS;
                        case "stack" -> VirtualMachine.DebugType.STACK;
                        default -> VirtualMachine.DebugType.NONE;
                    };
                    System.out.println("set debug mode to " + VirtualMachine.DEBUG);
                } else if (line.startsWith("test")) {
                    VMTestLoader.run();
                } else if (line.startsWith("help")) {
                    System.out.println("== HELP ==");
                    System.out.println("\texit                        - Ends the Program");
                    System.out.println("\tprofiler [start|end|toggle] - Runs the appropriate profiler action");
                    System.out.println("\trun <ClassPath>             - Executes the 'main(String[])' method of that class");
                    System.out.println("\tdebug                       - toggles debug log for the VM");
                    System.out.println("\ttest                        - Runs the benchmark test");
                    System.out.println("\tlist                        - Lists all methods of the given class and their content");
                } else if (line.startsWith("list ")) {
                    String classRef = line.substring(5);
                    list(classRef);
                } else if (!line.isEmpty()) System.err.println("unknown command: \"" + line + "\"");
            }
            line = scanner.next();
        }
    }

    private static void list(String classRef) {
        if ("$all".equals(classRef)) {
            VarTypeManager.listFlat();
        } else {
            ClassReference target = VarTypeManager.getClassForName(classRef);
            if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
            else {
                ScriptedClass scriptedClass = target.get();
                System.out.println("==== Info ====");
                System.out.println("Name:    " + scriptedClass.name());
                System.out.println("Package: " + scriptedClass.pck());
                System.out.println("\n=== Methods ===");
                Map<String, DataMethodContainer> methods = scriptedClass.getMethods().asMap();
                methods.forEach((string, dataMethodContainer) -> {
                    for (ScriptedCallable method : dataMethodContainer.methods()) {
                        String name = string + "(" + VarTypeManager.getArgsSignature(method.argTypes()) + ")" + VarTypeManager.getClassName(method.retType().get());
                        if (method.isNative()) {
                            System.out.println("== " + name + " ==");
                            System.out.println("<Native>");
                        } else {
                            Disassembler.disassemble(method.getChunk(), name);
                        }
                        System.out.println();
                    }
                });
                System.out.println("==== Info End ====");
            }
        }
    }

    public static void loadClasses() {
        PackageHolder<VMLoaderHolder> pckSkeleton = PackageHolder.load(cacheLoc, ".scrc", VMLoaderHolder::new);
        pckSkeleton.useClasses((classes, pck) -> classes.forEach((name, vmLoaderHolder) -> loadHolderReference(pck, vmLoaderHolder)), VarTypeManager.rootPackage());
        generateSkeletons(pckSkeleton);
        generateClasses(pckSkeleton);
    }

    private static void loadHolderReference(Package pck, VMLoaderHolder holder) {
        pck.addClass(holder.name, holder.reference);
    }

    public static void generateSkeletons(PackageHolder<?> root) {
        root.useClasses((classes, pck) -> classes.forEach((s, classLoaderHolder) -> classLoaderHolder.applySkeleton()), VarTypeManager.rootPackage());
    }

    public static void generateClasses(PackageHolder<VMLoaderHolder> root) {
        record Entry(VMLoaderHolder holder, String name, Package pck) {
        }

        List<Entry> entries = new ArrayList<>();

        root.useClasses((classes, pck) -> classes.forEach((name, holder1) -> entries.add(new Entry(holder1, name, pck))), VarTypeManager.rootPackage());

        //elements are sorted so that the first elements have only native or no parent to ensure classes
        // with in-code parents loading after their parent and its methods
        Comparator<Entry> sortFunction = (o1, o2) -> {
            ScriptedClass o1Class = o1.holder.reference.get();
            ScriptedClass o2Class = o2.holder.reference.get();
            return o1Class.isChildOf(o2Class) ? 1 : o2Class.isChildOf(o1Class) ? -1 : o1.name.compareTo(o2.name);
        };

        Entry[] values = entries.toArray(new Entry[0]);
        Arrays.sort(values, sortFunction);

        for (Entry entry : values) {
            entry.pck.addNullableClass(entry.name, entry.holder.loadClass());
        }
    }

    public static ClassReference loadClassReference(JsonObject object, String elementName) {
        return VarTypeManager.getClassOrError(GsonHelper.getAsString(object, elementName));
    }

    public static String[] loadInterfaces(JsonObject data) {
        return GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }

    public static String pck(File file) {
        String path = file.getPath().replace(cacheLoc.getPath(), "").replace(".scrc", "");
        List<String> pckData = new ArrayList<>(List.of(path.split("\\\\")));
        pckData = pckData.subList(1, pckData.size() - 1);
        return String.join(".", pckData);
    }
}
