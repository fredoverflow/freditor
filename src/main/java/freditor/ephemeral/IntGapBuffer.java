package freditor.ephemeral;

import java.util.Arrays;

public final class IntGapBuffer {
    private static final int INITIAL_CAPACITY = 8;
    // A shift of 1 implies growth by 50%, 2 by 25%, 3 by 12% etc.
    private static final int SHIFT_ADDITIONAL_CAPACITY = 2;

    private int[] buffer;
    private int gap;
    private int suffix;

    public IntGapBuffer() {
        buffer = new int[INITIAL_CAPACITY];
        gap = 0;
        suffix = INITIAL_CAPACITY;
    }

    public IntGapBuffer(int... values) {
        int initialCapacity = Math.max(INITIAL_CAPACITY, values.length);
        buffer = Arrays.copyOf(values, initialCapacity);
        gap = values.length;
        suffix = initialCapacity;
    }

    private int prefixSize() {
        return gap;
    }

    private int gapSize() {
        return suffix - gap;
    }

    private int suffixSize() {
        return buffer.length - suffix;
    }

    public int size() {
        return prefixSize() + suffixSize();
    }

    public boolean isEmpty() {
        return gapSize() == buffer.length;
    }

    public boolean isFull() {
        return gap == suffix;
    }

    public int get(int index) {
        if (index >= gap) {
            index += gapSize();
        }
        return buffer[index];
    }

    public int set(int index, int value) {
        if (index >= gap) {
            index += gapSize();
        }
        int previousValue = buffer[index];
        buffer[index] = value;
        return previousValue;
    }

    public void add(int value) {
        add(size(), value);
    }

    public void add(int index, int value) {
        if (isFull()) {
            increaseCapacity(index);
        } else {
            moveGapTo(index);
        }
        buffer[gap++] = value;
    }

    private void increaseCapacity(int index) {
        int additionalCapacity = buffer.length >>> SHIFT_ADDITIONAL_CAPACITY;
        int[] old = buffer;
        buffer = new int[old.length + additionalCapacity];
        gap = index;
        suffix = index + additionalCapacity;
        System.arraycopy(old, 0, buffer, 0, prefixSize());
        System.arraycopy(old, index, buffer, suffix, suffixSize());
    }

    private void moveGapTo(int index) {
        int delta = index - gap;
        if (delta < 0) {
            gap = index;
            suffix += delta;
            System.arraycopy(buffer, index, buffer, suffix, -delta);
        } else if (delta > 0) {
            System.arraycopy(buffer, suffix, buffer, gap, delta);
            gap = index;
            suffix += delta;
        }
    }

    public int remove(int index) {
        moveGapTo(index + 1);
        return buffer[--gap];
    }

    public void remove(int start, int end) {
        assert start <= end;
        moveGapTo(end);
        gap -= end - start;
    }

    public void clear() {
        gap = 0;
        suffix = buffer.length;
    }

    public int[] toArray() {
        int[] temp = new int[size()];
        System.arraycopy(buffer, 0, temp, 0, prefixSize());
        System.arraycopy(buffer, suffix, temp, prefixSize(), suffixSize());
        return temp;
    }
}
