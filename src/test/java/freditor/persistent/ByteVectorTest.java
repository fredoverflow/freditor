package freditor.persistent;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static freditor.Maths.atLeastZero;
import static org.junit.jupiter.api.Assertions.*;

public class ByteVectorTest {
    @Test
    public void emptyVector() {
        assertTrue(ByteVector.EMPTY.isEmpty());
        assertEquals(0, ByteVector.EMPTY.size());
        assertEquals("", ByteVector.EMPTY.toString());
    }

    @Test
    public void oneChar() {
        ByteVector v = ByteVector.of((byte) 11);
        assertFalse(v.isEmpty());
        assertEquals(1, v.size());
        assertEquals(11, v.byteAt(0));
    }

    @Test
    public void twoChars() {
        ByteVector v = ByteVector.of((byte) 11, (byte) 13);
        assertFalse(v.isEmpty());
        assertEquals(2, v.size());
        assertEquals(11, v.byteAt(0));
        assertEquals(13, v.byteAt(1));
    }

    private static byte[] arrayOfSize(int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; ++i) {
            bytes[i] = (byte) ('A' + i % 26);
        }
        return bytes;
    }

    private static void testVectorOfLength(int size) {
        byte[] bytes = arrayOfSize(size);
        ByteVector v = ByteVector.of(bytes);
        assertEquals(size, v.size());
        assertArrayEquals(bytes, v.toArray());
    }

    @Test
    public void fillLevel1() {
        testVectorOfLength(32);
    }

    @Test
    public void overflowLevel1() {
        testVectorOfLength(33);
    }

    @Test
    public void fillLevel2() {
        testVectorOfLength(32 * 32 + 32);
    }

    @Test
    public void overflowLevel2() {
        testVectorOfLength(32 * 32 + 33);
    }

    @Test
    public void fillLevel3() {
        testVectorOfLength(32 * 32 * 32 + 32);
    }

    @Test
    public void overflowLevel3() {
        testVectorOfLength(32 * 32 * 32 + 33);
    }

    @Test
    public void fillLevel4() {
        testVectorOfLength(32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void overflowLevel4() {
        testVectorOfLength(32 * 32 * 32 * 32 + 33);
    }

    @Test
    public void fillLevel5() {
        testVectorOfLength(32 * 32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void overflowLevel5() {
        testVectorOfLength(32 * 32 * 32 * 32 * 32 + 33);
    }

    private static final Random rng = new Random(System.nanoTime() / 1_000_000_000L);

    private static void randomBranch(int minSize, int maxSize) {
        int size = minSize + rng.nextInt(maxSize - minSize + 1);
        System.out.println("split after length " + size);
        byte[] bytes = arrayOfSize(size);

        ByteVector v = ByteVector.of(bytes);
        ByteVector w = v.push((byte) 11).push((byte) 13);
        ByteVector x = v.push((byte) 11).push((byte) 17);
        byte[] b = w.toArray();
        byte[] c = x.toArray();
        for (int i = 0; i < size; ++i) {
            assertEquals(bytes[i], b[i]);
            assertEquals(bytes[i], c[i]);
        }
        assertEquals(11, b[size]);
        assertEquals(13, b[size + 1]);
        assertEquals(11, c[size]);
        assertEquals(17, c[size + 1]);
    }

    @Test
    public void randomBranchLevel1() {
        randomBranch(0, 32);
    }

    @Test
    public void randomBranchLevel2() {
        randomBranch(33, 32 * 32 + 32);
    }

    @Test
    public void randomBranchLevel3() {
        randomBranch(32 * 32 + 33, 32 * 32 * 32 + 32);
    }

    @Test
    public void randomBranchLevel4() {
        randomBranch(32 * 32 * 32 + 33, 32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void randomBranchLevel5() {
        randomBranch(32 * 32 * 32 * 32 + 33, 32 * 32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void popFromOne() {
        ByteVector v = ByteVector.of((byte) 11);
        assertEquals(1, v.size());
        assertEquals(11, v.top());

        v = v.pop();
        assertEquals(0, v.size());
        assertEquals("", v.toString());
    }

    @Test
    public void popFromTwo() {
        ByteVector v = ByteVector.of((byte) 11, (byte) 13);
        assertEquals(2, v.size());
        assertEquals(13, v.top());

        v = v.pop();
        assertEquals(1, v.size());
        assertEquals(11, v.top());

        v = v.pop();
        assertEquals(0, v.size());
        assertEquals("", v.toString());
    }

    private static void checkLast32(byte[] bytes, ByteVector v) {
        final int size = v.size();
        for (int i = atLeastZero(size - 32); i < size; ++i) {
            assertEquals(bytes[i], v.byteAt(i));
        }
    }

    private static void exercisePopAndPush(int size) {
        final int max = size + 48;
        final int min = size - 48;
        byte[] bytes = arrayOfSize(max);

        ByteVector v = ByteVector.of(bytes);
        while (v.size() >= min) {
            checkLast32(bytes, v);
            v = v.pop();
        }
        while (v.size() < max) {
            v = v.push(bytes[v.size()]);
            checkLast32(bytes, v);
        }
    }

    @Test
    public void exerciseLevel1() {
        exercisePopAndPush(32 + 32);
    }

    @Test
    public void exerciseLevel2() {
        exercisePopAndPush(32 * 32 + 32);
    }

    @Test
    public void exerciseLevel3() {
        exercisePopAndPush(32 * 32 * 32 + 32);
    }

    @Test
    public void exerciseLevel4() {
        exercisePopAndPush(32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void exerciseLevel5() {
        exercisePopAndPush(32 * 32 * 32 * 32 * 32 + 32);
    }

    @Test
    public void popAndAppend() {
        ByteVector v = ByteVector.of((byte) 11, (byte) 13, (byte) 17, (byte) 19);
        ByteVector w = v.pop().pop().push((byte) 15).push((byte) 17);
        assertArrayEquals(new byte[]{11, 13, 17, 19}, v.toArray());
        assertArrayEquals(new byte[]{11, 13, 15, 17}, w.toArray());
    }

    @Test
    public void takeIsOptimizedPopChain() {
        final int MAX_SIZE = 1057;
        byte[] bytes = arrayOfSize(MAX_SIZE);
        for (int size = 0; size <= MAX_SIZE; ++size) {
            ByteVector v = ByteVector.of(bytes, size);
            ByteVector p = v.take(size);
            assertEquals(v, p);
            for (int targetSize = size - 1; targetSize >= 0; --targetSize) {
                p = p.pop();
                ByteVector t = v.take(targetSize);
                assertEquals(p, t);
            }
        }
    }

    private static void hashing(int size) {
        byte[] bytes = arrayOfSize(size);

        int hash = 0;
        for (byte b : bytes) {
            hash = hash * 31 + b;
        }

        ByteVector v = ByteVector.of(bytes);
        assertEquals(hash, v.hashCode());
    }

    @Test
    public void hashingLevel1() {
        hashing(0);
        hashing(1);

        hashing(31);
        hashing(32);
        hashing(33);

        hashing(63);
        hashing(64);
    }

    @Test
    public void hashingLevel2() {
        hashing(65);

        hashing(95);
        hashing(96);
        hashing(97);

        hashing(1023);
        hashing(1024);
        hashing(1025);

        hashing(1055);
        hashing(1056);
    }

    @Test
    public void hashingLevel3() {
        hashing(1057);

        hashing(1087);
        hashing(1088);
        hashing(1089);

        hashing(32767);
        hashing(32768);
        hashing(32769);

        hashing(32799);
        hashing(32800);
    }
}
