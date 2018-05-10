package freditor;

public abstract class JavaIndenter extends Indenter {
    private CharZipper text;

    @Override
    public String synthesizeOnEnterAfter(char previousCharTyped) {
        return previousCharTyped == '{' ? "\n}" : "";
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
            corrections[row] = indentation + leadingClosers(line);
            indentation = indentation + openersAndClosers(line);
            minimum = Math.min(minimum, indentation);
        }
        for (int row = 0; row < rows; ++row) {
            corrections[row] -= minimum;
            corrections[row] -= text.leadingSpaces(text.homePositionOfRow(row));
        }
        return corrections;
    }

    private int leadingClosers(int i) {
        int difference = 0;
        final int len = text.length();
        for (; i < len; ++i) {
            int x = text.intAt(i);
            int delta = indentationDelta(x >> 16);
            if (delta < 0) {
                difference += delta;
            }
            else if ((char) x != ' ') {
                return difference;
            }
        }
        return difference;
    }

    private int openersAndClosers(int i) {
        int difference = 0;
        final int len = text.length();
        for (; i < len; ++i) {
            int x = text.intAt(i);
            if ((char) x == '\n') {
                return difference;
            }
            difference += indentationDelta(x >> 16);
        }
        return difference;
    }

    protected abstract int indentationDelta(int state);
}
