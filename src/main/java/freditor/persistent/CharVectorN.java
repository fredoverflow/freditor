package freditor.persistent;

abstract class CharVectorN extends CharVector {
    protected final int length;
    // Maintenance note: Don't try to pull the char[] tail member up
    // from the subclasses! You won't be able to initialize it
    // without duplicating the leaf method logic inside all super calls.

    CharVectorN(int length) {
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

    protected abstract char[] leaf(int index);

    @Override
    public char[] copyIntoArray(char[] temp, int offset) {
        final int n = length;
        final int m = (n - 1) & ~31;
        for (int i = 0; i < m; i += 32) {
            System.arraycopy(leaf(i), 0, temp, offset + i, 32);
        }
        System.arraycopy(leaf(m), 0, temp, offset + m, n - m);
        return temp;
    }

    // TAIL

    static char[] makeTail(char firstEntry) {
        char[] array = new char[32];
        array[0] = firstEntry;
        return array;
    }

    static char[] storeInto(char[] array, int index, char entry) {
        array[index] = entry;
        return array;
    }

    // LAZY

    static char[] lazy(char[] array) {
        return (array != null) ? array : new char[32];
    }

    static char[][] lazy(char[][] array) {
        return (array != null) ? array : new char[32][];
    }

    static char[][][] lazy(char[][][] array) {
        return (array != null) ? array : new char[32][][];
    }

    static char[][][][] lazy(char[][][][] array) {
        return (array != null) ? array : new char[32][][][];
    }

    static char[][][][][] lazy(char[][][][][] array) {
        return (array != null) ? array : new char[32][][][][];
    }

    // WITH

    static char[] with(char[] array, int index, char entry) {
        if (array[index] != entry) {
            prefixCopy(array, array = new char[32], index);
            array[index] = entry;
        }
        return array;
    }

    private static void prefixCopy(Object src, Object dst, int prefixLength) {
        System.arraycopy(src, 0, dst, 0, prefixLength);
    }

    static char[][] with(char[][] array, int index, char[] entry) {
        if (array[index] != entry) {
            if (array[index] != null) prefixCopy(array, array = new char[32][], index);
            array[index] = entry;
        }
        return array;
    }

    static char[][][] with(char[][][] array, int index, char[][] entry) {
        if (array[index] != entry) {
            if (array[index] != null) prefixCopy(array, array = new char[32][][], index);
            array[index] = entry;
        }
        return array;
    }

    static char[][][][] with(char[][][][] array, int index, char[][][] entry) {
        if (array[index] != entry) {
            if (array[index] != null) prefixCopy(array, array = new char[32][][][], index);
            array[index] = entry;
        }
        return array;
    }

    static char[][][][][] with(char[][][][][] array, int index, char[][][][] entry) {
        if (array[index] != entry) {
            if (array[index] != null) prefixCopy(array, array = new char[32][][][][], index);
            array[index] = entry;
        }
        return array;
    }

    static char[][][][][][] with(char[][][][][][] array, int index, char[][][][][] entry) {
        if (array[index] != entry) {
            if (array[index] != null) prefixCopy(array, array = new char[32][][][][][], index);
            array[index] = entry;
        }
        return array;
    }
}
