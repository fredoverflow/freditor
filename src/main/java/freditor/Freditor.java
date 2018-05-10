package freditor;

import java.util.ArrayDeque;

public final class Freditor extends CharZipper {
    private final Indenter indenter;

    private static String clipboard = "";

    public Freditor(Flexer flexer, Indenter indenter) {
        super(flexer);
        this.indenter = indenter;
    }

    private int origin;
    private int cursor;
    private int desiredColumn;

    private class Memento extends CharZipper.Memento {
        private final int origin = Freditor.this.origin;
        private final int cursor = Freditor.this.cursor;
        private final int desiredColumn = Freditor.this.desiredColumn;

        @Override
        public void restore() {
            super.restore();
            Freditor.this.origin = origin;
            Freditor.this.cursor = cursor;
            Freditor.this.desiredColumn = desiredColumn;
        }
    }

    private void forgetDesiredColumn() {
        desiredColumn = -1;
    }

    private int rememberColumn() {
        if (desiredColumn == -1) {
            desiredColumn = column();
        }
        return desiredColumn;
    }

    public String getLineUntilCursor() {
        return subSequence(homePositionOf(cursor), cursor);
    }

    // CURSOR

    public int cursor() {
        return cursor;
    }

    public int row() {
        return rowOfPosition(cursor);
    }

    public int column() {
        return columnOfPosition(cursor);
    }

    public void adjustOrigin() {
        origin = cursor;
    }

    public boolean selectionIsEmpty() {
        return origin == cursor;
    }

    public int selectionStart() {
        return origin < cursor ? origin : cursor;
    }

    public int selectionEnd() {
        return origin > cursor ? origin : cursor;
    }

    public void setRowAndColumn(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        desiredColumn = column;
    }

    public void setCursorTo(int position) {
        cursor = position;
        forgetDesiredColumn();
        adjustOrigin();
    }

    public void setCursorTo(String prefix) {
        // TODO optimize
        int index = toString().indexOf(prefix);
        if (index != -1) {
            setCursorTo(index);
        }
    }

    public void selectLexemeAtCursor() {
        origin = startOfLexeme(cursor);
        if (cursor < length()) {
            cursor = endOfLexeme(cursor);
        }
        forgetDesiredColumn();
    }

    private boolean lexemeStartsAt(int index) {
        return index == length() || intAt(index) < 0;
    }

    public int startOfLexeme(int index) {
        while (!lexemeStartsAt(index)) {
            --index;
        }
        return index;
    }

    public int endOfLexeme(int index) {
        do {
            ++index;
        } while (!lexemeStartsAt(index));
        return index;
    }

    // TEXT MANIPULATION

    private final ArrayDeque<Memento> past = new ArrayDeque<>();
    private final ArrayDeque<Memento> future = new ArrayDeque<>();

    private int lastCursor = -1;
    private EditorAction lastAction = EditorAction.OTHER;

    private void commit() {
        past.push(new Memento());
        future.clear();
    }

    public void undo() {
        if (past.isEmpty()) return;

        future.push(new Memento());
        past.pop().restore();
        lastAction = EditorAction.OTHER;
    }

    public void redo() {
        if (future.isEmpty()) return;

        past.push(new Memento());
        future.pop().restore();
        lastAction = EditorAction.OTHER;
    }

    private boolean deleteSelection() {
        if (selectionIsEmpty()) return false;

        commit();
        deleteRange(selectionStart(), selectionEnd());
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
        return true;
    }

    public void copy() {
        if (selectionIsEmpty()) return;

        clipboard = subSequence(selectionStart(), selectionEnd());
        lastAction = EditorAction.OTHER;
    }

    public void cut() {
        if (selectionIsEmpty()) return;

        commit();
        clipboard = deleteRange(selectionStart(), selectionEnd());
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
    }

    public void paste() {
        insertString(clipboard);
    }

    public void insertCharacter(char c) {
        deleteSelection();
        if (lastAction != EditorAction.SINGLE_INSERT || cursor != lastCursor || c == ' ') {
            commit();
            lastAction = EditorAction.SINGLE_INSERT;
        }

        insertAt(cursor++, c);
        lastCursor = cursor;
        forgetDesiredColumn();
        adjustOrigin();
    }

    public void insertString(String s) {
        deleteSelection();
        commit();

        insertAt(cursor, s);
        cursor += s.length();
        forgetDesiredColumn();
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    public void onEnter(char previousCharTyped) {
        deleteSelection();
        commit();

        insertAt(cursor, indenter.synthesizeOnEnterAfter(previousCharTyped));
        insertAt(cursor++, '\n');
        adjustOrigin();
        indent();
        lastAction = EditorAction.OTHER;
    }

    public void deleteLeft() {
        if (deleteSelection()) return;

        if (cursor > 0) {
            if (lastAction != EditorAction.SINGLE_DELETE || cursor != lastCursor) {
                commit();
                lastAction = EditorAction.SINGLE_DELETE;
            }
            deleteLeftOf(cursor--);
            lastCursor = cursor;
            forgetDesiredColumn();
            adjustOrigin();
        }
    }

    public void deleteRight() {
        if (deleteSelection()) return;

        if (cursor < length()) {
            if (lastAction != EditorAction.SINGLE_DELETE || cursor != lastCursor) {
                commit();
                lastAction = EditorAction.SINGLE_DELETE;
            }
            deleteRightOf(cursor);
            lastCursor = cursor;
            forgetDesiredColumn();
            adjustOrigin();
        }
    }

    public void deleteCurrentLine() {
        commit();
        rememberColumn();
        int row = row();
        deleteRange(homePositionOfRow(row), homePositionOfRow(row + 1));
        setRowAndColumn(row, desiredColumn);
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    // NAVIGATION

    public void moveCursorLeft() {
        if (cursor > 0) {
            --cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToPreviousLexeme() {
        if (cursor > 0) {
            cursor = startOfLexeme(cursor - 1);
            forgetDesiredColumn();
        }
    }

    public void moveCursorRight() {
        if (cursor < length()) {
            ++cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToNextLexeme() {
        if (cursor < length()) {
            cursor = endOfLexeme(cursor);
            forgetDesiredColumn();
        }
    }

    public void moveCursorUp() {
        int row = row() - 1;
        if (row >= 0) {
            setRowAndColumn(row, rememberColumn());
        }
    }

    public void moveCursorUp(int rows) {
        int row = Math.max(0, row() - rows);
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorDown() {
        int row = row() + 1;
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorDown(int rows) {
        int row = row() + rows;
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorStart() {
        cursor = homePositionOf(cursor);
        forgetDesiredColumn();
    }

    public void moveCursorEnd() {
        cursor = endPositionOf(cursor);
        forgetDesiredColumn();
    }

    public void moveCursorTop() {
        cursor = 0;
        forgetDesiredColumn();
    }

    public void moveCursorBottom() {
        cursor = length();
        forgetDesiredColumn();
    }

    public void moveSelectedLinesUp() {
        int above = rowOfPosition(selectionStart()) - 1;
        if (above >= 0) {
            if (lastAction != EditorAction.LINE_MOVE) {
                commit();
                lastAction = EditorAction.LINE_MOVE;
            }
            int home = homePositionOfRow(above);
            int end = endPositionOfRow(above);
            int len = end - home + 1;
            deleteRightOf(end);
            String line = deleteRange(home, end);

            cursor -= len;
            origin -= len;

            int destination = endPositionOf(selectionEndForLineMovement());
            insertAt(destination, '\n');
            insertAt(destination + 1, line);
        }
    }

    private int selectionEndForLineMovement() {
        // When multiple lines are selected, the last line should not be moved
        // if the cursor is at the beginning of the line.
        return selectionIsEmpty() ? selectionEnd() : selectionEnd() - 1;
    }

    public void moveSelectedLinesDown() {
        int below = rowOfPosition(selectionEndForLineMovement()) + 1;
        if (below < rows()) {
            if (lastAction != EditorAction.LINE_MOVE) {
                commit();
                lastAction = EditorAction.LINE_MOVE;
            }
            int home = homePositionOfRow(below);
            int end = endPositionOfRow(below);
            int len = end - home + 1;
            String line = deleteRange(home, end);
            deleteLeftOf(home);

            int destination = homePositionOf(selectionStart());
            insertAt(destination, line);
            insertAt(destination + len - 1, '\n');

            cursor += len;
            origin += len;
        }
    }

    // INDENTATION

    public void indent() {
        final int oldRow = row();
        int[] corrections = indenter.corrections(this);
        for (int row = rows() - 1; row >= 0; --row) {
            correct(row, corrections[row]);
        }
        setRowAndColumn(oldRow, leadingSpaces(homePositionOfRow(oldRow)));
        adjustOrigin();
        forgetDesiredColumn();
    }

    private void correct(int row, int correction) {
        int start = homePositionOfRow(row);
        if (correction > 0) {
            insertSpacesAt(start, correction);
        } else if (correction < 0) {
            deleteSpacesAt(start, -correction);
        }
    }
}
