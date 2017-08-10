package freditor.vector;

final class IntVector2 extends IntVectorN {
    private final int[][] root;
    private final int[] tail;

    IntVector2(int[] full, int x) {
        super(CAPACITY_1 + 1);

        root = new int[32][];
        root[0] = full;
        root[1] = tail = makeTail(x);
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
    public int top() {
        return tail[(length - 1) & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) return new IntVector2(length + 1, root, storeInto(tail, a, x));
        if (length == CAPACITY_2) return new IntVector3(root, x);

        int b = length >>> 5;

        int[][] B = root;
        int[] A = lazy(B[b]);

        A = with(A, a, x);
        B = with(B, b, A);

        return new IntVector2(length + 1, B);
    }

    @Override
    public IntVector pop() {
        int len1 = length - 1;
        if (len1 == CAPACITY_1) return new IntVector1(len1, root[0]);
        return new IntVector2(len1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == CAPACITY_0) return IntVector.empty;
        if (n <= CAPACITY_1) return new IntVector1(n, root[0]);
        return new IntVector2(n, root);
    }
}
