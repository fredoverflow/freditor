package freditor;

import static freditor.IntVector.empty;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class IntVectorTest {
    @Test
    public void emptyVector() {
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.length());
        assertArrayEquals(new int[0], empty.toArray());
    }

    @Test
    public void take0isEmpty() {
        assertTrue(IntVector.of(42).take(0).isEmpty());
    }

    @Test
    public void oneInt() {
        IntVector v = IntVector.of(42);
        assertFalse(v.isEmpty());
        assertEquals(1, v.length());
        assertArrayEquals(new int[]{42}, v.toArray());
    }

    @Test
    public void twoInts() {
        IntVector v = IntVector.of(11, 13);
        assertFalse(v.isEmpty());
        assertEquals(2, v.length());
        assertArrayEquals(new int[]{11, 13}, v.toArray());
    }

    private static int[] makeArrayOfLength(int len) {
        int[] temp = new int[len];
        for (int i = 0; i < len; ++i) {
            temp[i] = 1 + i % 7;
        }
        return temp;
    }

    @Test
    public void makeArrayOfLength15() {
        int[] expected = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1};
        int[] actual = makeArrayOfLength(15);
        assertArrayEquals(expected, actual);
    }

    private static void testVectorOfLength(int len) {
        int[] a = makeArrayOfLength(len);
        IntVector v = IntVector.of(a);
        assertEquals(len, v.length());
        assertArrayEquals(a, v.toArray());
    }

    @Test
    public void fillLevel1() {
        testVectorOfLength(32);
    }

    @Test
    public void overflowLevel1() {
        testVectorOfLength(32 + 1);
    }

    @Test
    public void fillLevel2() {
        testVectorOfLength(32 * 32);
    }

    @Test
    public void overflowLevel2() {
        testVectorOfLength(32 * 32 + 1);
    }

    @Test
    public void fillLevel3() {
        testVectorOfLength(32 * 32 * 32);
    }

    @Test
    public void overflowLevel3() {
        testVectorOfLength(32 * 32 * 32 + 1);
    }

    @Test
    public void fillLevel4() {
        testVectorOfLength(32 * 32 * 32 * 32);
    }

    @Test
    public void overflowLevel4() {
        testVectorOfLength(32 * 32 * 32 * 32 + 1);
    }

    private static final Random rng = new Random(System.nanoTime() / 1_000_000_000L);

    private static void randomBranch(int startLength, int endLength) {
        int len = startLength + rng.nextInt(endLength - startLength);
        System.out.println("split after length " + len);
        int[] a = makeArrayOfLength(len);
        IntVector v = IntVector.of(a);
        IntVector w = v.push(42).push(11);
        IntVector x = v.push(42).push(13);
        int[] b = w.toArray();
        int[] c = x.toArray();
        for (int i = 0; i < len; ++i) {
            assertEquals(a[i], b[i]);
            assertEquals(a[i], c[i]);
        }
        assertEquals(42, b[len]);
        assertEquals(11, b[len + 1]);
        assertEquals(42, c[len]);
        assertEquals(13, c[len + 1]);
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
        IntVector v = IntVector.of(42);
        assertSame(empty, v.pop());
    }

    @Test
    public void popFromTwo() {
        IntVector v = IntVector.of(11, 13);
        assertArrayEquals(new int[]{11}, v.pop().toArray());
    }

    @Test
    public void popFrom33() {
        IntVector v = IntVector.of(makeArrayOfLength(33));
        assertArrayEquals(makeArrayOfLength(32), v.pop().toArray());
    }

    @Test
    public void popFrom34() {
        IntVector v = IntVector.of(makeArrayOfLength(34));
        assertArrayEquals(makeArrayOfLength(33), v.pop().toArray());
    }

    @Test
    public void popAndAppend() {
        IntVector v = IntVector.of(1, 2, 3, 4);
        IntVector w = v.pop().pop().push(4).push(8);
        assertArrayEquals(new int[]{1, 2, 3, 4}, v.toArray());
        assertArrayEquals(new int[]{1, 2, 4, 8}, w.toArray());
    }

    @Test
    public void takeAndPopTwoLevelVector() {
        IntVector len33 = IntVector.of(makeArrayOfLength(33));
        IntVector len1 = len33.take(1);
        IntVector len0 = len1.pop();
        assertTrue(len0.isEmpty());
    }
}
