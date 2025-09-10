package net.kapitencraft.math;

import java.util.Arrays;
import java.util.Random;

public class QuickSort {

    public static void swap(int[] array, int i1, int i2) {
        if (i1 == i2) return;
        System.out.println("swapping: " + i1 + "&" + i2);
        int c = array[i1];
        array[i1] = array[i2];
        array[i2] = c;
    }

    public static void quickSort(int[] array, int minI, int maxI) {
        //TODO set size dynamically
        if (minI == maxI) {
            return;
        }

        int m = minI + (maxI - minI) / 2;
        int pivot = array[m];
        System.out.println(pivot + " " + minI + " " + maxI + " " + m);

        for (int i = minI; i < m; i++) {
            int large = maxI - (i - minI) - 1;
            if (array[i] > pivot && array[large] < pivot) {
                swap(array, i, large);
                continue;
            }
            if (array[i] > array[large]) {
                swap(array, i, large);
            }
            if (array[i] > pivot && array[large] > pivot) {
                pivot = array[i];
                swap(array, m, i);
            } else if (array[i] < pivot && array[large] < pivot) {
                pivot = array[large];
                swap(array, m, large);
            }
        }

        System.out.println(Arrays.toString(array));

        if ((maxI - minI) <= 2) {
            return;
        }

        quickSort(array, minI, m + 1);
        quickSort(array, m + 1, maxI);
    }

    public static void main(String[] args) {
        int[] array = new int[] {5, 18, 35, 96, 24, 75, 9, 81, 94, 4, 7, 24, 70, 73, 62, 15, 4, 58, 25, 4, 13, 77, 74, 99, 77, 73, 2};
        System.out.println(Arrays.toString(array));
        quickSort(array, 0, array.length);
        System.out.println(Arrays.toString(array));

        //Random random = new Random();
        //for (int i = 0; i < 1000; i++) {
//
        //    int[] array = new int[random.nextInt(100)];
        //    for (int i1 = 0; i1 < array.length; i1++) {
        //        array[i1] = random.nextInt(100);
        //    }
        //    System.out.println(Arrays.toString(array));
        //    quickSort(array, 0, array.length);
        //    System.out.println(Arrays.toString(array));
        //}
    }
}