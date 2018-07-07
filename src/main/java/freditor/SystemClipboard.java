package freditor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class SystemClipboard {
    public static void set(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = new StringSelection(text);
        clipboard.setContents(contents, null);
    }

    public static String getUnicode() {
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
    }

    public static CharSequence getVisibleLatin1() {
        String unicode = getUnicode();
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
