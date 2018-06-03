package freditor.persistent;

import org.junit.Test;

import java.util.Random;

import static freditor.persistent.CharVector.*;
import static org.junit.Assert.*;

public class CharVectorTest {
    @Test
    public void emptyVector() {
        assertTrue(CharVector.empty.isEmpty());
        assertEquals(0, CharVector.empty.length());
        assertEquals("", CharVector.empty.toString());
    }

    @Test
    public void take0isEmpty() {
        assertTrue(CharVector.of("a").take(0).isEmpty());
    }

    @Test
    public void oneChar() {
        CharVector v = CharVector.of("a");
        assertFalse(v.isEmpty());
        assertEquals(1, v.length());
        assertEquals("a", v.toString());
    }

    @Test
    public void twoChars() {
        CharVector v = CharVector.of("ab");
        assertFalse(v.isEmpty());
        assertEquals(2, v.length());
        assertEquals("ab", v.toString());
    }

    private static String makeStringOfLength(int len) {
        char[] temp = new char[len];
        for (int i = 0; i < len; ++i) {
            temp[i] = (char) ('a' + i % 26);
        }
        return new String(temp);
    }

    @Test
    public void makeStringOfLength27() {
        String expected = "abcdefghijklmnopqrstuvwxyza";
        String actual = makeStringOfLength(27);
        assertEquals(expected, actual);
    }

    private static void testVectorOfLength(int len) {
        String a = makeStringOfLength(len);
        CharVector v = CharVector.of(a);
        assertEquals(len, v.length());
        assertEquals(a, v.toString());
    }

    @Test
    public void fillLevel1() {
        testVectorOfLength(CAPACITY_1);
    }

    @Test
    public void overflowLevel1() {
        testVectorOfLength(CAPACITY_1 + 1);
    }

    @Test
    public void fillLevel2() {
        testVectorOfLength(CAPACITY_2);
    }

    @Test
    public void overflowLevel2() {
        testVectorOfLength(CAPACITY_2 + 1);
    }

    @Test
    public void fillLevel3() {
        testVectorOfLength(CAPACITY_3);
    }

    @Test
    public void overflowLevel3() {
        testVectorOfLength(CAPACITY_3 + 1);
    }

    @Test
    public void fillLevel4() {
        testVectorOfLength(CAPACITY_4);
    }

    @Test
    public void overflowLevel4() {
        testVectorOfLength(CAPACITY_4 + 1);
    }

    private static final Random rng = new Random(System.nanoTime() / 1_000_000_000L);

    private static void randomBranch(int startLength, int endLength) {
        int len = startLength + rng.nextInt(endLength - startLength);
        System.out.println("split after length " + len);
        String a = makeStringOfLength(len);
        CharVector v = CharVector.of(a);
        CharVector w = v.push('a').push('b');
        CharVector x = v.push('a').push('c');
        String b = w.toString();
        String c = x.toString();
        for (int i = 0; i < len; ++i) {
            assertEquals(a.charAt(i), b.charAt(i));
            assertEquals(a.charAt(i), c.charAt(i));
        }
        assertEquals('a', b.charAt(len));
        assertEquals('b', b.charAt(len + 1));
        assertEquals('a', c.charAt(len));
        assertEquals('c', c.charAt(len + 1));
    }

    @Test
    public void randomBranchLevel1() {
        randomBranch(1, CAPACITY_1);
    }

    @Test
    public void randomBranchLevel2() {
        randomBranch(CAPACITY_1, CAPACITY_2);
    }

    @Test
    public void randomBranchLevel3() {
        randomBranch(CAPACITY_2, CAPACITY_3);
    }

    @Test
    public void randomBranchLevel4() {
        randomBranch(CAPACITY_3, CAPACITY_4);
    }

    @Test
    public void randomBranchLevel5() {
        randomBranch(CAPACITY_4, CAPACITY_5);
    }

    @Test
    public void popFromOne() {
        CharVector v = CharVector.of("a");
        assertSame(CharVector.empty, v.pop());
    }

    @Test
    public void popFromTwo() {
        CharVector v = CharVector.of("ab");
        assertEquals("a", v.pop().toString());
    }

    @Test
    public void popFrom33() {
        CharVector v = CharVector.of(makeStringOfLength(33));
        assertEquals(makeStringOfLength(32), v.pop().toString());
    }

    @Test
    public void popFrom34() {
        CharVector v = CharVector.of(makeStringOfLength(34));
        assertEquals(makeStringOfLength(33), v.pop().toString());
    }

    @Test
    public void popAndAppend() {
        CharVector v = CharVector.of("abcd");
        CharVector w = v.pop().pop().push('e').push('f');
        assertEquals("abcd", v.toString());
        assertEquals("abef", w.toString());
    }

    @Test
    public void takeAndPopTwoLevelVector() {
        CharVector len33 = CharVector.of(makeStringOfLength(33));
        CharVector len1 = len33.take(1);
        CharVector len0 = len1.pop();
        assertTrue(len0.isEmpty());
    }
}
