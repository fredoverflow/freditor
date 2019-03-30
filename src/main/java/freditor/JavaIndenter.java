package freditor;

import freditor.persistent.ChampMap;

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
        FlexerState space = Flexer.SPACE_HEAD;
        for (int i = home; i < end; ++i) {
            FlexerState state = freditor.stateAt(i);
            Integer delta = indentationDelta.get(state);
            if (delta != null && delta < 0) {
                difference += delta;
            } else if (state != space) {
                return difference;
            }
            space = Flexer.SPACE_TAIL;
        }
        return difference;
    }

    private int openersAndClosers(Freditor freditor, int home, int end) {
        int difference = 0;
        for (int i = home; i < end; ++i) {
            FlexerState state = freditor.stateAt(i);
            Integer delta = indentationDelta.get(state);
            if (delta != null) {
                difference += delta;
            }
        }
        return difference;
    }

    private static final ChampMap<FlexerState, Integer> indentationDelta = ChampMap.<FlexerState, Integer>empty()
            .put(Flexer.OPENING_PAREN, Flexer.OPENING_BRACKET, Flexer.OPENING_BRACE, +4)
            .put(Flexer.CLOSING_PAREN, Flexer.CLOSING_BRACKET, Flexer.CLOSING_BRACE, -4);
}
