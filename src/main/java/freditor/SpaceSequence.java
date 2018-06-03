package freditor;

public class SpaceSequence implements CharSequence {
    private final int numberOfSpaces;

    private SpaceSequence(int numberOfSpaces) {
        this.numberOfSpaces = numberOfSpaces;
    }

    @Override
    public int length() {
        return numberOfSpaces;
    }

    @Override
    public char charAt(int index) {
        return ' ';
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return of(end - start);
    }

    public static SpaceSequence of(int numberOfSpaces) {
        return (numberOfSpaces < cache.length) ? cache[numberOfSpaces] : new SpaceSequence(numberOfSpaces);
    }

    private static final SpaceSequence[] cache = {
            new SpaceSequence(0),
            new SpaceSequence(1),
            new SpaceSequence(2),
            new SpaceSequence(3),
            new SpaceSequence(4),
            new SpaceSequence(5),
            new SpaceSequence(6),
            new SpaceSequence(7),
            new SpaceSequence(8),
            new SpaceSequence(9),
            new SpaceSequence(10),
            new SpaceSequence(11),
            new SpaceSequence(12),
            new SpaceSequence(13),
            new SpaceSequence(14),
            new SpaceSequence(15),
            new SpaceSequence(16),
    };
}
