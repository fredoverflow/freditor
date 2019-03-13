package freditor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class SystemClipboard {
    private static String internalText = null;
    private static final int NUM_ATTEMPTS = 3;
    private static final int SLEEP_MILLIS = 100;

    private static final ClipboardOwner owner = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // external clipboard modification
            internalText = null;
        }
    };

    public static void set(String text) {
        internalText = text;
        for (int attempt = 1; attempt <= NUM_ATTEMPTS; ++attempt) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = new StringSelection(text);
                clipboard.setContents(contents, owner);
                return;
            } catch (IllegalStateException clipboardCurrentlyUnavailable) {
                handle(clipboardCurrentlyUnavailable, attempt);
            }
        }
    }

    private static void handle(IllegalStateException clipboardCurrentlyUnavailable, int attempt) {
        if (attempt == NUM_ATTEMPTS) {
            clipboardCurrentlyUnavailable.printStackTrace();
        } else {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static String getUnicode() {
        return internalText != null ? internalText : getExternalText();
    }

    private static String getExternalText() {
        for (int attempt = 1; attempt <= NUM_ATTEMPTS; ++attempt) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = clipboard.getContents(null);
                if (contents != null) {
                    try {
                        return (String) contents.getTransferData(DataFlavor.stringFlavor);
                    } catch (UnsupportedFlavorException | IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return "";
            } catch (IllegalStateException clipboardCurrentlyUnavailable) {
                handle(clipboardCurrentlyUnavailable, attempt);
            }
        }
        return "";
    }

    public static CharSequence getVisibleLatin1() {
        return visibleLatin1FromUnicode(getUnicode());
    }

    private static CharSequence visibleLatin1FromUnicode(String unicode) {
        final int len = unicode.length();
        for (int i = 0; i < len; ++i) {
            if (!isVisibleLatin1(unicode.charAt(i))) {
                return restrictToVisibleLatin1(unicode, i);
            }
        }
        return unicode;
    }

    private static boolean isVisibleLatin1(char x) {
        return x == '\n' || x >= 32 && x < 127 || x >= 160 && x <= 255;
    }

    private static CharSequence restrictToVisibleLatin1(String raw, int start) {
        final int len = raw.length();
        StringBuilder visibleLatin1 = new StringBuilder(len);
        visibleLatin1.append(raw, 0, start);
        for (int i = start; i < len; ++i) {
            char x = raw.charAt(i);
            if (isVisibleLatin1(x)) {
                visibleLatin1.append(x);
            } else if (x == '\t') {
                visibleLatin1.append("    ");
            }
        }
        return visibleLatin1;
    }
}
