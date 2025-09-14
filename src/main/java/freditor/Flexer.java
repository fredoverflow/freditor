package freditor;

import static freditor.FlexerState.EMPTY;
import static freditor.FlexerState.THIS;

public abstract class Flexer {
    public static final FlexerState END = EMPTY.head();
    public static final FlexerState ERROR = EMPTY.head();

    public static final FlexerState OPENING_PAREN = EMPTY.head().opening();
    public static final FlexerState CLOSING_PAREN = EMPTY.head().closing();
    public static final FlexerState OPENING_BRACKET = EMPTY.head().opening();
    public static final FlexerState CLOSING_BRACKET = EMPTY.head().closing();
    public static final FlexerState OPENING_BRACE = EMPTY.head().opening();
    public static final FlexerState CLOSING_BRACE = EMPTY.head().closing();

    public static final FlexerState NEWLINE = EMPTY.head();
    public static final FlexerState SPACE_TAIL = new FlexerState(' ', THIS);
    public static final FlexerState SPACE_HEAD = SPACE_TAIL.head();

    protected abstract FlexerState start();

    public int pickColorForLexeme(FlexerState previousState, FlexerState endState) {
        return 0x000000;
    }

    public boolean preventInsertion(FlexerState nextState) {
        return false;
    }

    public String synthesizeOnInsert(FlexerState state, FlexerState nextState) {
        return "";
    }

    public boolean arePartners(FlexerState opening, FlexerState closing) {
        return false;
    }

    public final FlexerState nextState(FlexerState currentState, char input) {
        FlexerState nextState = currentState.next(input);
        if (nextState == null) {
            nextState = start().next(input);
        }
        return nextState;
    }
}
