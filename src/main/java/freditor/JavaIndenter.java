package freditor;

public class JavaIndenter implements Indenter {
    private final Flexer flexer;
    private CharZipper text;

    public JavaIndenter(Flexer flexer) {
        this.flexer = flexer;
    }

    @Override
    public int[] corrections(CharZipper text) {
        this.text = text;
        final int rows = text.rows();
        int[] corrections = new int[rows];
        int indentation = 0;
        int minimum = 0;
        for (int row = 0; row < rows; ++row) {
            int line = text.homePositionOfRow(row);
            corrections[row] = indentation - leadingClosers(line);
            indentation = indentation + openersMinusClosers(line);
            minimum = Math.min(minimum, indentation);
        }
        for (int row = 0; row < rows; ++row) {
            corrections[row] -= minimum;
            corrections[row] *= 4;
            corrections[row] -= text.leadingSpaces(text.homePositionOfRow(row));
        }
        return corrections;
    }

    private int leadingClosers(int i) {
        int n = 0;
        final int len = text.length();
        for (; i < len; ++i) {
            int x = text.intAt(i);
            if (flexer.indentationDelta(x >> 16) < 0) {
                ++n;
            }
            else if ((char) x != ' ') {
                return n;
            }
        }
        return n;
    }

    private int openersMinusClosers(int i) {
        int difference = 0;
        final int len = text.length();
        for (; i < len; ++i) {
            int x = text.intAt(i);
            if ((char) x == '\n') {
                return difference;
            }
            difference += flexer.indentationDelta(x >> 16);
        }
        return difference;
    }
}
