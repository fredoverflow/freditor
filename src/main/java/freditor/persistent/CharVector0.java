package freditor.persistent;

final class CharVector0 extends CharVector {
    CharVector0() {
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
    public char charAt(int index) {
        throw new AssertionError("charAt on empty vector");
    }

    @Override
    public char top() {
        throw new AssertionError("top on empty vector");
    }

    @Override
    public CharVector push(char x) {
        return new CharVector1(x);
    }

    @Override
    public CharVector pop() {
        throw new AssertionError("pop on empty vector");
    }

    @Override
    public CharVector take(int n) {
        assert n == 0;
        return this;
    }

    @Override
    public char[] copyIntoArray(char[] temp, int offset) {
        return temp;
    }
}
