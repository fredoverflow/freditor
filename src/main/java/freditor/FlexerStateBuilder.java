package freditor;

import java.util.Arrays;

public class FlexerStateBuilder {
    private final FlexerState[] next = new FlexerState[128];

    public FlexerStateBuilder set(char key, FlexerState value) {
        next[key] = value;
        return this;
    }

    public FlexerStateBuilder set(String ranges, FlexerState value) {
        final int len = ranges.length();
        for (int from = 0, to = 1; to < len; from += 2, to += 2) {
            char f = ranges.charAt(from);
            char t = ranges.charAt(to);
            assert f <= t : f + " > " + t;
            assert (f < 64) == (t < 64) : f + ".." + t + " crosses lo/hi boundary";
            Arrays.fill(next, f, t + 1, value);
        }
        return this;
    }

    public FlexerState build() {
        return new FlexerState(next);
    }
}
