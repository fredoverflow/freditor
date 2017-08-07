package freditor.vector;

final class IntVector1 extends IntVectorN {
    private final int[] tail;

    IntVector1(int x) {
        super(1);
        tail = makeTail(x);
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
