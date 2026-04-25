package net.kapitencraft.math;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

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
        //int[] numbers = new int[] {
        //        51, 95, 66, 72, 42, 38, 39, 41, 15
        //};
        //sortArray(numbers);
        //System.out.println(Arrays.toString(numbers));
        List<BezierPoint> points = getBezierAtmospherePressure(1024);

        PrintStream output = System.out;

        output.println("[");
        output.println(
                points.stream().map(BezierPoint::toJson).collect(Collectors.joining(","))
        );
        output.println("]");
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
        int pivot = numbers[0];
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > pivot && numbers[numbers.length - i] < pivot) {
                swap(numbers, i, numbers.length - i);
            }
        }
    }

    private static void swap(int[] numbers, int p) {
        swap(numbers, p, p+1);
    }

    private static void swap(int[] numbers, int i1, int i2) {
        int c = numbers[i1];
        numbers[i1] = numbers[i2];
        numbers[i2] = c;
    }

    private static List<BezierPoint> getBezierAtmospherePressure(int maxAltitude) {
        double currentAltitude = -64;
        int seaLevel = 64;

        final double baseSlope = -0.004;
        final double maxPressure = 1.5;
        final double maxStep = 200;

        final double smoothingAltitude = maxAltitude - 40;

        // clamps the bottom most point to have a pressure of 1.5 or less
        currentAltitude = Math.max(currentAltitude, Math.log(maxPressure) / baseSlope + seaLevel);

        final List<BezierPoint> pressureFunction = new ArrayList<>();

        while (true) {
            final double currentPressure = Math.exp(baseSlope * (currentAltitude - seaLevel));
            final double currentSlope = currentPressure * baseSlope;
            pressureFunction.add(new BezierPoint(currentAltitude, currentPressure, currentSlope));

            if (currentAltitude < seaLevel && currentAltitude + maxStep >= seaLevel) {
                currentAltitude = seaLevel;
            } else if (currentAltitude < smoothingAltitude && currentAltitude + maxStep >= smoothingAltitude) {
                currentAltitude = smoothingAltitude;
            } else if (currentAltitude >= smoothingAltitude) {
                break;
            } else {
                currentAltitude += maxStep;
            }
        }

        final double smoothingPressure = pressureFunction.getLast().value();
        final double finalSlope = -2 * smoothingPressure / (maxAltitude - smoothingAltitude);
        pressureFunction.add(new BezierPoint(maxAltitude, 0, finalSlope));
        return pressureFunction;
    }

    private record BezierPoint(double altitude, double value, double slope) {

        public String toJson() {
            return """
                    {
                        "altitude": %s,
                        "value": %-4s,
                        "slope": %-4s
                    }
                    """.formatted(altitude, value, slope);
        }
    }
}
