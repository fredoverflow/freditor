package freditor.ephemeral;

import org.junit.Test;

import static org.junit.Assert.*;

public class GapBufferTest {
    @Test
    public void emptyBuffer() {
        GapBuffer<Integer> buffer = new GapBuffer<>();

        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());
        assertEquals(0, buffer.size());
    }

    @Test
    public void addOneValue() {
        GapBuffer<Integer> buffer = new GapBuffer<>();
        buffer.add(11);

        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());
        assertEquals(1, buffer.size());
        assertEquals(11, +buffer.get(0));
    }

    @Test
    public void addTwoValues() {
        GapBuffer<Integer> buffer = new GapBuffer<>();
        buffer.add(11);
        buffer.add(13);

        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());
        assertEquals(2, buffer.size());
        assertEquals(11, +buffer.get(0));
        assertEquals(13, +buffer.get(1));
    }

    @Test
    public void changeValue() {
        GapBuffer<Integer> buffer = new GapBuffer<>(3, 5, 9, 11);
        buffer.add(0, 1);

        assertEquals(1, +buffer.set(0, 2));
        assertEquals(2, +buffer.get(0));

        assertEquals(9, +buffer.set(3, 7));
        assertEquals(7, +buffer.get(3));
    }

    @Test
    public void fill() {
        GapBuffer<Integer> buffer = new GapBuffer<>(2, 3, 5, 7, 11, 13, 17, 19);

        assertFalse(buffer.isEmpty());
        assertTrue(buffer.isFull());

        assertEquals(8, buffer.size());
        assertEquals(2, +buffer.get(0));
        assertEquals(3, +buffer.get(1));
        assertEquals(5, +buffer.get(2));
        assertEquals(7, +buffer.get(3));
        assertEquals(11, +buffer.get(4));
        assertEquals(13, +buffer.get(5));
        assertEquals(17, +buffer.get(6));
        assertEquals(19, +buffer.get(7));
    }

    @Test
    public void removeFirstFromFullBuffer() {
        GapBuffer<Integer> buffer = new GapBuffer<>(2, 3, 5, 7, 11, 13, 17, 19);
        buffer.remove(0);

        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());

        assertEquals(7, buffer.size());
        assertEquals(3, +buffer.get(0));
        assertEquals(5, +buffer.get(1));
        assertEquals(7, +buffer.get(2));
        assertEquals(11, +buffer.get(3));
        assertEquals(13, +buffer.get(4));
        assertEquals(17, +buffer.get(5));
        assertEquals(19, +buffer.get(6));
    }

    @Test
    public void removeLastFromFullBuffer() {
        GapBuffer<Integer> buffer = new GapBuffer<>(2, 3, 5, 7, 11, 13, 17, 19);
        buffer.remove(7);

        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());

        assertEquals(7, buffer.size());
        assertEquals(2, +buffer.get(0));
        assertEquals(3, +buffer.get(1));
        assertEquals(5, +buffer.get(2));
        assertEquals(7, +buffer.get(3));
        assertEquals(11, +buffer.get(4));
        assertEquals(13, +buffer.get(5));
        assertEquals(17, +buffer.get(6));
    }

    @Test
    public void grow() {
        GapBuffer<Integer> buffer = new GapBuffer<>(2, 3, 5, 7, 11, 13, 17, 19);
        assertTrue(buffer.isFull());

        buffer.add(23);
        assertFalse(buffer.isFull());

        assertEquals(9, buffer.size());
        assertEquals(2, +buffer.get(0));
        assertEquals(3, +buffer.get(1));
        assertEquals(5, +buffer.get(2));
        assertEquals(7, +buffer.get(3));
        assertEquals(11, +buffer.get(4));
        assertEquals(13, +buffer.get(5));
        assertEquals(17, +buffer.get(6));
        assertEquals(19, +buffer.get(7));
        assertEquals(23, +buffer.get(8));
    }

    @Test
    public void playWithTheAlphabet() {
        GapBuffer<Integer> buffer = new GapBuffer<>();

        buffer.add(0, +'q'); // q
        buffer.add(1, +'w'); // qw
        buffer.add(0, +'e'); // eqw
        buffer.add(2, +'r'); // eqrw
        buffer.add(3, +'t'); // eqrtw
        buffer.add(5, +'z'); // eqrtwz
        buffer.add(4, +'u'); // eqrtuwz
        buffer.add(1, +'i'); // eiqrtuwz
        buffer.add(2, +'o'); // eioqrtuwz
        buffer.add(3, +'p'); // eiopqrtuwz
        buffer.add(0, +'a'); // aeiopqrtuwz
        buffer.add(7, +'s'); // aeiopqrstuwz
        buffer.add(1, +'d'); // adeiopqrstuwz
        buffer.add(3, +'f'); // adefiopqrstuwz
        buffer.add(4, +'g'); // adefgiopqrstuwz
        buffer.add(5, +'h'); // adefghiopqrstuwz
        buffer.add(7, +'j'); // adefghijopqrstuwz
        buffer.add(8, +'k'); // adefghijkopqrstuwz
        buffer.add(9, +'l'); // adefghijklopqrstuwz
        buffer.add(18, +'y'); //adefghijklopqrstuwyz
        buffer.add(18, +'x'); //adefghijklopqrstuwxyz
        buffer.add(1, +'c'); // acdefghijklopqrstuwxyz
        buffer.add(18, +'v'); //acdefghijklopqrstuvwxyz
        buffer.add(1, +'b'); // abcdefghijklopqrstuvwxyz
        buffer.add(12, +'n'); //abcdefghijklnopqrstuvwxyz
        buffer.add(12, +'m'); //abcdefghijklmnopqrstuvwxyz
        assertArrayEquals("abcdefghijklmnopqrstuvwxyz".codePoints().boxed().toArray(), buffer.toArray());

        buffer.remove(7, 16);
        assertArrayEquals("abcdefgqrstuvwxyz".codePoints().boxed().toArray(), buffer.toArray());

        buffer.remove(3, 14);
        assertArrayEquals("abcxyz".codePoints().boxed().toArray(), buffer.toArray());

        buffer.clear();
        assertTrue(buffer.isEmpty());
    }
}
