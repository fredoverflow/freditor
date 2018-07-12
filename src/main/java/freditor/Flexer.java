package freditor;

/**
 * States are encoded by numbers (which are stored alongside the characters).
 * <p>
 * Negative numbers denote states that are entered by the first character of a lexeme;
 * This enables the syntax highlighter to detect the beginning of a new lexeme.
 * Hence, all other states MUST use positive numbers!
 * As a consequence, identifiers require at least two states:
 * a negative for the first character, and a positive for the following characters.
 * <p>
 * The number 0 denotes a special end state. If you encounter a character
 * that does not belong to the current lexeme, return 0.
 * The system will then start a new lexeme at the very same character.
 */
public abstract class Flexer {
    public static final int END = 0;
    public static final int ERROR = -1;

    public static final int NEWLINE = -2;
    public static final int FIRST_SPACE = -3;
    public static final int NEXT_SPACE = 1;

    public int pickColorForLexeme(int previousState, char firstCharacter, int endState) {
        return 0x000000;
    }

    public boolean preventInsertion(int nextState) {
        return false;
    }

    public String synthesizeOnInsert(int state, int nextState) {
        return "";
    }

    public int nextState(int currentState, char input) {
        int nextState = nextStateOrEnd(currentState, input);
        if (nextState == END) {
            nextState = nextStateOrEnd(END, input);
        }
        return nextState;
    }

    protected abstract int nextStateOrEnd(int currentState, char input);
}
