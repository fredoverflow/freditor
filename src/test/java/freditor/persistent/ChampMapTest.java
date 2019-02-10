package freditor.persistent;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChampMapTest {
    private static final String[] NUMBERS = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    };

    @Test
    public void level2() {
        ChampMap<String, Integer> numbers = ChampMap.empty();
        for (int i = 0; i < NUMBERS.length; ++i) {
            numbers = numbers.put(NUMBERS[i], i);
            for (int k = 0; k <= i; ++k) {
                assertEquals((Integer) k, numbers.get(NUMBERS[k]));
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

        ChampMap<String, String> map = ChampMap.of(a, a);

        assertEquals(a, map.get(a));
        assertNull(map.get(b));
    }

    @Test
    public void putSameHash() {
        String a = "aa";
        String b = "bB";
        assertEquals(a.hashCode(), b.hashCode());

        ChampMap<String, String> map = ChampMap.of(a, a, b, b);

        assertEquals(a, map.get(a));
        assertEquals(b, map.get(b));

        assertEquals("[[[[[[[[aa, aa, bB, bB]]]]]]]]", map.toString());
    }

    @Test
    public void level7() {
        ChampMap<Integer, String> map = ChampMap.of(
                3 << 30, "3",
                1 << 30, "1",
                2 << 30, "2",
                0, "0");

        assertEquals("[[[[[[[0, 0, 1073741824, 1, -2147483648, 2, -1073741824, 3]]]]]]]", map.toString());
    }

    @Test
    public void putExistingKey() {
        ChampMap<Integer, Integer> odds = ChampMap.of(
                0, 1,
                1, 3,
                2, 5,
                3, 7);
        ChampMap<Integer, Integer> primes = odds.put(0, 2);

        assertEquals("[0, 1, 1, 3, 2, 5, 3, 7]", odds.toString());
        assertEquals("[0, 2, 1, 3, 2, 5, 3, 7]", primes.toString());
    }

    @Test
    public void putSameHashInDifferentOrder() {
        String a = "aa";
        String b = "bB";
        assertEquals(a.hashCode(), b.hashCode());

        ChampMap<String, String> ab = ChampMap.of(a, a, b, b);
        ChampMap<String, String> ba = ChampMap.of(b, b, a, a);
        assertEquals(ab, ba);

        ChampMap<ChampMap<String, String>, String> test = ChampMap.of(ab, "first", ba, "second");
        assertEquals("second", test.get(ab));
    }
}
