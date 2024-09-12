package net.kapitencraft.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        defineAst("src/main/java/net/kapitencraft/lang/ast", "Expr", Arrays.asList(
                "Assign        : Token name; Expr value; Token type",
                "SpecialAssign : Token name; Token type",
                "Binary        : Expr left; Token operator; Expr right",
                "Call          : Expr callee; Token paren; List<Expr> arguments",
                "Grouping      : Expr expression",
                //"Lambda   : List<Token> params, Stmt body",
                "Literal       : Object value",
                "Logical       : Expr left; Token operator; Expr right",
                "Unary         : Token operator; Expr right",
                "Variable      : Token name",
                "Function      : Token name"
        ));
        defineAst("src/main/java/net/kapitencraft/lang/ast", "Stmt", Arrays.asList(
                "Block            : List<Stmt> statements",
                "Expression       : Expr expression",
                "Function         : Token retType; Token name; List<Pair<Token,Token>> params; List<Stmt> body",
                "If               : Expr condition; Stmt thenBranch; Stmt elseBranch",
                "Return           : Token keyword; Expr value",
                "Var              : Token name; Token type; Expr initializer",
                "While            : Expr condition; Stmt body",
                "For              : Stmt init; Expr condition; Expr increment; Stmt body",
                "LoopInterruption : Token type"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package net.kapitencraft.lang.ast;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("import net.kapitencraft.lang.ast.token.Token;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        writer.println();

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println();
        writer.println("    public static class " + className + " extends " +
                baseName + " {");

        // Fields.
        String[] fields = fieldList.split("; ");
        for (String field : fields) {
            writer.println("        public final " + field + ";");
        }
        writer.println();


        // Constructor.
        writer.print("        public " + className + "(");
        for (int i = 0; i < fields.length - 1; i++) {
            writer.print(fields[i]);
            writer.print(", ");
        }
        writer.print(fields[fields.length - 1]);
        writer.println(") {");

        // Store parameters in fields.
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");


        // Visitor pattern.
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");

    }
}