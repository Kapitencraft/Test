package net.kapitencraft.math;

import java.util.Arrays;
import java.util.Random;

public class QuickSort {
    public static int vergleiche = 0;
    public static int vertausche = 0;

    public static void swap(int[] array, int i1, int i2) {
        if (i1 == i2) return;
        //System.out.println("swapping: " + i1 + "&" + i2);
        int c = array[i1];
        array[i1] = array[i2];
        array[i2] = c;
        vertausche++;
    }

    public static void quickSort(int[] array, int start, int end) {
        int p = array[start]; //pivot
        int l = start + 1; //pointer to the insert point for the next smaller element. points before the element being taken next
        int r = end; //pointer to the insert point for the next larger element. points to the element being taken next
        int sort = array[l]; //element to be sorted
        while (l < r) {
            vergleiche++;
            vertausche++;
            if (sort <= p) {
                array[l++] = sort;
                sort = array[l];
            } else {
                int c = array[r];
                array[r--] = sort;
                sort = c;
            }
        }
        vergleiche++;
        if (sort <= p) {
            array[l++] = sort;
        } else {
            array[r--] = sort;
        }

        swap(array, start, l - 1);
        if (l - start > 2) quickSort(array, start, l - 2);
        if (r + 1 < end) quickSort(array, r + 1, end);
    }

    public static void main(String[] args) {
        //int[] array = new int[] {92, 58, 30, 56, 11, 22, 33, 55, 79, 90, 45, 3, 39, 11, 14, 67, 23, 68, 37, 25, 22, 92, 36, 84, 38, 19, 86, 39, 1, 63, 56, 95, 10, 79, 61, 62, 26, 36, 32, 72, 53, 21, 48, 0, 24, 25, 62, 84, 66, 5, 71, 26, 40, 99, 72, 61, 98, 41, 51, 28, 16, 39, 2, 79, 53, 24, 28, 97, 56, 17, 37, 39, 29, 72, 24, 5, 70, 92, 36, 2, 75, 70, 34, 34, 91, 72, 87, 37, 66, 70, 14, 52, 21};
        //System.out.println(Arrays.toString(array));
        //quickSort(array, 0, array.length - 1);
        //System.out.println(Arrays.toString(array));

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {

            int[] array = new int[10 + random.nextInt(90)];
            for (int i1 = 0; i1 < array.length; i1++) {
                array[i1] = random.nextInt(100);
            }
            vergleiche = 0;
            vertausche = 0;
            quickSort(array, 0, array.length - 1);
            System.out.println("vertausche: " + vertausche + ", vergleiche: " + vergleiche);
        }
    }
}