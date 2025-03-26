package net.kapitencraft.lang.tool;

import com.google.gson.*;
import net.kapitencraft.tool.GsonHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
            Map<String, Map<String, TypeDef>> valueData = createValues(data.get("values"));
            defineAst(astType, valueData, imports, defaultImports);
        });

        //defineAstFile("Expr", List.of(
        //        "Assign        : Token name; Expr value; Token type; ClassReference executor; Operand operand",
        //        "SpecialAssign : Token name; Token assignType",
        //        "Binary        : Expr left; Token operator; ClassReference executor; Operand operand; Expr right",
        //        "When          : Expr condition; Expr ifTrue; Expr ifFalse",
        //        "InstCall      : Expr callee; Token name; int methodOrdinal; List<Expr> args",
        //        "StaticCall    : ClassReference target; Token name; int methodOrdinal; List<Expr> args",
        //        "Get           : Expr object; Token name",
        //        "StaticGet     : ClassReference target; Token name",
        //        "ArrayGet      : Expr object; Expr index",
        //        "Set           : Expr object; Token name; Expr value; Token assignType; ClassReference executor; Operand operand",
        //        "StaticSet     : ClassReference target; Token name; Expr value; Token assignType; ClassReference executor; Operand operand",
        //        "ArraySet      : Expr object; Expr index; Expr value; Token assignType; ClassReference executor; Operand operand",
        //        "SpecialSet    : Expr callee; Token name; Token assignType",
        //        "StaticSpecial : ClassReference target; Token name; Token assignType",
        //        "ArraySpecial  : Expr object; Expr index; Token assignType",
        //        "Slice         : Expr object; Expr start; Expr end; Expr interval",
        //        "Switch        : Expr provider; Map<Object,Expr> params; Expr defaulted; Token keyword",
        //        "CastCheck     : Expr object; ClassReference targetType; Token patternVarName",
        //        "Grouping      : Expr expression",
        //        //"Lambda   : List<Token> params, Stmt body",
        //        "Literal       : Token token",
        //        "Logical       : Expr left; Token operator; Expr right",
        //        "Unary         : Token operator; Expr right",
        //        "VarRef        : Token name",
        //        "Constructor   : Token keyword; ClassReference target; List<Expr> params; int ordinal"
        //), List.of(
        //        "net.kapitencraft.lang.run.algebra.Operand",
        //        "java.util.Map",
        //        "net.kapitencraft.lang.holder.LiteralHolder"
        //));
        //defineAstFile("Stmt",
        //        List.of(
        //        "Block            : List<Stmt> statements",
        //        "Expression       : Expr expression",
        //        "If               : Expr condition; Stmt thenBranch; Stmt elseBranch; List<Pair<Expr,Stmt>> elifs; Token keyword",
        //        "Return           : Token keyword; Expr value",
        //        "Throw            : Token keyword; Expr value",
        //        "VarDecl          : Token name; ClassReference type; Expr initializer; boolean isFinal",
        //        "While            : Expr condition; Stmt body; Token keyword",
        //        "For              : Stmt init; Expr condition; Expr increment; Stmt body; Token keyword",
        //        "ForEach          : ClassReference type; Token name; Expr initializer; Stmt body",
        //        "LoopInterruption : Token type",
        //        "Try              : Block body; List<Pair<Pair<List<ClassReference>,Token>,Block>> catches; Block finale"
        //), List.of(
        //        "net.kapitencraft.tool.Pair"
        //));
    }

    private static Map<String, Map<String, TypeDef>> createValues(JsonElement valuesData) {
        JsonObject object = valuesData.getAsJsonObject();
        Map<String, Map<String, TypeDef>> values = new HashMap<>();
        object.asMap().forEach((s, element) -> {
            Map<String, JsonElement> valueData = element.getAsJsonObject().asMap();
            Map<String, TypeDef> value = new HashMap<>();
            valueData.forEach((s1, element1) ->
                    value.put(s1, TypeDef.fromJsonElement(element1)));
            values.put(s, value);
        });
        return values;
    }

    private static Map<String, Map<String, JsonElement>> createObj(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        Map<String, Map<String, JsonElement>> data = new HashMap<>();
        object.asMap().forEach((s, typeData) ->
                data.put(s, typeData.getAsJsonObject().asMap()));
        return data;
    }

    private static void defineAst(String baseName, Map<String, Map<String, TypeDef>> data, Imports imports, Imports defaultImports) {
        defineAstFile(baseName, EnvironmentType.RUNTIME, data, imports, defaultImports);
        defineAstFile(baseName, EnvironmentType.COMPILE, data, imports, defaultImports);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void defineAstFile(String baseName, EnvironmentType type, Map<String, Map<String, TypeDef>> data, Imports imports, Imports defaultImports) {
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

    private static void defineType(PrintWriter writer, String baseName, String extendedName, String typeId, Map<String, TypeDef> types, EnvironmentType type) {
        writer.println();
        writer.println("    public static class " + typeId + " extends " +
                extendedName + " {");

        // Fields.
        types.forEach((s, typeDef) ->
                writer.println("        public final " + typeDef.get(type) + " " + s + ";"));
        writer.println();


        // Constructor.
        writer.print("        public " + typeId + "(");
        writer.print(
                types
                .keySet()
                .stream()
                .map(s -> types.get(s).get(type) + " " + s)
                .collect(Collectors.joining(", "))
        );
        writer.println(") {");

        // Store params in fields.
        for (String name : types.keySet()) {
            writer.println("            this." + name + " = " + name + ";");
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
                    runtime.replaceAll("Expr", "RuntimeExpr").replaceAll("Stmt", "RuntimeStmt").replaceAll("Token", "RuntimeToken"),
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
}