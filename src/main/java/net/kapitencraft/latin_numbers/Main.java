package net.kapitencraft.latin_numbers;

import net.kapitencraft.tool.Pair;

import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.out.println(makeLatin(50000));
    }

    static final List<Pair<Integer, String>> latins = List.of(
            Pair.of(1, "I"),
            Pair.of(5, "V"),
            Pair.of(10, "X"),
            Pair.of(50, "L"),
            Pair.of(100, "C"),
            Pair.of(500, "D"),
            Pair.of(1000, "M")
    );

    private static String makeLatin(int in) {
        StringBuilder s = new StringBuilder();
        while (in > 0) {
            for (int i = latins.size()-1; i >= 0; i--) {
                Pair<Integer, String> element = latins.get(i);
                if (element.left() <= in) {
                    s.append(element.right());
                    in -= element.left();
                    break;
                } else {
                    for (int i1 = 0; i1 < i; i1+=2) {
                        Pair<Integer, String> e1 = latins.get(i1);
                        if (element.left() - e1.left() <= in) {
                            s.append(e1.right()).append(element.right());
                            in -= element.left() - e1.left();
                            break;
                        }
                    }
                }
            }
        }
        return s.toString();
    }
}
