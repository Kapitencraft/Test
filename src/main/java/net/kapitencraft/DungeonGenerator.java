package net.kapitencraft;

import java.util.Random;

public class DungeonGenerator {

    private static Dungeon generate(int width, int height, Random random) {
        Dungeon dungeon = new Dungeon(width, height);
        dungeon.entrance =
    }

    private static class Dungeon {
        private Vec2 entrance;
        private Vec2 exit;

        private final char[][] grid;

        public Dungeon(int width, int height) {
            grid = new char[height][width];
        }
    }

    private record Vec2(int x, int y) {}
}
