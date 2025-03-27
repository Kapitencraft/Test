package net.kapitencraft.lang.tool;

import com.google.gson.*;
import net.kapitencraft.tool.GsonHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateAst {
    public static final String DIRECTORY = "src/main/java/net/kapitencraft/lang/holder/ast";
    private static final String SOURCE = "src/generate_ast.json";

    public static void main(String[] args) throws IOException {
        JsonObject object = GsonHelper.GSON.fromJson(new FileReader(SOURCE), JsonObject.class);
        Imports defaultImports = Imports.fromJsonElement(object.get("imports"));

        Map<String, Map<String, JsonElement>> obj = createObj(object.get("values"));
        obj.forEach((astType, data) -> {
            Imports imports = Imports.fromJsonElement(data.get("imports"));
            Map<String, AstDef> valueData = createValues(data.get("values"));
            defineAst(astType, valueData, imports, defaultImports);
        });
    }

    private static Map<String, AstDef> createValues(JsonElement valuesData) {
        JsonObject object = valuesData.getAsJsonObject();
        Map<String, AstDef> data = new HashMap<>();
        object.asMap().forEach((s, element) -> data.put(s, AstDef.fromJson(element)));
        return data;
    }

    private static Map<String, Map<String, JsonElement>> createObj(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        Map<String, Map<String, JsonElement>> data = new HashMap<>();
        object.asMap().forEach((s, typeData) ->
                data.put(s, typeData.getAsJsonObject().asMap()));
        return data;
    }

    private static void defineAst(String baseName, Map<String, AstDef> data, Imports imports, Imports defaultImports) {
        defineAstFile(baseName, EnvironmentType.RUNTIME, data, imports, defaultImports);
        defineAstFile(baseName, EnvironmentType.COMPILE, data, imports, defaultImports);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void defineAstFile(String baseName, EnvironmentType type, Map<String, AstDef> data, Imports imports, Imports defaultImports) {
        String extendedName = type.getName() + baseName;
        String path = DIRECTORY + "/" + extendedName + ".java";
        PrintWriter writer;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            writer = new PrintWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error defining AST '" + extendedName + "': " + e.getMessage());
            return;
        }

        writer.println("package net.kapitencraft.lang.holder.ast;");
        writer.println();
        for (String s : defaultImports.get(type)) {
            writer.print("import ");
            writer.print(s);
            writer.println(";");
        }
        for (String s : imports.get(type)) {
            writer.print("import ");
            writer.print(s);
            writer.println(";");
        }
        writer.println();
        writer.println("public abstract class " + extendedName + " {");
        writer.println();

        defineVisitor(writer, baseName, data.keySet());

        // The AST classes.
        for (String typeId : data.keySet()) {
            defineType(writer, baseName, extendedName, typeId, data.get(typeId), type);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, Set<String> types) {
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            writer.println("        R visit" + type + baseName + "(" +
                    type + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String extendedName, String typeId, AstDef types, EnvironmentType type) {
        writer.println();
        writer.println("    public static class " + typeId + " extends " +
                extendedName + " {");

        FieldDef[] fields = types.get(type);

        // Fields.
        for (FieldDef field : fields) {
            writer.println("        public final " + field.type.get(type) + " " + field.name + ";");
        }
        writer.println();


        // Constructor.
        writer.print("        public " + typeId + "(");
        writer.print(
                Arrays.stream(fields)
                        .map(f -> f.type.get(type) + " " + f.name)
                        .collect(Collectors.joining(", "))
        );
        writer.println(") {");

        // Store params in fields.
        for (FieldDef field : fields) {
            writer.println("            this." + field.name + " = " + field.name + ";");
        }

        writer.println("        }");


        // Visitor pattern.
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                typeId + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");

    }

    private record TypeDef(String runtime, String compile) {

        private static TypeDef expand(String runtime, String compile) {
            return new TypeDef(
                    runtime.replaceAll("Expr", "RuntimeExpr").replaceAll("Stmt", "RuntimeStmt").replaceAll("Token(?!Type)", "RuntimeToken"),
                    compile.replaceAll("Expr", "CompileExpr").replaceAll("Stmt", "CompileStmt")
            );
        }

        public static TypeDef fromJsonElement(JsonElement element) {
            if (element.isJsonPrimitive()) {
                String val = element.getAsString();
                return expand(val, val);
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                String runtime = object.getAsJsonPrimitive("runtime").getAsString();
                String compile = object.getAsJsonPrimitive("compile").getAsString();
                return expand(runtime, compile);
            }
            throw new JsonParseException("don't know how to turn '" + element + "' into a TypeDef");
        }

        public String get(EnvironmentType type) {
            return type == EnvironmentType.RUNTIME ? runtime : compile;
        }
    }

    private record Imports(String[] runtime, String[] compile) {

        public static Imports fromJsonElement(JsonElement element) {
            if (element.isJsonArray()) {
                String[] val = collectArray(element.getAsJsonArray());
                return new Imports(val, val);
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                String[] runtime = collectArray(object.getAsJsonArray("runtime"));
                String[] compile = collectArray(object.getAsJsonArray("compile"));
                return new Imports(runtime, compile);
            }
            throw new IllegalArgumentException("don't know how to turn '" + element + "' into a TypeDef");
        }

        private static String[] collectArray(JsonArray array) {
            return array.asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
        }

        public String[] get(EnvironmentType type) {
            return type == EnvironmentType.RUNTIME ? runtime : compile;
        }
    }

    private enum EnvironmentType {
        RUNTIME("Runtime"),
        COMPILE("Compile");

        private final String name;

        EnvironmentType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private record FieldDef(String name, TypeDef type) {
        public static FieldDef fromJson(String name, JsonElement value) {
            return new FieldDef(name, TypeDef.fromJsonElement(value));
        }

        public FieldDef trim() {
            return new FieldDef(name.replaceAll("[$%]", ""), type);
        }
    }

    private record AstDef(FieldDef[] compileFields, FieldDef[] runtimeFields) {

        public FieldDef[] get(EnvironmentType type) {
            return type == EnvironmentType.COMPILE ? compileFields : runtimeFields;
        }

        public static AstDef fromJson(JsonElement element) {
            List<FieldDef> compile = new ArrayList<>(), runtime = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                FieldDef def = FieldDef.fromJson(entry.getKey(), entry.getValue());
                if (!entry.getKey().startsWith("$")) {
                    runtime.add(def.trim());
                }
                if (!entry.getKey().startsWith("%")) {
                    compile.add(def.trim());
                }
            }
            return new AstDef(
                    compile.toArray(new FieldDef[0]),
                    runtime.toArray(new FieldDef[0])
            );
        }
    }
}