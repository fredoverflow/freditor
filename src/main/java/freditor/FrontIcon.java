package freditor;

import javax.swing.*;
import java.awt.*;

class FrontIcon implements Icon {
    public final Front front;
    private final String string;

    public FrontIcon(Front front) {
        this.front = front;
        this.string = front.height + "px The quick brown fox";
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        front.drawString(g, x, y, string, 0);
    }

    public int getIconWidth() {
        return front.width * string.length();
    }

    public int getIconHeight() {
        return front.height;
    }
}
