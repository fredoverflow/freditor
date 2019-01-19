package freditor.persistent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class ByteVector {
    private final Object root;
    private final byte[] tail;
    private final int size;

    private ByteVector(Object root, byte[] tail, int size) {
        assert size <= 32 || root != null;
        assert tail != null;

        this.root = root;
        this.tail = tail;
        this.size = size;
    }

    public static ByteVector EMPTY = new ByteVector(null, new byte[32], 0);

    public static ByteVector of(byte... bytes) {
        return of(bytes, bytes.length);
    }

    static ByteVector of(byte[] bytes, int size) {
        if (size <= 32) {
            return new ByteVector(null, Arrays.copyOf(bytes, 32), size);
        } else {
            // split input into chunks of 32, plus tail
            int n = (size - 1) >>> 5;
            Object[] temp = new Object[n];
            int index = 0;
            for (int i = 0; i < n; ++i) {
                temp[i] = Arrays.copyOfRange(bytes, index, index += 32);
            }
            byte[] tail = Arrays.copyOfRange(bytes, index, index + 32);

            // grow tree towards root
            while (n > 1) {
                int m = (n - 1) >>> 5;
                index = 0;
                for (int i = 0; i < m; ++i) {
                    temp[i] = Arrays.copyOfRange(temp, index, index += 32);
                }
                temp[m] = Arrays.copyOfRange(temp, index, n);
                n = m + 1;
            }
            return new ByteVector(temp[0], tail, size);
        }
    }

    public byte[] toArray() {
        byte[] bytes = new byte[size];
        copyIntoArray(bytes, 0);
        return bytes;
    }

    public void copyIntoArray(byte[] bytes, int offset) {
        int index = 0;
        if (size > 32) {
            index = copyIntoArray(root, 0, bytes, offset);
        }
        System.arraycopy(tail, 0, bytes, offset + index, size - index);
    }

    private int copyIntoArray(Object root, int index, byte[] bytes, int offset) {
        if (root instanceof Object[]) {
            for (Object r : (Object[]) root) {
                if (index + 32 >= size) break;
                index = copyIntoArray(r, index, bytes, offset);
            }
            return index;
        } else {
            System.arraycopy(root, 0, bytes, offset + index, 32);
            return index + 32;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public byte top() {
        if (isEmpty()) throw new IllegalStateException("top on empty vector");

        return tail[(size - 1) & 31];
    }

    public byte byteAt(int index) {
        return tailOrLeafContaining(index)[index & 31];
    }

    public byte[] tailOrLeafContaining(int index) {
        if ((index >>> 5) >= (size - 1) >>> 5) {
            return tail;
        } else {
            return leafContaining(index);
        }
    }

    private byte[] leafContaining(int index) {
        Object root = this.root;
        for (int shift = shift(size); shift > 0; shift -= 5) {
            root = ((Object[]) root)[(index >>> shift) & 31];
        }
        return (byte[]) root;
    }

    private static int shift(int size) {
        int maxTreeIndex = size - 33;
        int leadingZeros = Integer.numberOfLeadingZeros(maxTreeIndex);
        return "\0\36\31\31\31\31\31\24\24\24\24\24\17\17\17\17\17\12\12\12\12\12\5\5\5\5\5\0\0\0\0\0\0".charAt(leadingZeros);
    }

    public ByteVector push(byte x) {
        final int tailIndex = tailIndex();
        if (tailIndex > 0 || isEmpty()) {
            // same leaf count
            return new ByteVector(root, tailWith(tailIndex, x), size + 1);
        } else if (!isPowerOf32(size - 32)) {
            // same height
            return new ByteVector(integrate(root, shift(size + 1)), tailOf(x), size + 1);
        } else {
            // increased height
            return new ByteVector(integrate(new Object[]{root}, shift(size + 1)), tailOf(x), size + 1);
        }
    }

    private int tailIndex() {
        return size & 31;
    }

    private byte[] tailWith(int index, byte value) {
        byte[] tail = this.tail;
        if (tail[index] != value) {
            if (tail[index] != 0) {
                System.arraycopy(tail, 0, tail = new byte[32], 0, index);
            }
            tail[index] = value;
        }
        return tail;
    }

    private static byte[] tailOf(byte first) {
        byte[] tail = new byte[32];
        tail[0] = first;
        return tail;
    }

    private Object integrate(Object root, int shift) {
        if (shift == 0) {
            return tail;
        } else {
            int index = ((size - 32) >>> shift) & 31;
            Object[] newRoot = copy(root, index + 1);
            newRoot[index] = integrate(newRoot[index], shift - 5);
            return newRoot;
        }
    }

    private static Object[] copy(Object root, int size) {
        if (root == null) {
            return new Object[1];
        } else {
            return Arrays.copyOf((Object[]) root, size);
        }
    }

    public ByteVector pop() {
        if (isEmpty()) throw new IllegalStateException("pop on empty vector");

        if (tailIndex() != 1) {
            // same leaf count
            return new ByteVector(root, tail, size - 1);
        } else if (!isPowerOf32(size - 33)) {
            // same height
            return new ByteVector(root, tailOrLeafContaining(size - 2), size - 1);
        } else {
            // reduced height
            return new ByteVector(((Object[]) root)[0], tailOrLeafContaining(size - 2), size - 1);
        }
    }

    public ByteVector take(int n) {
        if (n <= 0) {
            return EMPTY;
        } else if (n == size) {
            return this;
        } else {
            Object root = this.root;
            for (int shift = shift(size), targetShift = shift(n); shift > targetShift; shift -= 5) {
                root = ((Object[]) root)[0];
            }
            return new ByteVector(root, tailOrLeafContaining(n - 1), n);
        }
    }

    private static boolean isPowerOf32(int x) {
        return (x & (x - 1)) == 0 && (0b01000010000100001000010000100001 << Integer.numberOfLeadingZeros(x)) < 0;
        //     zero or power of 2      32^6 32^5 32^4 32^3 32^2 32^1 32^0
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ByteVector && equals((ByteVector) obj);
    }

    boolean equals(ByteVector that) {
        if (this.size != that.size) return false;

        int i;
        for (i = 0; i + 32 < size; i += 32) {
            if (!Arrays.equals(this.leafContaining(i), that.leafContaining(i))) return false;
        }

        for (; i < size; ++i) {
            if (this.tail[i & 31] != that.tail[i & 31]) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        int i;
        for (i = 0; i + 32 < size; i += 32) {
            for (byte b : leafContaining(i)) {
                hash = hash * 31 + b;
            }
        }

        for (; i < size; ++i) {
            hash = hash * 31 + tail[i & 31];
        }

        return hash;
    }

    @Override
    public String toString() {
        return new String(toArray(), StandardCharsets.ISO_8859_1);
    }
}
