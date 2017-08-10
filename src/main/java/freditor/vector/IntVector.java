package freditor.vector;

import java.util.Arrays;

public abstract class IntVector {
    public static final IntVector empty = new IntVector0();

    public static IntVector of(int... values) {
        int len = values.length;
        if (len == 0) return empty;
        if (len <= 32) return new IntVector1(len, Arrays.copyOf(values, 32));

        IntVector temp = empty;
        for (int x : values) {
            temp = temp.push(x);
        }
        return temp;
    }

    public abstract boolean isEmpty();

    public abstract int length();

    public abstract int intAt(int index);

    public int top() {
        return intAt(length() - 1);
    }

    public abstract IntVector push(int x);

    public abstract IntVector pop();

    public abstract IntVector take(int n);

    public int[] toArray() {
        return copyIntoArray(new int[length()], 0);
    }

    public abstract int[] copyIntoArray(int[] temp, int offset);

    // CAPACITY CONSTANTS

    static final int CAPACITY_0 = 0;
    static final int CAPACITY_1 = 32;
    static final int CAPACITY_2 = 32 * 32;
    static final int CAPACITY_3 = 32 * 32 * 32;
    static final int CAPACITY_4 = 32 * 32 * 32 * 32;
    static final int CAPACITY_5 = 32 * 32 * 32 * 32 * 32;
    static final int CAPACITY_6 = 32 * 32 * 32 * 32 * 32 * 32;
}
