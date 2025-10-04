package freditor;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static freditor.Maths.atLeastZero;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathsTest {
    @Test
    public void atLeastZeroNegative() {
        int r = new Random().nextInt();
        for (int x = -1; x < 0; x += x + (r & 1), r >>>= 1) {
            assertEquals(0, atLeastZero(x));
        }
    }

    @Test
    public void atLeastZeroZero() {
        assertEquals(0, atLeastZero(0));
    }

    @Test
    public void atLeastZeroPositive() {
        int r = new Random().nextInt();
        for (int x = 1; x > 0; x += x + (r & 1), r >>>= 1) {
            assertEquals(x, atLeastZero(x));
        }
    }
}
