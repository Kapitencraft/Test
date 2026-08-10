package net.kapitencraft.tool;

import java.util.Arrays;
import java.util.Random;

public class MinHeap {
    int[] heapMemory = new int[8];
    int size = 0; //position to insert the next element

    public void insert(int num) {
        if (size >= heapMemory.length) {
            reallocate();
        }
        int insertIdx = size++;
        //Move up the ancestor chain while the parent violates the heap property (parent < num).
        while (parent(insertIdx) > num) {
            heapMemory[(insertIdx - 1) / 2] = heapMemory[insertIdx]; //move element down to child
            insertIdx = (insertIdx - 1) / 2;
        }
        heapMemory[insertIdx] = num; //insert new value at calculated position
    }

    private void reallocate() {
        int[] mem = new int[heapMemory.length << 1];
        System.arraycopy(heapMemory, 0, mem, 0, heapMemory.length);
        heapMemory = mem;
    }

    public int remove() {
        int insertIdx = 0;
        int targetIdx = --size;
        int val = heapMemory[0];
        //find the smallest child
        while (insertIdx * 2 + 1 <= size && heapMemory[targetIdx] > heapMemory[insertIdx]) {
            if (insertIdx * 2 + 2 <= size //check that a right child actually exists
                    && left(insertIdx) > right(insertIdx)) {
                heapMemory[insertIdx] = heapMemory[2 * insertIdx + 2]; //swap memory down
                insertIdx = 2 * insertIdx + 2; //move right
            } else {
                heapMemory[insertIdx] = heapMemory[2 * insertIdx + 1]; //swap memory down
                insertIdx = 2 * insertIdx + 1; //move left
            }
        }
        heapMemory[insertIdx] = heapMemory[size];
        return val;
    }

    private int parent(int idx) {
        return heapMemory[(idx - 1) / 2];
    }

    private int left(int idx) {
        return heapMemory[2 * idx + 1];
    }

    private int right(int idx) {
        return heapMemory[2 * idx + 2];
    }

    public static void main(String[] args) {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 10; i++) {
            heap.insert(i);
            System.out.println(Arrays.toString(heap.heapMemory));
        }
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int num = random.nextInt(10, 50);
            System.out.printf("adding %s: ", num);
            heap.insert(num);
            System.out.println(Arrays.toString(heap.heapMemory));
        }
        for (int i = 0; i < 20; i++) {
            System.out.printf("removed %s: %s\n", heap.remove(), Arrays.toString(heap.heapMemory));
        }
    }
}
