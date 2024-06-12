package freditor;

import freditor.ephemeral.GapBuffer;
import freditor.ephemeral.IntStack;
import freditor.persistent.ByteVector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static freditor.Maths.atLeastZero;

public final class Freditor extends CharZipper {
    private final IntStack lineBreaksBefore;
    private final IntStack lineBreaksAfter;

    private final GapBuffer<FlexerState> flexerStates;

    public final Flexer flexer;
    public final Indenter indenter;
    public final Path file;

    public Freditor(Flexer flexer, Indenter indenter, Path file) {
        lineBreaksBefore = new IntStack();
        lineBreaksAfter = new IntStack();

        flexerStates = new GapBuffer<>();

        this.flexer = flexer;
        this.indenter = indenter;
        this.file = file;
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

            refreshBookkeeping();
        }
    }

    private void refreshBookkeeping() {
        refreshLineBreaks();
        refreshFlexerStates();
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

    public boolean lineIsBlankBefore(int index) {
        for (int i = index - 1; i >= 0; --i) {
            switch (charAt(i)) {
                case '\n':
                    return true;
                case ' ':
                    continue;
                default:
                    return false;
            }
        }
        return true;
    }

    public String getLineBeforeSelection() {
        int selectionStart = selectionStart();
        return subSequence(homePositionOf(selectionStart), selectionStart);
    }

    public String getTextBeforeSelection() {
        int selectionStart = selectionStart();
        if (before().size() < selectionStart) {
            focusOn(selectionStart);
        }
        return before().take(selectionStart).toString();
    }

    // LINE BREAKS

    private void refreshLineBreaks() {
        refreshLineBreaks(before(), lineBreaksBefore);
        refreshLineBreaks(after(), lineBreaksAfter);
    }

    private static void refreshLineBreaks(ByteVector text, IntStack lineBreaks) {
        lineBreaks.clear();
        final int len = text.size();
        for (int i = 0; i < len; ++i) {
            if (text.byteAt(i) == '\n') {
                lineBreaks.push(i);
            }
        }
    }

    private int numberOfLineBreaks() {
        return lineBreaksBefore.size() + lineBreaksAfter.size();
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
        if (row < lineBreaksBefore.size()) return lineBreaksBefore.get(row) + 1;
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.get(n - 1 - row) + 1;
        return length();
    }

    public int endPositionOfRow(int row) {
        if (row < lineBreaksBefore.size()) return lineBreaksBefore.get(row);
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.get(n - 1 - row);
        return length();
    }

    public int rowOfPosition(int position) {
        if (position < before().size()) {
            return lineBreaksBefore.binarySearch(position);
        } else {
            return numberOfLineBreaks() - lineBreaksAfter.binarySearch(length() - position);
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

    // FLEXER

    public FlexerState stateAt(int index) {
        return (0 <= index) && (index < length()) ? flexerStates.get(index) : Flexer.END;
    }

    private void refreshFlexerStates() {
        flexerStates.clear();
        FlexerState state = flexer.start();
        final int len = length();
        for (int i = 0; i < len; ++i) {
            char x = charAt(i);
            state = flexer.nextState(state, x);
            flexerStates.add(state);
        }
    }

    private void fixFlexerStatesFrom(int index) {
        FlexerState state = stateAt(index - 1);
        final int len = length();
        for (int i = index; i < len; ++i) {
            char x = charAt(i);
            state = flexer.nextState(state, x);
            if (flexerStates.set(i, state) == state) return;
        }
    }

    public int startOfLexeme(int index) {
        while (!stateAt(index).isHead()) {
            --index;
        }
        return index;
    }

    public int endOfLexeme(int index) {
        do {
            ++index;
        } while (!stateAt(index).isHead());
        return Math.min(length(), index);
    }

    public void findOpeningParen(int start, IntConsumer onPresent, Runnable onMissing) {
        int nesting = 0;
        for (int i = cursor - 1; i >= start; --i) {
            nesting += Flexer.nestingDelta.getOrDefault(stateAt(i), 0);
            if (nesting > 0) {
                onPresent.accept(i);
                return;
            }
        }
        onMissing.run();
    }

    public static final Runnable doNothing = () -> {
    };

    public void findClosingParen(int end, IntConsumer onPresent, Runnable onMissing) {
        int nesting = 0;
        for (int i = cursor; i < end; ++i) {
            nesting += Flexer.nestingDelta.getOrDefault(stateAt(i), 0);
            if (nesting < 0) {
                onPresent.accept(i);
                return;
            }
        }
        onMissing.run();
    }

    // CHARZIPPER OVERRIDES

    @Override
    public void clear() {
        lineBreaksBefore.clear();
        lineBreaksAfter.clear();

        flexerStates.clear();

        super.clear();
    }

    @Override
    protected void focusOn(int index) {
        super.focusOn(index);
        final int len = length();
        mirrorLineBreaks(lineBreaksBefore, lineBreaksAfter, index, len - 1);
        mirrorLineBreaks(lineBreaksAfter, lineBreaksBefore, len - index, len - 1);
    }

    private void mirrorLineBreaks(IntStack src, IntStack dst, int threshold, int mirror) {
        while (!src.isEmpty() && src.top() >= threshold) {
            dst.push(mirror - src.pop());
        }
    }

    @Override
    public void insertAt(int index, char x) {
        super.insertAt(index, x);
        if (x == '\n') {
            lineBreaksBefore.push(index);
        }
        flexerStates.add(index, FlexerState.EMPTY);
        fixFlexerStatesFrom(index);
    }

    @Override
    public void insertAt(int index, CharSequence s) {
        super.insertAt(index, s);
        final ByteVector before = before();
        final int end = before.size();
        for (int i = index; i < end; ++i) {
            if (before.byteAt(i) == '\n') {
                lineBreaksBefore.push(i);
            }
            flexerStates.add(i, FlexerState.EMPTY);
        }
        fixFlexerStatesFrom(index);
    }

    private void insertAt(int index, char x, CharSequence s) {
        super.insertAt(index, x);
        if (x == '\n') {
            lineBreaksBefore.push(index);
        }
        flexerStates.add(index, FlexerState.EMPTY);

        final int start = after().size();
        insertAfterFocus(s);
        final ByteVector after = after();
        final int end = after.size();
        for (int i = start; i < end; ++i) {
            if (after.byteAt(i) == '\n') {
                lineBreaksAfter.push(i);
            }
            flexerStates.add(index + 1, FlexerState.EMPTY);
        }
        fixFlexerStatesFrom(index);
    }

    @Override
    public byte deleteLeftOf(int index) {
        byte deleted = super.deleteLeftOf(index);
        if (deleted == '\n') {
            lineBreaksBefore.pop();
        }
        flexerStates.remove(index - 1);
        fixFlexerStatesFrom(index - 1);
        return deleted;
    }

    @Override
    public byte deleteRightOf(int index) {
        byte deleted = super.deleteRightOf(index);
        if (deleted == '\n') {
            lineBreaksAfter.pop();
        }
        flexerStates.remove(index);
        fixFlexerStatesFrom(index);
        return deleted;
    }

    @Override
    public String deleteRange(int start, int end) {
        String result = super.deleteRange(start, end);
        int firstObsoleteLineBreak = lineBreaksBefore.binarySearch(start);
        lineBreaksBefore.shrinkToSize(firstObsoleteLineBreak);
        flexerStates.remove(start, end);
        fixFlexerStatesFrom(start);
        return result;
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
        return Math.min(origin, cursor);
    }

    public int selectionEnd() {
        return Math.max(origin, cursor);
    }

    public void setRowAndColumn(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        desiredColumn = column;
    }

    public void clickRowAndColumn(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        forgetDesiredColumn();
    }

    public void setCursorTo(int position) {
        cursor = position;
        forgetDesiredColumn();
        adjustOrigin();
    }

    public void setCursorTo(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        desiredColumn = column;
        adjustOrigin();
    }

    public boolean setCursorTo(Pattern pattern, int group) {
        Matcher matcher = pattern.matcher(toString());
        boolean found = matcher.find();
        if (found) {
            setCursorTo(matcher.start(group));
        }
        return found;
    }

    public void selectLexemeAtCursor() {
        origin = startOfLexeme(cursor);
        cursor = endOfLexeme(cursor);
        forgetDesiredColumn();
    }

    public String lexemeAtCursor() {
        return lexemeAt(cursor);
    }

    private String lexemeAt(int index) {
        int start = startOfLexeme(index);
        int end = endOfLexeme(index);
        return subSequence(start, end);
    }

    public String symbolNearCursor(FlexerState symbolTail) {
        // coerce keyword/literal prefixes to symbol
        if (stateAt(cursor).next('_') == symbolTail) {
            return lexemeAt(cursor);
        } else if (cursor >= 1 && stateAt(cursor - 1).next('_') == symbolTail) {
            return lexemeAt(cursor - 1);
        } else {
            return "";
        }
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

    public void uncommit() {
        past.pop();
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

        SystemClipboard.set(subSequence(selectionStart(), selectionEnd()));
        lastAction = EditorAction.OTHER;
    }

    public void cut() {
        if (selectionIsEmpty()) return;

        commit();
        SystemClipboard.set(deleteRange(selectionStart(), selectionEnd()));
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
    }

    public void paste() {
        insert(SystemClipboard.getVisibleLatin1());
    }

    private static char partner(char c) {
        switch (c) {
            case '(':
                return ')';
            case ')':
                return '(';

            case '[':
                return ']';
            case ']':
                return '[';

            case '{':
                return '}';
            case '}':
                return '{';

            default:
                return 0;
        }
    }

    public void insertCharacter(char c) {
        char partner;
        if (!selectionIsEmpty() && (partner = partner(c)) != 0) {
            commit();
            lastAction = EditorAction.OTHER;

            if (c < partner) {
                insertAt(selectionEnd(), partner);
                cursor = selectionStart();
            } else {
                insertAt(selectionStart(), partner);
                cursor = selectionEnd() + 1;
            }
            insertAt(cursor++, c);
        } else {
            deleteSelection();
            if (lastAction != EditorAction.SINGLE_INSERT || cursor != lastCursor || c == ' ') {
                commit();
                lastAction = EditorAction.SINGLE_INSERT;
            }

            insertWithSynthAt(cursor++, c);
            lastCursor = cursor;
        }
        forgetDesiredColumn();
        adjustOrigin();
    }

    private void insertWithSynthAt(int index, char x) {
        final FlexerState oldState = stateAt(index);
        final FlexerState newState = flexer.nextState(stateAt(index - 1), x);
        if (newState == oldState && flexer.preventInsertion(newState)) return;

        String synth = flexer.synthesizeOnInsert(newState, oldState);
        if (synth.isEmpty()) {
            insertAt(index, x);
        } else {
            insertAt(index, x, synth);
        }
    }

    public void insert(CharSequence s) {
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

        String synth = indenter.synthesizeOnEnterAfter(previousCharTyped);
        if (synth.isEmpty()) {
            insertAt(cursor++, '\n');
        } else {
            insertAt(cursor++, '\n', synth);
        }
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
            FlexerState before = stateAt(cursor - 1);
            FlexerState after = stateAt(cursor);
            deleteLeftOf(cursor--);
            if (flexer.arePartners(before, after)) {
                deleteRightOf(cursor);
            }
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

    public void slurpForward() {
        findClosingParen(length(), closing -> {
            commit();
            int backup = cursor;
            cursor = closing;
            moveCursorToNextForm();
            if (cursor < length()) {
                moveCursorAfterCurrentForm();
                String form = deleteRange(origin, cursor);
                insertAt(closing, form);
                cursor = backup;
                if (cursor == closing) {
                    cursor += form.length();
                }
                adjustOrigin();
                forgetDesiredColumn();
                lastAction = EditorAction.OTHER;
            } else {
                past.pop().restore();
            }
        }, doNothing);
    }

    private void moveCursorToNextForm() {
        final int len = length();
        while (cursor < len) {
            FlexerState state = stateAt(cursor);
            if (Flexer.nestingDelta.getOrDefault(state, 0) == -1) {
                origin = cursor + 1;
            } else if (state != Flexer.NEWLINE && state != Flexer.SPACE_HEAD) {
                return;
            }
            cursor = endOfLexeme(cursor);
        }
    }

    private void moveCursorAfterCurrentForm() {
        final int len = length();
        int nesting = 0;
        do {
            nesting += Flexer.nestingDelta.getOrDefault(stateAt(cursor), 0);
            cursor = endOfLexeme(cursor);
        } while (nesting > 0 && cursor < len);
    }

    public void replace(String regex, String replacement) {
        String text = toString();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String newText = matcher.replaceAll(replacement);
        replace(newText);
    }

    public void replace(String newText) {
        commit();
        int row = row();
        int column = column();
        load(newText);
        setRowAndColumn(row, column);
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    public void rename(String oldName, String newName, int... positions) {
        if (oldName.equals(newName)) return;

        Arrays.sort(positions);

        commit();
        int row = row();
        int column = column();

        final int oldLength = oldName.length();
        final int lengthDelta = newName.length() - oldLength;
        int offset = 0;
        for (int position : positions) {
            position += offset;
            deleteRange(position, position + oldLength);
            insertAt(position, newName);
            offset += lengthDelta;
        }

        setRowAndColumn(row, column);
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
        while (cursor > 0) {
            cursor = startOfLexeme(cursor - 1);
            FlexerState state = stateAt(cursor);
            if (state == Flexer.NEWLINE || state == Flexer.SPACE_HEAD) continue;

            forgetDesiredColumn();
            break;
        }
    }

    public void moveCursorBeforePreviousOpeningParen(boolean isShiftDown) {
        if (isShiftDown) {
            findClosingParen(length(), closing -> origin = closing + 1, () -> origin = length());
        }
        findOpeningParen(0, opening -> cursor = opening, () -> cursor = 0);
        forgetDesiredColumn();
    }

    public void moveCursorRight() {
        if (cursor < length()) {
            ++cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToNextLexeme() {
        while (cursor < length()) {
            cursor = endOfLexeme(cursor);
            FlexerState state = stateAt(cursor);
            if (state == Flexer.NEWLINE || state == Flexer.SPACE_HEAD) continue;

            forgetDesiredColumn();
            break;
        }
    }

    public void moveCursorAfterNextClosingParen(boolean isShiftDown) {
        if (isShiftDown) {
            findOpeningParen(0, opening -> origin = opening, () -> origin = 0);
        }
        findClosingParen(length(), closing -> cursor = closing + 1, () -> cursor = length());
        forgetDesiredColumn();
    }

    public void moveCursorUp() {
        int row = row() - 1;
        if (row >= 0) {
            setRowAndColumn(row, rememberColumn());
        }
    }

    public void moveCursorUp(int rows) {
        int row = atLeastZero(row() - rows);
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

    // FORMATTING

    public void isolateBraces() {
        for (int i = 0; i < length(); ++i) {
            FlexerState state = flexerStates.get(i);
            if (state == Flexer.OPENING_BRACE) {
                i = ensureSpaceBeforeBrace(i);
                i = ensureNewlineAfterBrace(i);
            } else if (state == Flexer.CLOSING_BRACE) {
                i = ensureNewlineBeforeBrace(i);
                i = ensureNewlineAfterBrace(i);
            }
        }
    }

    private int ensureSpaceBeforeBrace(int i) {
        while (stateAt(i - 1) == Flexer.SPACE_TAIL) {
            deleteLeftOf(i--);
        }
        FlexerState state = stateAt(i - 1);
        if (state != Flexer.SPACE_HEAD && state != Flexer.NEWLINE) {
            insertAt(i++, ' ');
        }
        return i;
    }

    private int ensureNewlineAfterBrace(int i) {
        ++i;
        while (stateAt(i) == Flexer.SPACE_HEAD) {
            deleteRightOf(i);
        }
        if (stateAt(i) != Flexer.NEWLINE) {
            insertAt(i, '\n');
        }
        return i;
    }

    private int ensureNewlineBeforeBrace(int i) {
        int j = skipSpacesBefore(i);
        if (stateAt(j) != Flexer.NEWLINE) {
            insertAt(j + 1, '\n');
            ++i;
        }
        return i;
    }

    private int skipSpacesBefore(int j) {
        do --j; while (stateAt(j) == Flexer.SPACE_TAIL);
        if (stateAt(j) == Flexer.SPACE_HEAD) --j;
        return j;
    }

    public void indent() {
        final int oldRow = row();
        int[] corrections = indenter.corrections(this);
        for (int row = corrections.length - 1; row >= 0; --row) {
            correct(row, corrections[row]);
        }
        setRowAndColumn(oldRow, leadingSpaces(homePositionOfRow(oldRow)));
        adjustOrigin();
        forgetDesiredColumn();
    }

    private void correct(int row, int correction) {
        int start = homePositionOfRow(row);
        if (correction > 0) {
            insertAt(start, SpaceSequence.of(correction));
        } else if (correction < 0) {
            deleteRange(start, start - correction);
        }
    }

    public int leadingSpaces(int index) {
        int start = index;
        final int len = length();
        while (index < len && charAt(index) == ' ') {
            ++index;
        }
        return index - start;
    }

    // PERSISTENCE

    public void load() throws IOException {
        super.loadStrict(Files.readAllBytes(file));
        bytesLoaded();
    }

    public void load(String program) {
        super.loadLenient(program.getBytes(StandardCharsets.ISO_8859_1));
        bytesLoaded();
    }

    private void bytesLoaded() {
        refreshBookkeeping();
        if (cursor >= length()) {
            cursor = length();
        }
        adjustOrigin();
        forgetDesiredColumn();
    }

    public void save() {
        save(file, toByteArray());
    }

    private void save(Path file, byte[] bytes) {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, bytes);
        } catch (IOException savingFailed) {
            // If saving to user.home fails, there is no sensible way to recover
            savingFailed.printStackTrace();
        }
    }

    public void saveWithBackup() {
        byte[] bytes = toByteArray();
        save(file, bytes);
        save(backupFile(bytes), bytes);
    }

    private Path backupFile(byte[] bytes) {
        String filename;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            // File names starting with a minus sign require special care.
            // A base64url-encoded SHA-256 hash never ends with a minus sign,
            // so reversing the encoded string is an easy fix.
            filename = new StringBuilder(encoded).reverse().append(".txt").toString();
        } catch (NoSuchAlgorithmException sha256unsupported) {
            // Every implementation of the Java platform is REQUIRED to support SHA-1 and SHA-256
            sha256unsupported.printStackTrace();
            filename = String.format("%08x.txt", Arrays.hashCode(bytes));
        }
        return file.getParent().resolve("backup").resolve(filename);
    }
}
