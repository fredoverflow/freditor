package freditor.vector;

abstract class IntVectorN extends IntVector {
    protected final int length;
    // Maintenance note: Don't try to pull the int[] tail member up
    // from the subclasses! You won't be able to initialize it
    // without duplicating the leaf method logic inside all super calls.

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

    // TAIL

    static int[] makeTail(int firstEntry) {
        int[] array = new int[32];
        array[0] = firstEntry;
        return array;
    }

    static int[] storeInto(int[] array, int index, int entry) {
        array[index] = entry;
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

    static int[] with(int[] array, int index, int entry) {
        if (array[index] == entry) return array;
        int[] old = array;
        array = new int[32];
        System.arraycopy(old, 0, array, 0, index);
        array[index] = entry;
        return array;
    }

    static int[][] with(int[][] array, int index, int[] entry) {
        if (array[index] == entry) return array;
        if (array[index] != null) {
            int[][] old = array;
            array = new int[32][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][] with(int[][][] array, int index, int[][] entry) {
        if (array[index] == entry) return array;
        if (array[index] != null) {
            int[][][] old = array;
            array = new int[32][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][] with(int[][][][] array, int index, int[][][] entry) {
        if (array[index] == entry) return array;
        if (array[index] != null) {
            int[][][][] old = array;
            array = new int[32][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][][] with(int[][][][][] array, int index, int[][][][] entry) {
        if (array[index] == entry) return array;
        if (array[index] != null) {
            int[][][][][] old = array;
            array = new int[32][][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }

    static int[][][][][][] with(int[][][][][][] array, int index, int[][][][][] entry) {
        if (array[index] == entry) return array;
        if (array[index] != null) {
            int[][][][][][] old = array;
            array = new int[32][][][][][];
            System.arraycopy(old, 0, array, 0, index);
        }
        array[index] = entry;
        return array;
    }
}
