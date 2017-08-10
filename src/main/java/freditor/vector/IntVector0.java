package freditor.vector;

final class IntVector0 extends IntVector {
    IntVector0() {
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
    public int top()  {
        throw new AssertionError("top on empty vector");
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

    @Override
    public int[] copyIntoArray(int[] temp, int offset) {
        return temp;
    }
}
