package net.kapitencraft.math;

import java.util.*;

public class MathMain {
    static Map<Integer, List<Integer>> cache = new HashMap<>();

    private static List<Integer> alleTeiler(int x) {
        if (cache.containsKey(x)) return cache.get(x);
        List<Integer> list = new ArrayList<>();
        for (int i = 2; i <= Math.sqrt(x); i++) {
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
        //Scanner scanner = new Scanner(System.in);
//
        //int i;
        //while (true) {
        //    do {
        //        String s = scanner.nextLine();
        //        try {
        //            i = Integer.parseInt(s);
        //            break;
        //        } catch (Exception ignored) {
        //            System.out.print("please enter a number");
        //        }
//
        //    } while (true);
//
        //    Map<Integer, Integer> primzahlen = primzahlen(i);
        //    if (istPrim(i)) System.out.print("primzahl xD");
        //    else primzahlen.forEach((integer, integer2) -> System.out.println(integer + "->" + integer2));
        //    System.out.println();
        //}
        int[] numbers = new int[] {
                342, 525, 154, 356, 856, 734, 429, 692, 347, 845, 256, 584, 846, 523, 342, 953, 826, 862
        };
        sortArray(numbers);
        System.out.println(Arrays.toString(numbers));
    }

    private static int invertColor(int i) {
        for (int j = 0; j < 3; j++) {
            int val = (255 - (i >> ((1 + j) * 8)) & 255);
            int mask = 0xFF << ((1 + j) * 8);
            i = (i & ~mask) | (val << ((1 + j) * 8));
        }
        return i;
    }

    private static void sortArray(int[] numbers) {
        for (int i = 0; i < numbers.length - 1; i++) {
            int p = i;
            while (p >= 0 && numbers[p] > numbers[p + 1]) {
                swap(numbers, p);
                p--;
            }
        }
    }

    private static void swap(int[] numbers, int p) {
        int c = numbers[p];
        numbers[p] = numbers[p + 1];
        numbers[p + 1] = c;
    }
}
