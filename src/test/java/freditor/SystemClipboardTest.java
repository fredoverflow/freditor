package freditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class SystemClipboardTest {
    @BeforeEach
    public void skipSystemClipboardTestsInHeadlessEnvironment() {
        assumeFalse(GraphicsEnvironment.isHeadless());
    }

    @Test
    public void fox() {
        SystemClipboard.set("the quick brown fox jumps over the lazy dog");
        assertEquals("the quick brown fox jumps over the lazy dog", SystemClipboard.getVisibleLatin1());
    }

    private static String codePoints(int startInclusive, int endExclusive) {
        int[] codePoints = IntStream.range(startInclusive, endExclusive).toArray();
        return new String(codePoints, 0, endExclusive - startInclusive);
    }

    @Test
    public void invisibleControlCharacters() {
        String input = codePoints(0, 32);
        String output = "    \n";
        SystemClipboard.set(input);
        assertEquals(output, SystemClipboard.getVisibleLatin1().toString());
    }

    @Test
    public void visibleAscii() {
        String input = codePoints(31, 128);
        String output = codePoints(32, 127);
        SystemClipboard.set(input);
        assertEquals(output, SystemClipboard.getVisibleLatin1().toString());
    }

    @Test
    public void visibleLatin1AfterAscii() {
        String input = codePoints(127, 257);
        String output = codePoints(160, 256);
        SystemClipboard.set(input);
        assertEquals(output, SystemClipboard.getVisibleLatin1().toString());
    }
}
