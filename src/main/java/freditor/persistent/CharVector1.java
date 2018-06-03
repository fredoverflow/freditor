package freditor.persistent;

final class CharVector1 extends CharVectorN {
    private final char[] tail;

    CharVector1(char x) {
        super(1);
        tail = makeTail(x);
    }

    CharVector1(int length, char[] tail) {
        super(length);
        this.tail = tail;
    }

    @Override
    protected char[] leaf(int index) {
        return tail;
    }

    @Override
    public char charAt(int index) {
        return tail[index];
    }

    @Override
    public char top() {
        return tail[length - 1];
    }

    @Override
    public CharVector push(char x) {
        if (length == CAPACITY_1) return new CharVector2(tail, x);
        if (tail[length] == 0) return new CharVector1(length + 1, storeInto(tail, length, x));
        return new CharVector1(length + 1, with(tail, length, x));
    }

    @Override
    public CharVector pop() {
        int len1 = length - 1;
        if (len1 == CAPACITY_0) return CharVector.empty;
        return new CharVector1(len1, tail);
    }

    @Override
    public CharVector take(int n) {
        if (n == length) return this;
        if (n == CAPACITY_0) return CharVector.empty;
        return new CharVector1(n, tail);
    }
}
