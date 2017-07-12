package freditor;

public abstract class IntVector {
    public static final IntVector empty = IntVector0.instance;

    public abstract boolean isEmpty();

    public abstract int length();

    public abstract int intAt(int index);

    public abstract int[] copyIntoArray(int[] temp, int offset);

    public int top() {
        return intAt(length() - 1);
    }

    public abstract IntVector push(int x);

    public abstract IntVector pop();

    public abstract IntVector take(int n);

    public int binarySearch(int key) {
        int left = 0; // inclusive
        int right = length(); // exclusive
        while (left < right) {
            int middle = (left + right) >>> 1;
            int element = intAt(middle);
            if (element < key) {
                left = middle + 1;
            } else if (element > key) {
                right = middle;
            } else {
                return middle;
            }
        }
        return left;
    }
}

final class IntVector0 extends IntVector {
    static final IntVector0 instance = new IntVector0();

    private IntVector0() {
        // prevent external instantiations
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public int intAt(int index) {
        throw new AssertionError("intAt on empty vector");
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int[] copyIntoArray(int[] temp, int offset) {
        return temp;
    }

    @Override
    public IntVector push(int x) {
        return new IntVector1(x);
    }

    @Override
    public IntVector pop() {
        throw new AssertionError("pop on empty vector");
    }

    @Override
    public IntVector take(int n) {
        assert n == 0;
        return this;
    }
}

abstract class IntVectorN extends IntVector {
    protected final int length;

    IntVectorN(int length) {
        this.length = length;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int length() {
        return length;
    }

    protected abstract int[] leaf(int index);

    @Override
    public int[] copyIntoArray(int[] temp, int offset) {
        final int n = length;
        final int m = (n - 1) & ~31;
        for (int i = 0; i < m; i += 32) {
            System.arraycopy(leaf(i), 0, temp, offset + i, 32);
        }
        System.arraycopy(leaf(m), 0, temp, offset + m, n - m);
        return temp;
    }

    static int[] array32of(int x) {
        int[] array = new int[32];
        array[0] = x;
        return array;
    }

    // LAZY

    static int[] lazy(int[] array) {
        return (array != null) ? array : new int[32];
    }

    static int[][] lazy(int[][] array) {
        return (array != null) ? array : new int[32][];
    }

    static int[][][] lazy(int[][][] array) {
        return (array != null) ? array : new int[32][][];
    }

    static int[][][][] lazy(int[][][][] array) {
        return (array != null) ? array : new int[32][][][];
    }

    static int[][][][][] lazy(int[][][][][] array) {
        return (array != null) ? array : new int[32][][][][];
    }

    // WITH

    static final boolean optimize = true;

    static int[] with(int[] array, int index, int entry) {
        if (optimize && array[index] == entry) return array;
        int[] old = array;
        array = new int[32];
        System.arraycopy(old, 0, array, 0, index);
        array[index] = entry;
        return array;
    }

    static int[][] with(int[][] array, int index, int[] entry) {
        if (optimize && array[index] == entry) return array;
        if (!optimize || array[index] != null) {
            int[][] old = array;
            array = new int[32][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][] with(int[][][] array, int index, int[][] entry) {
        if (optimize && array[index] == entry) return array;
        if (!optimize || array[index] != null) {
            int[][][] old = array;
            array = new int[32][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][] with(int[][][][] array, int index, int[][][] entry) {
        if (optimize && array[index] == entry) return array;
        if (!optimize || array[index] != null) {
            int[][][][] old = array;
            array = new int[32][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][][] with(int[][][][][] array, int index, int[][][][] entry) {
        if (optimize && array[index] == entry) return array;
        if (!optimize || array[index] != null) {
            int[][][][][] old = array;
            array = new int[32][][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][][][] with(int[][][][][][] array, int index, int[][][][][] entry) {
        if (optimize && array[index] == entry) return array;
        if (!optimize || array[index] != null) {
            int[][][][][][] old = array;
            array = new int[32][][][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }
}

final class IntVector1 extends IntVectorN {
    private final int[] tail;

    IntVector1(int x) {
        super(1);
        tail = array32of(x);
    }

    IntVector1(int length, int[] tail) {
        super(length);
        this.tail = tail;
    }

    @Override
    protected int[] leaf(int index) {
        return tail;
    }

    @Override
    public int intAt(int index) {
        return tail[index];
    }

    @Override
    public IntVector push(int x) {
        if (length == 32) return new IntVector2(tail, x);
        if (tail[length] == 0) {
            tail[length] = x;
            return new IntVector1(length + 1, tail);
        }
        return new IntVector1(length + 1, with(tail, length, x));
    }

    @Override
    public IntVector pop() {
        if (length == 1) return IntVector.empty;
        return new IntVector1(length - 1, tail);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        return new IntVector1(n, tail);
    }
}

final class IntVector2 extends IntVectorN {
    private final int[][] root;
    private final int[] tail;

    IntVector2(int[] full, int x) {
        super(32 + 1);

        root = new int[32][];
        root[0] = full;
        root[1] = tail = array32of(x);
    }

    private IntVector2(int length, int[][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector2(int length, int[][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    @Override
    protected int[] leaf(int i) {
        return root[i >>> 5];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) {
            tail[a] = x;
            return new IntVector2(length + 1, root, tail);
        }
        if (length == 32 * 32) return new IntVector3(root, x);
        int b = length >>> 5;
        return new IntVector2(length + 1, with(root, b, with(tail, a, x)));
    }

    @Override
    public IntVector pop() {
        if (length == 32 + 1) return new IntVector1(length - 1, root[0]);
        return new IntVector2(length - 1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        if (n <= 32) return new IntVector1(n, root[0]);
        return new IntVector2(n, root);
    }
}

final class IntVector3 extends IntVectorN {
    private final int[][][] root;
    private final int[] tail;

    IntVector3(int[][] full, int x) {
        super(32 * 32 + 1);

        root = new int[32][][];
        root[0] = full;
        root[1] = new int[32][];
        root[1][0] = tail = array32of(x);
    }

    private IntVector3(int length, int[][][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector3(int length, int[][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    protected int[] leaf(int i) {
        return root[i >>> 10][(i >>> 5) & 31];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) {
            tail[a] = x;
            return new IntVector3(length + 1, root, tail);
        }
        if (length == 32 * 32 * 32) return new IntVector4(root, x);

        int b = (length >>> 5) & 31;
        int c = length >>> 10;

        int[][][] C = root;
        int[][] B = lazy(C[c]);
        int[] A = lazy(B[b]);

        return new IntVector3(length + 1, with(C, c, with(B, b, with(A, a, x))));
    }

    @Override
    public IntVector pop() {
        if (length == 32 * 32 + 1) return new IntVector2(length - 1, root[0]);
        return new IntVector3(length - 1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        if (n <= 32) return new IntVector1(n, root[0][0]);
        if (n <= 32 * 32) return new IntVector2(n, root[0]);
        return new IntVector3(n, root);
    }
}

final class IntVector4 extends IntVectorN {
    private final int[][][][] root;
    private final int[] tail;

    IntVector4(int[][][] full, int x) {
        super(32 * 32 * 32 + 1);

        root = new int[32][][][];
        root[0] = full;
        root[1] = new int[32][][];
        root[1][0] = new int[32][];
        root[1][0][0] = tail = array32of(x);
    }

    private IntVector4(int length, int[][][][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector4(int length, int[][][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    protected int[] leaf(int i) {
        return root[i >>> 15][(i >>> 10) & 31][(i >>> 5) & 31];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) {
            tail[a] = x;
            return new IntVector4(length + 1, root, tail);
        }

        if (length == 32 * 32 * 32 * 32) return new IntVector5(root, x);

        int b = (length >>> 5) & 31;
        int c = (length >>> 10) & 31;
        int d = length >>> 15;

        int[][][][] D = root;
        int[][][] C = lazy(D[d]);
        int[][] B = lazy(C[c]);
        int[] A = lazy(B[b]);

        return new IntVector4(length + 1, with(D, d, with(C, c, with(B, b, with(A, a, x)))));
    }

    @Override
    public IntVector pop() {
        if (length == 32 * 32 * 32 + 1) return new IntVector3(length - 1, root[0]);
        return new IntVector4(length - 1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        if (n <= 32) return new IntVector1(n, root[0][0][0]);
        if (n <= 32 * 32) return new IntVector2(n, root[0][0]);
        if (n <= 32 * 32 * 32) return new IntVector3(n, root[0]);
        return new IntVector4(n, root);
    }
}

final class IntVector5 extends IntVectorN {
    private final int[][][][][] root;
    private final int[] tail;

    IntVector5(int[][][][] full, int x) {
        super(32 * 32 * 32 * 32 + 1);

        root = new int[32][][][][];
        root[0] = full;
        root[1] = new int[32][][][];
        root[1][0] = new int[32][][];
        root[1][0][0] = new int[32][];
        root[1][0][0][0] = tail = array32of(x);
    }

    private IntVector5(int length, int[][][][][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector5(int length, int[][][][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    protected int[] leaf(int i) {
        return root[i >>> 20][(i >>> 15) & 31][(i >>> 10) & 31][(i >>> 5) & 31];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) {
            tail[a] = x;
            return new IntVector5(length + 1, root, tail);
        }
        if (length == 32 * 32 * 32 * 32 * 32) return new IntVector6(root, x);

        int b = (length >>> 5) & 31;
        int c = (length >>> 10) & 31;
        int d = (length >>> 15) & 31;
        int e = length >>> 20;

        int[][][][][] E = root;
        int[][][][] D = lazy(E[e]);
        int[][][] C = lazy(D[d]);
        int[][] B = lazy(C[c]);
        int[] A = lazy(B[b]);

        return new IntVector5(length + 1,
                with(E, e, with(D, d, with(C, c, with(B, b, with(A, a, x))))));
    }

    @Override
    public IntVector pop() {
        if (length == 32 * 32 * 32 * 32 + 1) return new IntVector4(length - 1, root[0]);
        return new IntVector5(length - 1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        if (n <= 32) return new IntVector1(n, root[0][0][0][0]);
        if (n <= 32 * 32) return new IntVector2(n, root[0][0][0]);
        if (n <= 32 * 32 * 32) return new IntVector3(n, root[0][0]);
        if (n <= 32 * 32 * 32 * 32) return new IntVector4(n, root[0]);
        return new IntVector5(n, root);
    }
}

final class IntVector6 extends IntVectorN {
    private final int[][][][][][] root;
    private final int[] tail;

    IntVector6(int[][][][][] full, int x) {
        super(32 * 32 * 32 * 32 * 32 + 1);

        root = new int[32][][][][][];
        root[0] = full;
        root[1] = new int[32][][][][];
        root[1][0] = new int[32][][][];
        root[1][0][0] = new int[32][][];
        root[1][0][0][0] = new int[32][];
        root[1][0][0][0][0] = tail = array32of(x);
    }

    private IntVector6(int length, int[][][][][][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector6(int length, int[][][][][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    protected int[] leaf(int i) {
        return root[i >>> 25][(i >>> 20) & 31][(i >>> 15) & 31][(i >>> 10) & 31][(i >>> 5) & 31];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) {
            tail[a] = x;
            return new IntVector6(length + 1, root, tail);
        }
        if (length == 32 * 32 * 32 * 32 * 32 * 32) throw new AssertionError("vector exhausted");

        int b = (length >>> 5) & 31;
        int c = (length >>> 10) & 31;
        int d = (length >>> 15) & 31;
        int e = (length >>> 20) & 31;
        int f = length >>> 25;

        int[][][][][][] F = root;
        int[][][][][] E = lazy(F[f]);
        int[][][][] D = lazy(E[e]);
        int[][][] C = lazy(D[d]);
        int[][] B = lazy(C[c]);
        int[] A = lazy(B[b]);

        return new IntVector6(length + 1,
                with(F, f, with(E, e, with(D, d, with(C, c, with(B, b, with(A, a, x)))))));
    }

    @Override
    public IntVector pop() {
        if (length == 32 * 32 * 32 * 32 * 32 + 1) return new IntVector5(length - 1, root[0]);
        return new IntVector6(length - 1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == 0) return IntVector.empty;
        if (n <= 32) return new IntVector1(n, root[0][0][0][0][0]);
        if (n <= 32 * 32) return new IntVector2(n, root[0][0][0][0]);
        if (n <= 32 * 32 * 32) return new IntVector3(n, root[0][0][0]);
        if (n <= 32 * 32 * 32 * 32) return new IntVector4(n, root[0][0]);
        if (n <= 32 * 32 * 32 * 32 * 32) return new IntVector5(n, root[0]);
        return new IntVector6(n, root);
    }
}
