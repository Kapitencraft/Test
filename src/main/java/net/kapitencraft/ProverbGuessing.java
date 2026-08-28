package net.kapitencraft;

import java.util.Random;

public class ProverbGuessing {
    private static final String[] PROVERBS = {
            "quousque tandem abutere catilina patientia nostra",
            "o tempora, o mores",
            "si vis pacem para bellum",
            "in dubito pro reo",
            "veni, vidi, vici",
            "cogito ergo sum",
            "cartargo delendum est",
            "errare humanum est",
            "quo vadis",
            "acta est fabula",
            "nunc est bibendum",
            "tempus est pecunia"
    };

    public static void main(String[] args) {
        String[][] proverbsWordSplit = new String[PROVERBS.length][];
        for (int i = 0; i < PROVERBS.length; i++) {
            proverbsWordSplit[i] = PROVERBS[i].split(" ");
        }
        Random random = new Random();
        int proverbIndex = random.nextInt(PROVERBS.length);
        String[] proverb = proverbsWordSplit[proverbIndex];
        int wordIndex = random.nextInt(proverb.length);

    }
}
