package freditor.persistent;

import org.junit.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HamtSetTest {
    private static final String[] NUMBERS = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    };

    @Test
    public void level2() {
        HamtSet<String> numbers = HamtSet.empty();
        for (int i = 0; i < NUMBERS.length; ++i) {
            numbers = numbers.put(NUMBERS[i]);
            for (int k = 0; k <= i; ++k) {
                assertEquals(NUMBERS[k], numbers.get(NUMBERS[k]));
            }
            for (int k = i + 1; k < NUMBERS.length; ++k) {
                assertNull(numbers.get(NUMBERS[k]));
            }
        }
    }

    @Test
    public void getSameHash() {
        String a = "aa";
        String b = "bB";
        assertEquals(a.hashCode(), b.hashCode());

        HamtSet<String> set = HamtSet.of(a);

        assertEquals(a, set.get(a));
        assertNull(set.get(b));
    }

    @Test
    public void putSameHash() {
        String a = "aa";
        String b = "bB";
        assertEquals(a.hashCode(), b.hashCode());

        HamtSet<String> set = HamtSet.of(a, b);

        assertEquals(a, set.get(a));
        assertEquals(b, set.get(b));

        assertEquals("[[[[[[[[aa, bB]]]]]]]]", set.toString());
    }

    @Test
    public void level7() {
        HamtSet<Integer> set = HamtSet.of(
                3 << 30,
                1 << 30,
                2 << 30,
                0);

        assertEquals("[[[[[[[0, 1073741824, -2147483648, -1073741824]]]]]]]", set.toString());
    }

    @Test
    public void iterator() {
        HamtSet<Long> numbers = HamtSet.of(Stream.generate(new Random(2020_08_13)::nextLong).limit(1_000_000).toArray(Long[]::new));
        Iterator<Long> it = numbers.iterator();
        numbers.forEach(number -> assertEquals(number, it.next()));
    }
}
