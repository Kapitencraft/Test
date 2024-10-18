package net.kapitencraft.math;

import java.util.*;

public class MathMain {
    static Map<Integer, List<Integer>> cache = new HashMap<>();

    private static List<Integer> alleTeiler(int x) {
        if (cache.containsKey(x)) return cache.get(x);
        List<Integer> list = new ArrayList<>();
        for (int i = 2; i <= x / 2; i++) {
            if (x % i == 0) list.add(i);
        }
        cache.put(x, list);
        return list;
    }

    private static boolean istPrim(int x) {
        return alleTeiler(x).isEmpty();
    }

    private static Map<Integer, Integer> primzahlen(int x) {
        Map<Integer, Integer> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(x);
        while (!list.isEmpty()) {
            int i = list.get(0);
            list.remove(0);
            if (istPrim(i)) map.put(i, Objects.requireNonNullElse(map.get(i), 0) + 1);
            else {
                List<Integer> l = alleTeiler(i);
                list.add(l.get(0));
                list.add(i / l.get(0));
            }
        }
        return map;
    }

    public static void main(String[] args) {
        System.out.println("\u001B[32mE");
        System.exit(0);
        Scanner scanner = new Scanner(System.in);

        int i;
        while (true) {
            do {
                String s = scanner.nextLine();
                try {
                    i = Integer.parseInt(s);
                    break;
                } catch (Exception ignored) {
                    System.out.print("please enter a number");
                }

            } while (true);

            Map<Integer, Integer> primzahlen = primzahlen(i);
            if (istPrim(i)) System.out.print("primzahl xD");
            else primzahlen.forEach((integer, integer2) -> System.out.println(integer + "->" + integer2));
            System.out.println();
        }
    }
 }
