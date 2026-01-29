package net.kapitencraft.chemical_lexer;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChemicalLexer {
    public static void main(String[] args) {
        Node root = generateTree();

        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        String o;
        while (!Objects.equals(o = scanner.next(), "exit")) {
            Result result = parse(o.toLowerCase(), root);
            System.out.println("Results: ");
            result.complete.forEach(System.out::println);
            System.out.println("=============");
            System.out.println("Partial Results: ");
            result.partial.sort(Comparator.comparingInt(String::length));
            result.partial.forEach(System.out::println);
        }
    }

    private static Node generateTree() {

        String[] elements = new String[] {
                "H",                                                                                                  "He",
                "Li", "Be",                                                             "B",  "C",  "N",  "O",  "F",  "Ne",
                "Na", "Mg",                                                             "Al", "Si", "P",  "S",  "Cl", "Ar",
                "K",  "Ca", "Sc", "Ti", "V",  "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr",
                "Rb", "Sr", "Y",  "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I",  "Xe",
                "Cs", "Ba", "La", "Hf", "Ta", "W",  "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn",
                "Fr", "Ra", "Ac", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds", "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og",

                "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu",
                "Th", "Pa", "U",  "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr"
        };

        Node node = new Node(null);
        for (String element : elements) {
            Node e = node;
            for (char c : element.toCharArray()) {
                e = e.getOrAddChild(Character.toLowerCase(c));
            }
            e.setEntry(element);
        }
        return node;
    }

    private static final class Node {
        private @Nullable String entry;
        private final @Nullable Node[] children;

        private Node(@Nullable String entry) {
            this.entry = entry;
            this.children = new Node[26];
        }

        private void addChild(char c, Node child) {
            this.children[c - 97] = child;
        }

        private void setEntry(String entry) {
            this.entry = entry;
        }

        public Node getOrAddChild(char c) {
            Node n = getChild(c);
            if (n != null) return n;
            Node n1 = new Node(null);
            addChild(c, n1);
            return n1;
        }

        public Node getChild(char c) {
            return this.children[c - 97];
        }
    }

    private static class Result {
        private final String original;
        private final List<String> partial = new ArrayList<>();
        private final List<String> complete = new ArrayList<>();
        private int depth = 0;

        private Result(String original) {
            this.original = original;
        }

        public int getDepth() {
            return depth;
        }

        public void addPartial(String partial) {
            this.partial.add(partial);
            if (partial.length() > depth)
                depth = partial.length();
        }

        public void addComplete(String obj) {
            this.complete.add(obj);
            this.depth = obj.length();
        }
    }


    private static Result parse(String o, Node root) {
        Result result = new Result(o);
        parseRecursive(o, root, result, 0, "");
        return result;
    }

    private static void parseRecursive(String o, Node root, Result result, int i, String parsed) {
        Node n;
        Node n1 = root;
        boolean success = false;
        while (i < o.length() && (n = n1.getChild(o.charAt(i++))) != null) {
            if (n.entry != null) {
                parseRecursive(o, root, result, i, parsed + n.entry);
                success = true;
            }
            n1 = n;
        }
        if (!success) {
            if (parsed.length() == o.length())
                result.addComplete(parsed);
            else
                result.addPartial(parsed);
        }
    }
}
