package freditor;

import static freditor.IntVector.empty;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class IntVectorTest {
    private static IntVector valueOf(char x) {
        return empty.push(x);
    }

    private static IntVector valueOf(String s) {
        IntVector result = empty;
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            result = result.push(s.charAt(i));
        }
        return result;
    }

    private static String toString(IntVector v) {
        final int len = v.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append((char) v.intAt(i));
        }
        return sb.toString();
    }

    @Test
    public void emptyVector() {
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.length());
        assertEquals("", toString(empty));
    }

    @Test
    public void take0isEmpty() {
        assertTrue(valueOf('x').take(0).isEmpty());
    }

    @Test
    public void oneChar() {
        IntVector v = valueOf('x');
        assertFalse(v.isEmpty());
        assertEquals(1, v.length());
        assertEquals("x", toString(v));
    }

    @Test
    public void twoChars() {
        IntVector v = valueOf('x').push('y');
        assertFalse(v.isEmpty());
        assertEquals(2, v.length());
        assertEquals("xy", toString(v));
    }

    private static String makeStringOfLength(int len) {
        final String atom = "0123456789_";
        final int n = atom.length();
        StringBuilder sb = new StringBuilder(len);
        int i;
        for (i = len; i >= n; i -= n) {
            sb.append(atom);
        }
        sb.append(atom, 0, i);
        return sb.toString();
    }

    @Test
    public void makeString32() {
        assertEquals("0123456789_0123456789_0123456789", makeStringOfLength(32));
    }

    private static void appendStringOfLength(int len) {
        String s = makeStringOfLength(len);
        IntVector v = valueOf(s);
        assertEquals(s.length(), v.length());
        assertEquals(s, toString(v));
    }

    @Test
    public void fillLevel1() {
        appendStringOfLength(32);
    }

    @Test
    public void overflowLevel1() {
        appendStringOfLength(32 + 1);
    }

    @Test
    public void fillLevel2() {
        appendStringOfLength(32 * 32);
    }

    @Test
    public void overflowLevel2() {
        appendStringOfLength(32 * 32 + 1);
    }

    @Test
    public void fillLevel3() {
        appendStringOfLength(32 * 32 * 32);
    }

    @Test
    public void overflowLevel3() {
        appendStringOfLength(32 * 32 * 32 + 1);
    }

    @Test
    public void fillLevel4() {
        appendStringOfLength(32 * 32 * 32 * 32);
    }

    @Test
    public void overflowLevel4() {
        appendStringOfLength(32 * 32 * 32 * 32 + 1);
    }

    private static final Random rng = new Random(System.nanoTime() / 1_000_000_000L);

    private static void randomBranch(int startLength, int endLength) {
        int len = startLength + rng.nextInt(endLength - startLength);
        System.out.println("split after length " + len);
        String s = makeStringOfLength(len);
        IntVector v = valueOf(s);
        IntVector a = v.push('-').push('a');
        IntVector b = v.push('-').push('b');
        assertEquals(s + "-a", toString(a));
        assertEquals(s + "-b", toString(b));
    }

    @Test
    public void randomBranchLevel1() {
        randomBranch(1, 32);
    }

    @Test
    public void randomBranchLevel2() {
        randomBranch(32, 32 * 32);
    }

    @Test
    public void randomBranchLevel3() {
        randomBranch(32 * 32, 32 * 32 * 32);
    }

    @Test
    public void randomBranchLevel4() {
        randomBranch(32 * 32 * 32, 32 * 32 * 32 * 32);
    }

    @Test
    public void randomBranchLevel5() {
        randomBranch(32 * 32 * 32 * 32, 32 * 32 * 32 * 32 * 2);
    }

    @Test
    public void popFromOne() {
        IntVector x = valueOf('x');
        assertSame(empty, x.pop());
    }

    @Test
    public void popFromTwo() {
        IntVector xy = valueOf('x').push('y');
        assertEquals("x", toString(xy.pop()));
    }

    @Test
    public void popFrom33() {
        IntVector v = valueOf("0123456789abcdefghijklmnopqrstuvw");
        assertEquals("0123456789abcdefghijklmnopqrstuv", toString(v.pop()));
    }

    @Test
    public void popFrom34() {
        IntVector v = valueOf("0123456789abcdefghijklmnopqrstuvwx");
        assertEquals("0123456789abcdefghijklmnopqrstuvw", toString(v.pop()));
    }

    @Test
    public void popAndAppend() {
        IntVector a = valueOf("shelter");
        IntVector b = a.pop().pop().pop().push('d').push('o').push('n');
        assertEquals("shelter", toString(a));
        assertEquals("sheldon", toString(b));
    }
}
