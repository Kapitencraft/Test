package net.kapitencraft;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawMatches {
    private static final Random RANDOM = new Random();
    private static final AI[] bots = new AI[] {new AI(), new AI()};

    public static void main(String[] args) {
        for (int it = 0; it < 10000; it++) {
            int amount = 10;
            int turn = 0;
            while (amount > 1) {
                amount -= bots[turn++ & 1].makeTurn(amount);
            }
            for (int i = 0; i < bots.length; i++) {
                bots[i].learn((turn & 1) == i);
            }
        }
        System.out.println("trained");
    }


    private static class AI {
        private final int[] hits = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        wins = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        private final List<Integer> iteration = new ArrayList<>();

        public int makeTurn(int remaining) {
            int turn = calculateNext(remaining);
            iteration.add(remaining - turn);
            return turn;
        }

        public int calculateNext(int remaining) {
            if (remaining < 3) return 1;
            if (remaining == 3) return 2; //remove 2 so the opponent remains with 1 and loses
            float wC1 = wins[remaining - 1] / (float) hits[remaining - 1]; //stored win-chance for removing 1 at this position
            float wC2 = wins[remaining - 2] / (float) hits[remaining - 2]; //stored win-chance for removing 2 at this position
            float wC3 = wins[remaining - 3] / (float) hits[remaining - 3]; //stored win-chance for removing 3 at this position
            if (wC1 > wC3)
                return check2Next(remaining, 1, 2);
            if (wC1 < wC3)
                return check2Next(remaining, 2, 3);
            if (wC1 == wC2)
                return RANDOM.nextInt(3) + 1; //value exclusive
            if (wC1 > wC2) {
                if (RANDOM.nextBoolean())
                    return 1;
                else
                    return 2;
            }
            return 2;
        }

        private int check2Next(int remaining, int l, int r) {
            float wCl = wins[remaining - l] / (float) hits[remaining - l];
            float wCr = wins[remaining - r] / (float) hits[remaining - r];
            if (wCl > wCr)
                return l;
            if (wCl < wCr)
                return r;
            return RANDOM.nextInt(l, r + 1);
        }

        public void learn(boolean win) {
            for (Integer i : iteration) {
                hits[i]++;
                if (win)
                    wins[i]++;
            }
            iteration.clear();
        }
    }
}
