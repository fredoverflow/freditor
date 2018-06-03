package freditor.persistent;

import java.util.Arrays;

public abstract class CharVector implements CharSequence {
    public static final CharVector empty = new CharVector0();

    public static CharVector of(String value) {
        int len = value.length();
        if (len == 0) return CharVector.empty;
        char[] values = value.toCharArray();
        if (len <= 32) return new CharVector1(len, Arrays.copyOf(values, 32));

        CharVector temp = CharVector.empty;
        for (char x : values) {
            temp = temp.push(x);
        }
        return temp;
    }

    public abstract boolean isEmpty();

    public abstract int length();

    public abstract char charAt(int index);

    public abstract char top();

    public abstract CharVector push(char x);

    public abstract CharVector pop();

    public abstract CharVector take(int n);

    public char[] toArray() {
        return copyIntoArray(new char[length()], 0);
    }

    public abstract char[] copyIntoArray(char[] temp, int offset);

    @Override
    public String toString() {
        return new String(toArray());
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(toArray(), start, end - start);
    }

    // CAPACITY CONSTANTS

    static final int CAPACITY_0 = 0;
    static final int CAPACITY_1 = 32;
    static final int CAPACITY_2 = 32 * 32;
    static final int CAPACITY_3 = 32 * 32 * 32;
    static final int CAPACITY_4 = 32 * 32 * 32 * 32;
    static final int CAPACITY_5 = 32 * 32 * 32 * 32 * 32;
    static final int CAPACITY_6 = 32 * 32 * 32 * 32 * 32 * 32;
}
