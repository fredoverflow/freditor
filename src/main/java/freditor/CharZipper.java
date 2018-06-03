package freditor;

import freditor.persistent.CharVector;

public class CharZipper implements CharSequence {
    private CharVector before = CharVector.empty;
    private CharVector after = CharVector.empty;

    protected class Memento {
        private final CharVector before = CharZipper.this.before;
        private final CharVector after = CharZipper.this.after;

        public void restore() {
            CharZipper.this.before = before;
            CharZipper.this.after = after;
        }
    }

    protected CharVector before() {
        return before;
    }

    protected CharVector after() {
        return after;
    }

    // CHARSEQUENCE

    @Override
    public int length() {
        return before.length() + after.length();
    }

    @Override
    public char charAt(int index) {
        if (index < before.length()) return before.charAt(index);
        index -= before.length();
        return after.charAt(after.length() - 1 - index);
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
        char[] temp = new char[len];

        if (lenBefore < lenAfter) {
            after.copyIntoArray(temp, lenBefore);
            for (int i = lenBefore, k = temp.length - 1; i < k; ++i, --k) {
                char x = temp[i];
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
        return new String(temp, 0, len);
    }

    // TEXT MANIPULATION

    public void clear() {
        before = CharVector.empty;
        after = CharVector.empty;
    }

    protected void focusOn(int index) {
        int delta = index - before.length();
        for (; delta < 0; ++delta) {
            char x = before.top();
            before = before.pop();
            after = after.push(x);
        }
        for (; delta > 0; --delta) {
            char x = after.top();
            after = after.pop();
            before = before.push(x);
        }
    }

    public void insertAt(int index, char x) {
        focusOn(index);
        before = before.push(x);
    }

    public void insertAt(int index, CharSequence s) {
        focusOn(index);
        insertBeforeFocus(s);
    }

    protected void insertBeforeFocus(CharSequence s) {
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            before = before.push(s.charAt(i));
        }
    }

    protected void insertAfterFocus(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            after = after.push(s.charAt(i));
        }
    }

    public char deleteLeftOf(int index) {
        focusOn(index);
        char deleted = before.top();
        before = before.pop();
        return deleted;
    }

    public char deleteRightOf(int index) {
        focusOn(index);
        char deleted = after.top();
        after = after.pop();
        return deleted;
    }

    public String deleteRange(int start, int end) {
        focusOn(end);
        String result = beforeSlice(start, end);
        before = before.take(start);
        return result;
    }

    private String beforeSlice(int start, int end) {
        final int len = end - start;
        char[] temp = new char[len];
        for (int i = 0; i < len; ++i) {
            temp[i] = before.charAt(start + i);
        }
        return new String(temp);
    }
}
