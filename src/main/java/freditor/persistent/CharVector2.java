package freditor.persistent;

final class CharVector2 extends CharVectorN {
    private final char[][] root;
    private final char[] tail;

    CharVector2(char[] full, char x) {
        super(CAPACITY_1 + 1);

        root = new char[32][];
        root[0] = full;
        root[1] = tail = makeTail(x);
    }

    private CharVector2(int length, char[][] root, char[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    CharVector2(int length, char[][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    @Override
    protected char[] leaf(int i) {
        return root[i >>> 5];
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
        if (tail[a] == 0) return new CharVector2(length + 1, root, storeInto(tail, a, x));
        if (length == CAPACITY_2) return new CharVector3(root, x);

        int b = length >>> 5;

        char[][] B = root;
        char[] A = lazy(B[b]);

        A = with(A, a, x);
        B = with(B, b, A);

        return new CharVector2(length + 1, B);
    }

    @Override
    public CharVector pop() {
        int len1 = length - 1;
        if (len1 == CAPACITY_1) return new CharVector1(len1, root[0]);
        return new CharVector2(len1, root);
    }

    @Override
    public CharVector take(int n) {
        if (n == length) return this;
        if (n == CAPACITY_0) return CharVector.empty;
        if (n <= CAPACITY_1) return new CharVector1(n, root[0]);
        return new CharVector2(n, root);
    }
}
