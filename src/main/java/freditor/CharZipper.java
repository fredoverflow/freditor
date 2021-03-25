package freditor;

import freditor.persistent.ByteVector;

import java.nio.charset.StandardCharsets;

public class CharZipper implements CharSequence {
    private ByteVector before = ByteVector.EMPTY;
    private ByteVector after = ByteVector.EMPTY;

    protected class Memento {
        private final ByteVector before = CharZipper.this.before;
        private final ByteVector after = CharZipper.this.after;

        public void restore() {
            CharZipper.this.before = before;
            CharZipper.this.after = after;
        }
    }

    protected ByteVector before() {
        return before;
    }

    protected ByteVector after() {
        return after;
    }

    // CHARSEQUENCE

    @Override
    public int length() {
        return before.size() + after.size();
    }

    @Override
    public char charAt(int index) {
        if (index < before.size()) return charAt(before, index);
        index -= before.size();
        return charAt(after, after.size() - 1 - index);
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

    public byte[] toByteArray() {
        final int lenBefore = before.size();
        final int lenAfter = after.size();
        final int len = lenBefore + lenAfter;
        byte[] temp = new byte[len];

        if (lenBefore < lenAfter) {
            after.copyIntoArray(temp, lenBefore);
            for (int i = lenBefore, k = temp.length - 1; i < k; ++i, --k) {
                byte x = temp[i];
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
        return temp;
    }

    @Override
    public String toString() {
        return new String(toByteArray(), StandardCharsets.ISO_8859_1);
    }

    public boolean isBlank() {
        for (byte b : toByteArray()) {
            switch (b & 255) {
                case '\n':
                case ' ':
                    continue;
                default:
                    return false;
            }
        }
        return true;
    }

    // TEXT MANIPULATION

    public void clear() {
        before = ByteVector.EMPTY;
        after = ByteVector.EMPTY;
    }

    protected void focusOn(int index) {
        int delta = index - before.size();
        for (; delta < 0; ++delta) {
            byte x = before.top();
            before = before.pop();
            after = after.push(x);
        }
        for (; delta > 0; --delta) {
            byte x = after.top();
            after = after.pop();
            before = before.push(x);
        }
    }

    public void insertAt(int index, char x) {
        focusOn(index);
        before = before.push((byte) x);
    }

    public void insertAt(int index, CharSequence s) {
        focusOn(index);
        insertBeforeFocus(s);
    }

    protected void insertBeforeFocus(byte[] bytes) {
        final int len = bytes.length;
        for (int i = 0; i < len; ) {
            byte b = bytes[i++];
            if (b != '\r') {
                before = before.push(b);
            } else if (i == len || bytes[i] != '\n') {
                // convert Mac OS Classic "\r" to Unix "\n"
                before = before.push((byte) '\n');
            }
        }
    }

    protected void insertBeforeFocus(CharSequence s) {
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            before = before.push((byte) s.charAt(i));
        }
    }

    protected void insertAfterFocus(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            after = after.push((byte) s.charAt(i));
        }
    }

    public byte deleteLeftOf(int index) {
        focusOn(index);
        byte deleted = before.top();
        before = before.pop();
        return deleted;
    }

    public byte deleteRightOf(int index) {
        focusOn(index);
        byte deleted = after.top();
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
            temp[i] = charAt(before, start + i);
        }
        return new String(temp);
    }

    private static char charAt(ByteVector v, int index) {
        return (char) (v.byteAt(index) & 255);
    }
}
