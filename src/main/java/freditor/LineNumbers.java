package freditor;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

public class LineNumbers extends JComponent {
    public static final int LINE_NUMBER_COLOR = 0x858585;

    private final FreditorUI editor;

    public LineNumbers(FreditorUI editor) {
        this.editor = editor;
        Dimension size = new Dimension(5 * FreditorUI.frontWidth, 1);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(new Dimension(5 * FreditorUI.frontWidth, 65536));
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(FreditorUI.BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        int y = 0;
        int lastLineToPaint = Math.min(editor.lastVisibleLine(), editor.lastLine());
        for (int line = editor.firstVisibleLine(); line <= lastLineToPaint; ) {
            String s = String.format("%4d", ++line);
            Fronts.front.drawString(g, 0, y, s, LINE_NUMBER_COLOR);
            y += FreditorUI.frontHeight;
        }
    }
}
