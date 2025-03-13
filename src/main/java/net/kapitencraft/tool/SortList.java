package net.kapitencraft.tool;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class SortList {

    public static void main(String[] args) throws IOException {
        String out = new BufferedReader(new FileReader("G:/Klasse 11b/Informatik/3 - Miniprojekte/Erasmus & Jendrik/wortliste.txt"))
                .lines()
                .map(s -> s.replace("ä", "ae").replace("Ä", "Ae")
                        .replace("ü", "ue").replace("Ü", "Ue")
                        .replace("ö", "oe").replace("Ö", "Oe"))
                .sorted()
                .collect(Collectors.joining("\n"));
        FileWriter writer = new FileWriter("G:/Klasse 11b/Informatik/3 - Miniprojekte/Erasmus & Jendrik/wortliste.txt");
        writer.write(out);
        writer.close();
    }
}
