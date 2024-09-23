package net.kapitencraft.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GenerateBuilders {
    public static final String DIRECTORY = "src/main/java/net/kapitencraft/lang/holder/builder";
    public static final int maxParams = 10;

    public static void main(String[] args) throws IOException {
        defineBuilder("MB");

    }

    public static void defineBuilder(String baseName) throws IOException {
        String path = DIRECTORY + "/" + baseName + ".java";
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

    }

    public static void printGenerics(PrintWriter writer, int min, int max) {
        writer.print("<");
        for (int i = min; i <= max; i++) {
            writer.print("P");
            writer.print(i);
            if (i != max) writer.print(", ");
        }
        writer.print(">");
    }
}
