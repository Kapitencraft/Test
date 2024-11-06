package net.kapitencraft.cluedo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {
    private static final Pattern registerPlayer = Pattern.compile("register ([a-zA-Z]+) as ([a-zA-Z]+)");
    private static final Pattern missing = Pattern.compile("([a-zA-Z]+) didn't have ([a-zA-Z]+)");
    private static final Pattern found = Pattern.compile("([a-zA-Z]+) had ([a-zA-Z]+)");
    private static final Pattern print = Pattern.compile("print ([a-zA-Z]+)");
    private static final Pattern check = Pattern.compile("checked ([a-zA-Z]+) got ([a-zA-Z]+)");

    private static final List<String> defaultCharacters = List.of(

    );
    private static final List<String> defaultRooms = List.of(

    );
    private static final List<String> defaultWeapons = List.of(

    );


    static Map<String, Element> playerCache = new HashMap<>();
    static List<String> characters = new ArrayList<>();
    static List<String> rooms = new ArrayList<>();
    static List<String> weapons = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = "";
        while (!"exit".equals(s)) {
            s = scanner.nextLine();
            if ("clear".equals(s)) {
                playerCache.clear();
                characters.clear();
                rooms.clear();
                weapons.clear();
                continue;
            }
            {
                Matcher matcher = registerPlayer.matcher(s);
                if (matcher.matches()) {
                    String player = matcher.group(1);
                    String character = matcher.group(2);
                    if (playerCache.containsKey(player)) {
                        System.err.printf("player %s already registered!", player);
                        System.out.println();
                        continue;
                    }
                    if (!characters.contains(character)) {
                        System.err.printf("attempting to register unknown character '%s'", character);
                        System.out.println();
                        continue;
                    }
                    playerCache.put(player, new Element(character));
                    System.out.printf("registered new player %s", player);
                    System.out.println();
                    continue;
                }
            }
            {
                Matcher matcher = missing.matcher(s);
                if (matcher.matches()) {
                    String player = matcher.group(1);
                    String element = matcher.group(2);
                    Element elementData = playerCache.get(player);
                    if (characters.contains(element)) elementData.failedCharacters.add(element);
                    else if (rooms.contains(element)) elementData.failedRooms.add(element);
                    else if (weapons.contains(element)) elementData.failedWeapons.add(element);
                    else {
                        System.err.printf("unknown element '%s'", element);
                        System.out.println();
                    }
                    continue;
                }
            }
            {
                Matcher matcher = found.matcher(s);
                if (matcher.matches()) {
                    String player = matcher.group(1);
                    String element = matcher.group(2);
                    Element elementData = playerCache.get(player);
                    elementData.addSuccess(element);
                    continue;
                }
            }
            {
                Matcher matcher = print.matcher(s);
                if (matcher.matches()) {
                    String type = matcher.group(1);
                    if ("missingChars".equals(type)) {
                        List<String> data = new ArrayList<>(characters);
                        data.removeAll(playerCache.values().stream().map(element -> element.succeededCharacters).flatMap(List::stream).toList());
                        System.out.println("unknown characters:");
                        for (String charId : data) {
                            System.out.println("\t" + charId);
                        }
                    } else if ("missingRooms".equals(type)) {
                        List<String> data = new ArrayList<>(rooms);
                        data.removeAll(playerCache.values().stream().map(element -> element.succeededRooms).flatMap(List::stream).toList());
                        System.out.println("unknown rooms:");
                        for (String charId : data) {
                            System.out.println("\t" + charId);
                        }
                    }
                    else if ("missingWeapons".equals(type)) {
                        List<String> data = new ArrayList<>(weapons);
                        data.removeAll(playerCache.values().stream().map(element -> element.succeededWeapons).flatMap(List::stream).toList());
                        System.out.println("unknown weapons:");
                        for (String charId : data) {
                            System.out.println("\t" + charId);
                        }
                    }
                    else if ("unchecked".equals(type)) {
                        List<String> elements = new ArrayList<>();
                        playerCache.forEach((s1, element) -> {
                            if (!element.checkedInfo) elements.add(s1);
                        });
                        System.out.println("unchecked players:");
                        for (String element : elements) {
                            System.out.println("\t" + element);
                        }
                    }
                }
            }
            {
                Matcher matcher = check.matcher(s);
                if (matcher.matches()) {
                    String player = matcher.group(1);
                    String got = matcher.group(2);
                    Element element = playerCache.get(player);
                    element.checkedInfo = true;
                    element.addSuccess(got);
                    continue;
                }
            }
            System.err.printf("unknown command '%s'", s);
            System.out.println();
        }
    }


    private static class Element {
        private final String charName;
        boolean checkedInfo = false;
        final List<String> failedCharacters = new ArrayList<>();
        final List<String> failedRooms = new ArrayList<>();
        final List<String> failedWeapons = new ArrayList<>();
        final List<String> succeededCharacters = new ArrayList<>();
        final List<String> succeededRooms = new ArrayList<>();
        final List<String> succeededWeapons = new ArrayList<>();

        private Element(String charName) {
            this.charName = charName;
        }

        public void addSuccess(String element) {
            if (characters.contains(element)) succeededCharacters.add(element);
            else if (rooms.contains(element)) succeededRooms.add(element);
            else if (weapons.contains(element)) succeededWeapons.add(element);
            else {
                System.err.printf("unknown element '%s'", element);
                System.out.println();
            }
        }
    }
}
