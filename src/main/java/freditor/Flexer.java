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

    public abstract int openingBrace();

    public abstract int closingBrace();

    public int pickColorForLexeme(int endState) {
        return 0x000000;
    }

    public int nextState(int currentState, char input) {
        int nextState = nextStateOrEnd(currentState, input);
        if (nextState == END) {
            nextState = nextStateOrEnd(END, input);
        }
        return nextState;
    }

    protected abstract int nextStateOrEnd(int currentState, char input);

    protected int operator(char expected, int nextState, char input) {
        return input == expected ? nextState : END;
    }

    protected int operator(char expected1, int nextState1, char expected2, int nextState2, char input) {
        return input == expected1 ? nextState1 : input == expected2 ? nextState2 : END;
    }

    protected int operator(char expected1, int nextState1, char expected2, int nextState2, char expected3, int nextState3, char input) {
        return input == expected1 ? nextState1 : input == expected2 ? nextState2 : input == expected3 ? nextState3 : END;
    }

    protected int keyword(char expected, int nextState, char input) {
        return input == expected ? nextState : identifier(input);
    }

    protected int keyword(char expected1, int nextState1, char expected2, int nextState2, char input) {
        return input == expected1 ? nextState1 : input == expected2 ? nextState2 : identifier(input);
    }

    protected int keyword(char expected1, int nextState1, char expected2, int nextState2, char expected3, int nextState3, char input) {
        return input == expected1 ? nextState1 : input == expected2 ? nextState2 : input == expected3 ? nextState3 : identifier(input);
    }

    protected int keyword(char expected1, int nextState1, char expected2, int nextState2, char expected3, int nextState3, char expected4, int nextState4, char input) {
        return input == expected1 ? nextState1 : input == expected2 ? nextState2 : input == expected3 ? nextState3 : input == expected4 ? nextState4 : identifier(input);
    }

    protected abstract int identifier(char input);
}
