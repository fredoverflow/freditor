package freditor;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LevenshteinTest {
    @Test
    public void equal() {
        assertEquals(0, Levenshtein.distance("zero", "zero"));
    }

    @Test
    public void missingFirstCharacter() {
        assertEquals(1, Levenshtein.distance("cleanTheRoom", "leanTheRoom"));
    }

    @Test
    public void missingCharacter() {
        assertEquals(1, Levenshtein.distance("karelsFirstProgram", "karelFirstProgram"));
    }

    @Test
    public void missingLastCharacter() {
        assertEquals(1, Levenshtein.distance("defuseTwoBombs", "defuseTwoBomb"));
    }

    @Test
    public void missingWord() {
        assertEquals(3, Levenshtein.distance("climbTheStairs", "climbStairs"));
    }

    @Test
    public void wrongFirstCharacter() {
        assertEquals(1, Levenshtein.distance("fillTheHoles", "killTheHoles"));
    }

    @Test
    public void wrongCharacter() {
        assertEquals(1, Levenshtein.distance("fillTheHoles", "fillThaHoles"));
    }

    @Test
    public void wrongLastCharacter() {
        assertEquals(1, Levenshtein.distance("fillTheHoles", "fillTheHolez"));
    }

    @Test
    public void swappedCharacters() {
        assertEquals(2, Levenshtein.distance("walkTheLabyrinth", "walkTheLabyrinht"));
    }

    @Test
    public void keyboardLayouts() {
        assertEquals(3, Levenshtein.distance("qwertz", "azerty"));
    }

    @Test
    public void lastMatch() {
        List<String> vocabulary = Arrays.asList("apple", "apples", "apply", "applies");
        List<String> actual = Levenshtein.bestMatches("supplies", vocabulary);
        List<String> expected = Arrays.asList("applies");
        assertEquals(expected, actual);
    }

    @Test
    public void multipleMatches() {
        List<String> vocabulary = Arrays.asList("quine", "quid", "quit", "juice", "guide");
        List<String> actual = Levenshtein.bestMatches("gui", vocabulary);
        List<String> expected = Arrays.asList("guide", "quid", "quit");
        assertEquals(expected, actual);
    }
}
