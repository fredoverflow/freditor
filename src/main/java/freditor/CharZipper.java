package freditor;

import freditor.vector.IntVector;

import java.io.*;

public class CharZipper implements CharSequence {
    public final Flexer flexer;

    public CharZipper(Flexer flexer) {
        this.flexer = flexer;
    }

    private IntVector before = IntVector.empty;
    private IntVector after = IntVector.empty;

    private IntVector lineBreaksBefore = IntVector.empty;
    private IntVector lineBreaksAfter = IntVector.empty;

    private void pushLineBreakBefore() {
        lineBreaksBefore = lineBreaksBefore.push(before.length());
    }

    private void popLineBreakBefore() {
        lineBreaksBefore = lineBreaksBefore.pop();
    }

    private void pushLineBreakAfter() {
        lineBreaksAfter = lineBreaksAfter.push(after.length());
    }

    private void popLineBreakAfter() {
        lineBreaksAfter = lineBreaksAfter.pop();
    }

    protected class Memento {
        private final IntVector before = CharZipper.this.before;
        private final IntVector after = CharZipper.this.after;

        private final IntVector lineBreaksBefore = CharZipper.this.lineBreaksBefore;
        private final IntVector lineBreaksAfter = CharZipper.this.lineBreaksAfter;

        public void restore() {
            CharZipper.this.before = before;
            CharZipper.this.after = after;

            CharZipper.this.lineBreaksBefore = lineBreaksBefore;
            CharZipper.this.lineBreaksAfter = lineBreaksAfter;
        }
    }

    // CHARSEQUENCE

    @Override
    public int length() {
        return before.length() + after.length();
    }

    @Override
    public char charAt(int index) {
        return (char) intAt(index);
    }

    public int intAt(int index) {
        if (index < before.length()) return before.intAt(index);
        index -= before.length();
        return after.intAt(after.length() - 1 - index);
    }

    public int stateAt(int index) {
        return intAt(index) >> 16;
    }

    @Override
    public String subSequence(int start, int end) {
        final int len = end - start;
        char[] temp = new char[len];
        for (int i = 0; i < len; ++i) {
            temp[i] = charAt(start + i);
        }
        return new String(temp);
    }

    @Override
    public String toString() {
        final int lenBefore = before.length();
        final int lenAfter = after.length();
        final int len = lenBefore + lenAfter;
        int[] temp = new int[len];

        if (lenBefore < lenAfter) {
            after.copyIntoArray(temp, lenBefore);
            for (int i = lenBefore, k = temp.length - 1; i < k; ++i, --k) {
                int x = temp[i];
                temp[i] = temp[k];
                temp[k] = x;
            }
        } else {
            after.copyIntoArray(temp, 0);
            for (int i = lenAfter - 1, k = lenBefore; i >= 0; --i, ++k) {
                temp[k] = temp[i];
            }
        }
        before.copyIntoArray(temp, 0);
        // remove lexer states
        for (int i = 0; i < len; ++i) {
            temp[i] &= 0xffff;
        }
        return new String(temp, 0, len);
    }

    // LINES

    private int numberOfLineBreaks() {
        return lineBreaksBefore.length() + lineBreaksAfter.length();
    }

    public int rows() {
        return numberOfLineBreaks() + 1;
    }

    public int lengthOfRow(int row) {
        return endPositionOfRow(row) - homePositionOfRow(row);
    }

    public int homePositionOfRow(int row) {
        if (row == 0) return 0;
        --row;
        if (row < lineBreaksBefore.length()) return lineBreaksBefore.intAt(row) + 1;
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.intAt(n - 1 - row) + 1;
        return length();
    }

    public int endPositionOfRow(int row) {
        if (row < lineBreaksBefore.length()) return lineBreaksBefore.intAt(row);
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.intAt(n - 1 - row);
        return length();
    }

    public int rowOfPosition(int position) {
        if (position < before.length()) {
            return binarySearch(lineBreaksBefore, position);
        } else {
            return numberOfLineBreaks() - binarySearch(lineBreaksAfter, length() - position);
        }
    }

    public int homePositionOf(int position) {
        return homePositionOfRow(rowOfPosition(position));
    }

    public int endPositionOf(int position) {
        return endPositionOfRow(rowOfPosition(position));
    }

    public int columnOfPosition(int position) {
        return position - homePositionOf(position);
    }

    // TEXT MANIPULATION

    public void clear() {
        before = IntVector.empty;
        after = IntVector.empty;

        lineBreaksBefore = IntVector.empty;
        lineBreaksAfter = IntVector.empty;
    }

    private void focusOn(int index) {
        while (index < before.length()) {
            int x = before.top();
            before = before.pop();

            if ((char) x == '\n') {
                popLineBreakBefore();
                pushLineBreakAfter();
            }
            after = after.push(x);
        }
        while (index > before.length()) {
            int x = after.top();
            after = after.pop();

            if ((char) x == '\n') {
                popLineBreakAfter();
                pushLineBreakBefore();
            }
            before = before.push(x);
        }
    }

    public void insertAt(int index, char x) {
        focusOn(index);
        insertAtFocus(x);
        fixAfterStates();
    }

    public void insertWithSynthAt(int index, char x) {
        focusOn(index);
        final int nextState = flexer.nextState(stateAtFocus(), x);
        if (nextState == stateAfterFocus() && flexer.preventInsertion(nextState)) return;

        insertAtFocus(x);
        String synth = flexer.synthesizeOnInsert(stateAtFocus(), stateAfterFocus());
        insertAtFocus(synth);
        fixAfterStates();
    }

    private void insertAtFocus(char x) {
        if (x == '\n') {
            pushLineBreakBefore();
        }
        int state = stateAtFocus();
        state = flexer.nextState(state, x);
        pushWithState(x, state);
    }

    private int stateAtFocus() {
        return before.isEmpty() ? Flexer.END : before.top() >> 16;
    }

    private int stateAfterFocus() {
        return after.isEmpty() ? Flexer.END : after.top() >> 16;
    }

    private void pushWithState(char x, int state) {
        before = before.push(state << 16 | x);
    }

    private void fixAfterStates() {
        int state = stateAtFocus();
        while (!after.isEmpty()) {
            int x = after.top();
            char c = (char) x;
            int cachedState = x >> 16;
            state = flexer.nextState(state, c);
            if (state == cachedState) break;

            after = after.pop();
            if (c == '\n') {
                popLineBreakAfter();
                pushLineBreakBefore();
            }
            pushWithState(c, state);
        }
    }

    public void insertAt(int index, String s) {
        focusOn(index);
        insertAtFocus(s);
        fixAfterStates();
    }

    private void insertAtFocus(String s) {
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            insertAtFocus(s.charAt(i));
        }
    }

    public int leadingSpaces(int index) {
        int start = index;
        final int len = length();
        if (index < len && stateAt(index) == Flexer.FIRST_SPACE) {
            ++index;
            while (index < len && stateAt(index) == Flexer.NEXT_SPACE) {
                ++index;
            }
        }
        return index - start;
    }

    public void insertSpacesAt(int index, int len) {
        focusOn(index);
        for (; len > 0; --len) {
            insertAtFocus(' ');
        }
        fixAfterStates();
    }

    public void deleteSpacesAt(int index, int len) {
        focusOn(index + len);
        before = before.take(index);
        fixAfterStates();
    }

    public void deleteLeftOf(int index) {
        focusOn(index);
        if ((char) before.top() == '\n') {
            popLineBreakBefore();
        }
        before = before.pop();
        fixAfterStates();
    }

    public void deleteRightOf(int index) {
        focusOn(index);
        if ((char) after.top() == '\n') {
            popLineBreakAfter();
        }
        after = after.pop();
        fixAfterStates();
    }

    public String deleteRange(int start, int end) {
        focusOn(end);
        String result = beforeSlice(start, end);

        int firstObsoleteLineBreak = binarySearch(lineBreaksBefore, start);
        lineBreaksBefore = lineBreaksBefore.take(firstObsoleteLineBreak);

        before = before.take(start);
        fixAfterStates();
        return result;
    }

    private String beforeSlice(int start, int end) {
        final int len = end - start;
        char[] temp = new char[len];
        for (int i = 0; i < len; ++i) {
            temp[i] = (char) before.intAt(start + i);
        }
        return new String(temp);
    }

    // PERSISTENCE

    public void loadFromFile(String pathname) throws IOException {
        loadFromReader(new FileReader(pathname));
    }

    public void loadFromString(String program) {
        try {
            loadFromReader(new StringReader(program));
        } catch (IOException impossible) {
            impossible.printStackTrace();
        }
    }

    private void loadFromReader(Reader reader) throws IOException {
        try (BufferedReader in = new BufferedReader(reader)) {
            String line = in.readLine();
            if (line != null) {
                clear();
                insertAtFocus(line);
                while ((line = in.readLine()) != null) {
                    insertAtFocus('\n');
                    insertAtFocus(line);
                }
            }
        }
    }

    public void saveToFile(String pathname) throws IOException {
        String text = toString();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(pathname))) {
            for (int i = 0; i < rows(); ++i) {
                int start = homePositionOfRow(i);
                int end = endPositionOfRow(i);
                out.write(text, start, end - start);
                out.newLine();
            }
        }
    }

    // BINARY SEARCH

    private static int binarySearch(IntVector lineBreaks, int key) {
        int left = 0; // inclusive
        int right = lineBreaks.length(); // exclusive
        while (left < right) {
            int middle = (left + right) >>> 1;
            int element = lineBreaks.intAt(middle);
            if (element < key) {
                left = middle + 1; // inclusive
            } else if (element > key) {
                right = middle; // exclusive
            } else {
                return middle;
            }
        }
        return left;
    }
}
