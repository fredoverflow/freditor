package freditor.ephemeral;

import java.util.Arrays;

public final class IntStack {
    private static final int INITIAL_CAPACITY = 8;
    // A shift of 1 implies growth by 50%, 2 by 25%, 3 by 12% etc.
    private static final int SHIFT_ADDITIONAL_CAPACITY = 2;

    private int[] buffer;
    private int size;

    public IntStack() {
        buffer = new int[INITIAL_CAPACITY];
    }

    public IntStack(int... values) {
        int initialCapacity = Math.max(INITIAL_CAPACITY, values.length);
        buffer = Arrays.copyOf(values, initialCapacity);
        size = values.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == buffer.length;
    }

    public int get(int index) {
        assert index < size;
        return buffer[index];
    }

    public void push(int value) {
        if (isFull()) {
            increaseCapacity();
        }
        buffer[size++] = value;
    }

    private void increaseCapacity() {
        int additionalCapacity = buffer.length >>> SHIFT_ADDITIONAL_CAPACITY;
        buffer = Arrays.copyOf(buffer, buffer.length + additionalCapacity);
    }

    public int top() {
        return buffer[size - 1];
    }

    public int pop() {
        return buffer[--size];
    }

    public void clear() {
        size = 0;
    }

    public void shrinkToSize(int newSize) {
        assert 0 <= newSize && newSize <= size : newSize;
        size = newSize;
    }

    public int binarySearch(int key) {
        int left = 0; // inclusive
        int right = size; // exclusive
        while (left < right) {
            int middle = (left + right) >>> 1;
            int value = get(middle);
            if (value < key) {
                left = middle + 1; // inclusive
            } else if (value > key) {
                right = middle; // exclusive
            } else {
                return middle;
            }
        }
        return left;
    }
}
