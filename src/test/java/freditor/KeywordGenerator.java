package freditor;

import java.util.HashSet;

public class KeywordGenerator {
    public static void main(String[] args) {
        new KeywordGenerator().generateKeywords("else", "if", "repeat", "void", "while");
    }

    private int negative = -15;
    private int positive = 10;

    private final HashSet<String> states = new HashSet<>();

    private void generateKeywords(String... keywords) {
        for (String keyword : keywords) {
            generateStates(keyword);
        }
        for (String keyword : keywords) {
            generateCases(keyword);
        }
    }

    private void generateStates(String keyword) {
        String upper = keyword.toUpperCase();
        generateFirstState(upper);
        generateNextStates(upper);
        System.out.println();
    }

    private void generateFirstState(String upper) {
        String prefix = upper.substring(0, 1);
        if (!states.contains(prefix)) {
            System.out.println("public static final int " + prefix + " = " + negative-- + ";");
            states.add(prefix);
        }
    }

    private void generateNextStates(String upper) {
        final int len = upper.length();
        for (int i = 2; i <= len; ++i) {
            String prefix = upper.substring(0, i);
            if (!states.contains(prefix)) {
                System.out.println("public static final int " + prefix + " = " + positive++ + ";");
                states.add(prefix);
            }
        }
    }

    private void generateCases(String keyword) {
        String upper = keyword.toUpperCase();
        final int len = keyword.length();

        StringBuilder state = new StringBuilder(len);
        state.append(upper.charAt(0));

        for (int i = 1; i < len; ++i) {
            System.out.println("case " + state + ":");
            state.append(upper.charAt(i));
            System.out.println("return keyword('" + keyword.charAt(i) + "', " + state + ", input);");
        }
        System.out.println("case " + upper + ":");
        System.out.println("return identifier(input);");
        System.out.println();
    }
}
