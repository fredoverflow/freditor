package freditor.persistent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringedValueMapTest {
    private static final String[] NUMBERS = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    };

    @Test
    public void level2() {
        StringedValueMap<String> numbers = StringedValueMap.empty();
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

        StringedValueMap<String> map = StringedValueMap.of(a);

        assertEquals(a, map.get(a));
        assertNull(map.get(b));
    }

    @Test
    public void putSameHash() {
        String a = "aa";
        String b = "bB";
        assertEquals(a.hashCode(), b.hashCode());

        StringedValueMap<String> map = StringedValueMap.of(a, b);

        assertEquals(a, map.get(a));
        assertEquals(b, map.get(b));

        assertEquals("[[[[[[[[aa, bB]]]]]]]]", map.toString());
    }

    @Test
    public void level7() {
        StringedValueMap<String> map = StringedValueMap.of("hnwsmcx", "tqygjbc", "xfjfxtf", "zsjpxah");

        assertEquals("[[[[[[[zsjpxah, hnwsmcx, xfjfxtf, tqygjbc]]]]]]]", map.toString());
    }

    @Test
    public void level8() {
        String[] sameHash = {"alxexlnb", "aqsfmkgf", "badhkgqr", "bjyitecz", "byjkrang", "dbapvvuh", "dulsiqxx", "enrvpkum", "esmwejnq", "exhwyigu", "fgxywerb", "flszldkf", "gyfaexkk", "hhvcctuw", "hwgeaqad", "ikrgsldt", "izciqhoa", "jdxjzfai", "jiskodym", "jnnldcrq", "jsilxbku", "jxdmmady", "kqjpszan", "kveqhxyr", "loktorvg", "ltfudqok", "lyauxpho", "mcvwbmyw", "onjavzou", "osebkyhy", "pbudiusf", "plkersen", "qeqhymbc", "qjlinkzg", "qogjcjsk", "qtbjwilo", "rrcnsbbh", "spdrnywa", "syyswwii", "tdouatzq", "tijuussu", "tnevjrly", "ugkyqlin", "ulfzfkbr", "uqazzizv", "wmdcqzfh", "wvydzwwp", "xaofduix", "xtzhvpmi", "xyuikofm", "ydkjtlwu", "yifkikpy", "zblnpemn", "zggoedfr"};
        Collections.shuffle(Arrays.asList(sameHash));

        StringedValueMap<String> map = StringedValueMap.of(sameHash);

        assertEquals("[[[[[[[[alxexlnb, aqsfmkgf, badhkgqr, bjyitecz, byjkrang, dbapvvuh, dulsiqxx, enrvpkum, esmwejnq, exhwyigu, fgxywerb, flszldkf, gyfaexkk, hhvcctuw, hwgeaqad, ikrgsldt, izciqhoa, jdxjzfai, jiskodym, jnnldcrq, jsilxbku, jxdmmady, kqjpszan, kveqhxyr, loktorvg, ltfudqok, lyauxpho, mcvwbmyw, onjavzou, osebkyhy, pbudiusf, plkersen, qeqhymbc, qjlinkzg, qogjcjsk, qtbjwilo, rrcnsbbh, spdrnywa, syyswwii, tdouatzq, tijuussu, tnevjrly, ugkyqlin, ulfzfkbr, uqazzizv, wmdcqzfh, wvydzwwp, xaofduix, xtzhvpmi, xyuikofm, ydkjtlwu, yifkikpy, zblnpemn, zggoedfr]]]]]]]]", map.toString());

        for (String value : sameHash) {
            assertEquals(value, map.get(value));
        }
    }

    @Test
    public void putExistingKey() {
        StringedValueMap<Object> integerZero = StringedValueMap.of(8, 4, 0, 6, 2);
        StringedValueMap<Object> stringZero = integerZero.put("0");

        assertEquals(0, integerZero.get(0));
        assertEquals("0", stringZero.get("0"));
    }

    @Test
    public void iterator() {
        StringedValueMap<Integer> numbers = StringedValueMap.of(Stream.generate(new Random(2020_08_14)::nextInt).limit(32769).toArray(Integer[]::new));
        Iterator<Integer> it = numbers.iterator();
        numbers.forEach(number -> assertEquals(number, it.next()));
    }
}

class StringHashFinder {
    public static void main(String[] args) {
        printStringsWithHashCode(0x00000000, 7);
        printStringsWithHashCode(0x40000000, 7);
        printStringsWithHashCode(0x80000000, 7);
        printStringsWithHashCode(0xc0000000, 7);

        printStringsWithHashCode(0x80000000, 8);
    }

    private static final int MAX_IMPACT_6 = "zzzzzz".hashCode() - "aaaaaa".hashCode();

    public static void printStringsWithHashCode(int target, int length) {
        System.out.printf("%nstrings of length %d with hashCode %08x%n", length, target);

        char[] a = new char[length];
        Arrays.fill(a, 'a');
        int hash = new String(a).hashCode();

        while (true) {
            int missing = target - hash;
            if (0 <= missing && missing <= MAX_IMPACT_6) {
                fillSuffix(a, missing);
            }

            int i = a.length - 6;
            int weight = 31 * 31 * 31 * 31 * 31 * 31;
            while (true) {
                if (--i < 0) return;

                ++a[i];
                hash += weight;
                if (a[i] <= 'z') break;

                a[i] -= 26;
                hash -= 26 * weight;
                weight *= 31;
            }
        }
    }

    private static void fillSuffix(char[] a, int missing) {
        int i = a.length - 6;
        for (int weight = 31 * 31 * 31 * 31 * 31; weight > 0; weight /= 31) {
            int m = missing / weight;
            if (m > 25) return;

            a[i++] = (char) ('a' + m);
            missing -= m * weight;
        }
        System.out.println(a);
    }
}
