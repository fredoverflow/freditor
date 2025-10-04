package freditor;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CharZipperTest {
    private final CharZipper text = new CharZipper();

    @Test
    public void insertUnixLineSeparators() {
        insertBytesSeparatedBy("\n");
    }

    @Test
    public void insertWindowsLineSeparators() {
        insertBytesSeparatedBy("\r\n");
    }

    private void insertBytesSeparatedBy(String lineSeparator) {
        String input = lineSeparator + "one" + lineSeparator + "two" + lineSeparator + "three" + lineSeparator;
        byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
        text.loadLenient(bytes);

        byte[] output = text.toByteArray();
        byte[] expected = "\none\ntwo\nthree\n".getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(expected, output);
    }
}
