package freditor;

import static freditor.Maths.atLeastZero;

public class JavaIndenter extends Indenter {
    public static final JavaIndenter instance = new JavaIndenter();

    @Override
    public String synthesizeOnEnterAfter(char previousCharTyped) {
        return previousCharTyped == '{' ? "\n}" : "";
    }

    @Override
    public int[] corrections(Freditor freditor) {
        final int rows = freditor.rows();
        int[] corrections = new int[rows];
        int indentation = 0;
        for (int row = 0; row < rows; ++row) {
            int home = freditor.homePositionOfRow(row);
            int end = freditor.endPositionOfRow(row);
            corrections[row] = atLeastZero(indentation + leadingClosers(freditor, home, end)) - freditor.leadingSpaces(home);
            indentation = atLeastZero(indentation + openersAndClosers(freditor, home, end));
        }
        return corrections;
    }

    private int leadingClosers(Freditor freditor, int home, int end) {
        int difference = 0;
        int space = Flexer.FIRST_SPACE;
        for (int i = home; i < end; ++i) {
            int state = freditor.stateAt(i);
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

    private int openersAndClosers(Freditor freditor, int home, int end) {
        int difference = 0;
        for (int i = home; i < end; ++i) {
            int state = freditor.stateAt(i);
            difference += indentationDelta(state);
        }
        return difference;
    }

    private int indentationDelta(int state) {
        switch (state) {
            case Flexer.OPENING_PAREN:
            case Flexer.OPENING_BRACKET:
            case Flexer.OPENING_BRACE:
                return +4;

            case Flexer.CLOSING_PAREN:
            case Flexer.CLOSING_BRACKET:
            case Flexer.CLOSING_BRACE:
                return -4;

            default:
                return 0;
        }
    }
}
