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
            int home = text.homePositionOfRow(row);
            int end = text.endPositionOfRow(row);
            corrections[row] = indentation + leadingClosers(home, end) - text.leadingSpaces(home);
            indentation = indentation + openersAndClosers(home, end);
            minimum = Math.min(minimum, indentation);
        }
        for (int row = 0; row < rows; ++row) {
            corrections[row] -= minimum;
        }
        return corrections;
    }

    private int leadingClosers(int home, int end) {
        int difference = 0;
        int space = Flexer.FIRST_SPACE;
        for (int i = home; i < end; ++i) {
            int state = text.stateAt(i);
            int delta = indentationDelta(state);
            if (delta < 0) {
                difference += delta;
            } else if (state != space) {
                return difference;
            }
            space = Flexer.NEXT_SPACE;
        }
        return difference;
    }

    private int openersAndClosers(int home, int end) {
        int difference = 0;
        for (int i = home; i < end; ++i) {
            int state = text.stateAt(i);
            difference += indentationDelta(state);
        }
        return difference;
    }

    protected abstract int indentationDelta(int state);
}
