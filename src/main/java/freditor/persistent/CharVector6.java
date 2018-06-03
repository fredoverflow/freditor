package freditor.persistent;

final class CharVector6 extends CharVectorN {
    private final char[][][][][][] root;
    private final char[] tail;

    CharVector6(char[][][][][] full, char x) {
        super(CAPACITY_5 + 1);

        root = new char[32][][][][][];
        root[0] = full;
        root[1] = new char[32][][][][];
        root[1][0] = new char[32][][][];
        root[1][0][0] = new char[32][][];
        root[1][0][0][0] = new char[32][];
        root[1][0][0][0][0] = tail = makeTail(x);
    }

    private CharVector6(int length, char[][][][][][] root, char[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    CharVector6(int length, char[][][][][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    @Override
    protected char[] leaf(int i) {
        return root[i >>> 25][(i >>> 20) & 31][(i >>> 15) & 31][(i >>> 10) & 31][(i >>> 5) & 31];
    }

    @Override
    public char charAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public char top() {
        return tail[(length - 1) & 31];
    }

    @Override
    public CharVector push(char x) {
        int a = length & 31;
        if (tail[a] == 0) return new CharVector6(length + 1, root, storeInto(tail, a, x));
        if (length == CAPACITY_6) throw new AssertionError("vector exhausted");

        int b = (length >>> 5) & 31;
        int c = (length >>> 10) & 31;
        int d = (length >>> 15) & 31;
        int e = (length >>> 20) & 31;
        int f = length >>> 25;

        char[][][][][][] F = root;
        char[][][][][] E = lazy(F[f]);
        char[][][][] D = lazy(E[e]);
        char[][][] C = lazy(D[d]);
        char[][] B = lazy(C[c]);
        char[] A = lazy(B[b]);

        A = with(A, a, x);
        B = with(B, b, A);
        C = with(C, c, B);
        D = with(D, d, C);
        E = with(E, e, D);
        F = with(F, f, E);

        return new CharVector6(length + 1, F);
    }

    @Override
    public CharVector pop() {
        int len1 = length - 1;
        if (len1 == CAPACITY_5) return new CharVector5(len1, root[0]);
        return new CharVector6(len1, root);
    }

    @Override
    public CharVector take(int n) {
        if (n == length) return this;
        if (n == CAPACITY_0) return CharVector.empty;
        if (n <= CAPACITY_1) return new CharVector1(n, root[0][0][0][0][0]);
        if (n <= CAPACITY_2) return new CharVector2(n, root[0][0][0][0]);
        if (n <= CAPACITY_3) return new CharVector3(n, root[0][0][0]);
        if (n <= CAPACITY_4) return new CharVector4(n, root[0][0]);
        if (n <= CAPACITY_5) return new CharVector5(n, root[0]);
        return new CharVector6(n, root);
    }
}
