package freditor;

public class Maths {
    private Maths() {
        // prevent instantiation and inheritance
    }

    /**
     * Maps negative numbers to zero and non-negative numbers to themselves.
     */
    public static int atLeastZero(int x) {
        return x & ~(x >> 31);
    }
}
