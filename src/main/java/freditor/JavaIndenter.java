package freditor;

import static freditor.Maths.atLeastZero;

public class JavaIndenter extends Indenter {
    public static final int SPACES = 4;
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
        for (int i = home; i < end; ++i) {
            if (freditor.stateAt(i).nesting < 0) {
                difference -= SPACES;
            } else if (freditor.charAt(i) != ' ') {
                return difference;
            }
        }
        return difference;
    }

    private int openersAndClosers(Freditor freditor, int home, int end) {
        int difference = 0;
        for (int i = home; i < end; ++i) {
            difference += freditor.stateAt(i).nesting * SPACES;
        }
        return difference;
    }
}
