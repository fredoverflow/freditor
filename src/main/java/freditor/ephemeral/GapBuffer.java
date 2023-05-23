package freditor.ephemeral;

import java.util.Arrays;

public final class GapBuffer<E> {
    private static final int INITIAL_CAPACITY = 8;
    // A shift of 1 implies growth by 50%, 2 by 25%, 3 by 12% etc.
    private static final int SHIFT_ADDITIONAL_CAPACITY = 2;

    private Object[] buffer;
    private int gapStart;
    private int gapLength;

    public GapBuffer() {
        buffer = new Object[INITIAL_CAPACITY];
        gapStart = 0;
        gapLength = INITIAL_CAPACITY;
    }

    @SafeVarargs
    public GapBuffer(E... values) {
        int initialCapacity = Math.max(INITIAL_CAPACITY, values.length);
        buffer = Arrays.copyOf(values, initialCapacity);
        gapStart = values.length;
        gapLength = initialCapacity - values.length;
    }

    public int size() {
        return buffer.length - gapLength;
    }

    public boolean isEmpty() {
        return gapLength == buffer.length;
    }

    public boolean isFull() {
        return gapLength == 0;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index >= gapStart) {
            index += gapLength;
        }
        return (E) buffer[index];
    }

    @SuppressWarnings("unchecked")
    public E set(int index, E value) {
        if (index >= gapStart) {
            index += gapLength;
        }
        E previousValue = (E) buffer[index];
        buffer[index] = value;
        return previousValue;
    }

    public void add(E value) {
        add(size(), value);
    }

    public void add(int index, E value) {
        if (isFull()) {
            increaseCapacity(index);
        } else {
            moveGapTo(index);
        }
        buffer[gapStart] = value;
        ++gapStart;
        --gapLength;
    }

    private void increaseCapacity(int index) {
        Object[] old = buffer;
        gapLength = old.length >>> SHIFT_ADDITIONAL_CAPACITY;
        buffer = new Object[old.length + gapLength];
        gapStart = index;
        System.arraycopy(old, 0, buffer, 0, index);
        System.arraycopy(old, index, buffer, gapStart + gapLength, old.length - index);
    }

    private void moveGapTo(int index) {
        if (index < gapStart) {
            System.arraycopy(buffer, index, buffer, index + gapLength, gapStart - index);
            gapStart = index;
        } else if (index > gapStart) {
            System.arraycopy(buffer, gapStart + gapLength, buffer, gapStart, index - gapStart);
            gapStart = index;
        }
    }

    public void remove(int index) {
        moveGapTo(index + 1);
        --gapStart;
        ++gapLength;
    }

    public void remove(int start, int end) {
        int count = end - start;
        if (count > 0) {
            moveGapTo(end);
            gapStart -= count;
            gapLength += count;
        }
    }

    public void clear() {
        gapStart = 0;
        gapLength = buffer.length;
    }

    public Object[] toArray() {
        Object[] result = new Object[size()];
        System.arraycopy(buffer, 0, result, 0, gapStart);
        System.arraycopy(buffer, gapStart + gapLength, result, gapStart, result.length - gapStart);
        return result;
    }
}
