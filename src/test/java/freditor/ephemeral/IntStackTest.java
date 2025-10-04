package freditor.ephemeral;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntStackTest {
    @Test
    public void emptyStack() {
        IntStack stack = new IntStack();

        assertTrue(stack.isEmpty());
        assertFalse(stack.isFull());
        assertEquals(0, stack.size());
    }

    @Test
    public void pushOneValue() {
        IntStack stack = new IntStack();
        stack.push(11);

        assertFalse(stack.isEmpty());
        assertFalse(stack.isFull());

        assertEquals(1, stack.size());
        assertEquals(11, stack.get(0));
    }

    @Test
    public void pushTwoValues() {
        IntStack stack = new IntStack();
        stack.push(11);
        stack.push(13);

        assertFalse(stack.isEmpty());
        assertFalse(stack.isFull());

        assertEquals(2, stack.size());
        assertEquals(11, stack.get(0));
        assertEquals(13, stack.get(1));
    }

    @Test
    public void topOfThePops() {
        IntStack stack = new IntStack();
        stack.push(11);
        stack.push(13);
        stack.push(17);
        stack.push(19);

        assertEquals(19, stack.top());
        assertEquals(19, stack.pop());
        assertEquals(17, stack.top());
        assertEquals(17, stack.pop());
        assertEquals(13, stack.top());
        assertEquals(13, stack.pop());
        assertEquals(11, stack.top());
        assertEquals(11, stack.pop());
    }

    @Test
    public void fill() {
        IntStack stack = new IntStack(2, 3, 5, 7, 11, 13, 17, 19);
        assertFalse(stack.isEmpty());
        assertTrue(stack.isFull());

        assertEquals(8, stack.size());
        assertEquals(2, stack.get(0));
        assertEquals(19, stack.get(7));
    }

    @Test
    public void grow() {
        IntStack stack = new IntStack(2, 3, 5, 7, 11, 13, 17, 19);
        stack.push(23);

        assertFalse(stack.isEmpty());
        assertFalse(stack.isFull());

        assertEquals(9, stack.size());
        assertEquals(2, stack.get(0));
        assertEquals(23, stack.get(8));
    }

    @Test
    public void shrink() {
        IntStack stack = new IntStack(2, 3, 5, 7, 11, 13, 17, 19);

        stack.shrinkToSize(5);
        assertEquals(5, stack.size());
        assertEquals(2, stack.get(0));
        assertEquals(11, stack.get(4));

        stack.clear();
        assertTrue(stack.isEmpty());
    }

    @Test
    public void binarySearch() {
        IntStack stack = new IntStack(2, 3, 5, 7, 11, 13, 17, 19);

        assertEquals(0, stack.binarySearch(0));
        assertEquals(0, stack.binarySearch(1));
        assertEquals(0, stack.binarySearch(2));

        assertEquals(1, stack.binarySearch(3));

        assertEquals(2, stack.binarySearch(4));
        assertEquals(2, stack.binarySearch(5));

        assertEquals(3, stack.binarySearch(6));
        assertEquals(3, stack.binarySearch(7));

        assertEquals(4, stack.binarySearch(8));
        assertEquals(4, stack.binarySearch(9));
        assertEquals(4, stack.binarySearch(10));
        assertEquals(4, stack.binarySearch(11));

        assertEquals(5, stack.binarySearch(12));
        assertEquals(5, stack.binarySearch(13));

        assertEquals(6, stack.binarySearch(14));
        assertEquals(6, stack.binarySearch(15));
        assertEquals(6, stack.binarySearch(16));
        assertEquals(6, stack.binarySearch(17));

        assertEquals(7, stack.binarySearch(18));
        assertEquals(7, stack.binarySearch(19));

        assertEquals(8, stack.binarySearch(20));
    }
}
