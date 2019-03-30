package freditor;

import java.util.Arrays;
import java.util.Objects;

public class FlexerState {
    private FlexerState defaultValue;
    private final FlexerState[] next;
    private final long lo, hi;
    private final boolean isHead;

    private FlexerState() {
        next = new FlexerState[0];
        lo = 0;
        hi = 0;
        isHead = false;
    }

    public static final FlexerState EMPTY = new FlexerState();

    public static final FlexerState THIS = EMPTY;

    private FlexerState resolveThis(FlexerState state) {
        return state == THIS ? this : state;
    }

    public FlexerState(char c, FlexerState state) {
        next = new FlexerState[]{resolveThis(state)};
        if (c < 64) {
            lo = 1L << c;
            hi = 0;
        } else {
            lo = 0;
            hi = 1L << c;
        }
        isHead = false;
    }

    public FlexerState(char c1, FlexerState state1, char c2, FlexerState state2) {
        assert c1 < c2 : c1 + " >= " + c2;
        next = new FlexerState[]{resolveThis(state1), resolveThis(state2)};
        long lo = 0;
        long hi = 0;
        if (c1 < 64) {
            lo |= 1L << c1;
        } else {
            hi |= 1L << c1;
        }
        if (c2 < 64) {
            lo |= 1L << c2;
        } else {
            hi |= 1L << c2;
        }
        this.lo = lo;
        this.hi = hi;
        isHead = false;
    }

    public FlexerState(char c1, FlexerState state1, char c2, FlexerState state2, char c3, FlexerState state3) {
        assert c1 < c2 : c1 + " >= " + c2;
        assert c2 < c3 : c2 + " >= " + c3;
        next = new FlexerState[]{resolveThis(state1), resolveThis(state2), resolveThis(state3)};
        long lo = 0;
        long hi = 0;
        if (c1 < 64) {
            lo |= 1L << c1;
        } else {
            hi |= 1L << c1;
        }
        if (c2 < 64) {
            lo |= 1L << c2;
        } else {
            hi |= 1L << c2;
        }
        if (c3 < 64) {
            lo |= 1L << c3;
        } else {
            hi |= 1L << c3;
        }
        this.lo = lo;
        this.hi = hi;
        isHead = false;
    }

    public FlexerState(String ranges, FlexerState state) {
        final int len = ranges.length();
        int n = 0;
        for (int from = 0, to = 1; to < len; from += 2, to += 2) {
            char f = ranges.charAt(from);
            char t = ranges.charAt(to);
            assert f <= t : f + " > " + t;
            assert (f < 64) == (t < 64) : f + ".." + t + " crosses lo/hi boundary";
            n += t + 1 - f;
        }
        next = new FlexerState[n];
        Arrays.fill(next, resolveThis(state));
        long lo = 0;
        long hi = 0;
        for (int from = 0, to = 1; to < len; from += 2, to += 2) {
            int f = ranges.charAt(from);
            int t = ranges.charAt(to);
            long m = ((1L << (t + 1 - f)) - 1) << f;
            if (f < 64) {
                lo |= m;
            } else {
                hi |= m;
            }
        }
        this.lo = lo;
        this.hi = hi;
        isHead = false;
    }

    public FlexerState(FlexerState[] uncompressed) {
        assert uncompressed.length == 128 : uncompressed.length + " != 128";
        int n = (int) Arrays.stream(uncompressed).filter(Objects::nonNull).count();
        FlexerState[] compressed = new FlexerState[n];
        int k = 0;
        long lo = 0;
        for (int i = 0; i < 64; ++i) {
            if (uncompressed[i] != null) {
                compressed[k++] = resolveThis(uncompressed[i]);
                lo |= 1L << i;
            }
        }
        long hi = 0;
        for (int i = 64; i < 128; ++i) {
            if (uncompressed[i] != null) {
                compressed[k++] = resolveThis(uncompressed[i]);
                hi |= 1L << i;
            }
        }
        assert k == n : k + " != " + n;
        this.next = compressed;
        this.lo = lo;
        this.hi = hi;
        isHead = false;
    }

    public FlexerState tail() {
        return new FlexerState(this, false);
    }

    public FlexerState head() {
        return new FlexerState(this, true);
    }

    private FlexerState(FlexerState that, boolean isHead) {
        defaultValue = that.defaultValue;
        next = that.next;
        lo = that.lo;
        hi = that.hi;
        this.isHead = isHead;
    }

    public FlexerState with(char c, FlexerState state) {
        return new FlexerState(this, c, resolveThis(state));
    }

    private FlexerState(FlexerState that, char c, FlexerState state) {
        long lo = that.lo;
        long hi = that.hi;

        long m = 1L << c;
        if (c < 64) {
            int i = Long.bitCount(lo & (m - 1));
            if ((lo & m) != 0) {
                next = replace(that.next, i, state);
            } else {
                next = insert(that.next, i, state);
                lo |= m;
            }
        } else {
            int i = Long.bitCount(hi & (m - 1)) + Long.bitCount(lo);
            if ((hi & m) != 0) {
                next = replace(that.next, i, state);
            } else {
                next = insert(that.next, i, state);
                hi |= m;
            }
        }
        this.lo = lo;
        this.hi = hi;
        isHead = that.isHead;
    }

    private static FlexerState[] replace(FlexerState[] next, int index, FlexerState state) {
        FlexerState[] result = next.clone();
        result[index] = state;
        return result;
    }

    private static FlexerState[] insert(FlexerState[] next, int index, FlexerState state) {
        FlexerState[] result = new FlexerState[next.length + 1];
        System.arraycopy(next, 0, result, 0, index);
        System.arraycopy(next, index, result, index + 1, next.length - index);
        result[index] = state;
        return result;
    }

    public FlexerState verbatim(FlexerState end, String... lexemes) {
        FlexerState state = this;
        for (String lexeme : lexemes) {
            state = state.verbatim(end, lexeme, 0);
        }
        return state;
    }

    private FlexerState verbatim(FlexerState end, String lexeme, int index) {
        if (index == lexeme.length()) {
            return this != end ? this : end.tail();
        }

        char c = lexeme.charAt(index);
        FlexerState state = next(c);
        if (state == null) {
            state = end;
        }

        state = state.verbatim(end, lexeme, index + 1);
        if (index == 0) {
            state = state.head();
        }

        return with(c, state);
    }

    public FlexerState setDefault(FlexerState defaultValue) {
        assert this.defaultValue == null : this.defaultValue + " != null";
        this.defaultValue = resolveThis(defaultValue);
        return this;
    }

    public FlexerState next(char c) {
        long m = 1L << c;
        if (c < 64) {
            return (lo & m) == 0 ? defaultValue : next[Long.bitCount(lo & (m - 1))];
        } else {
            return (hi & m) == 0 ? defaultValue : next[Long.bitCount(hi & (m - 1)) + Long.bitCount(lo)];
        }
    }

    public FlexerState read(String word) {
        FlexerState state = this;
        final int len = word.length();
        for (int i = 0; i < len; ++i) {
            char c = word.charAt(i);
            state = state.next(c);
            assert state != null : word.substring(0, i) + " has no suffix " + word.substring(i);
        }
        return state;
    }

    public boolean isHead() {
        return isHead;
    }
}
