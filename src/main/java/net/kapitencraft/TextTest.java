package net.kapitencraft;

import java.util.List;

public class TextTest {
    private static final List<String> text = List.of(
            "unbreaking_core",
            "this_item_is_large",
            "large_celium_leave"
    );

    public static String makeGrammar(String toName) {
        char[] chars = toName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                chars[0] = Character.toUpperCase(chars[0]);
            } else if (chars[i] == '_') {
                chars[i++] = ' ';
                chars[i] = Character.toUpperCase(chars[i]);
            }
        }
        return new String(chars);
    }

    public static void main(String[] args) {
        for (String s : text) {
            System.out.println(makeGrammar(s));
        }
    }
}
