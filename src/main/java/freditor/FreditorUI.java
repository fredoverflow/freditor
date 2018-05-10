package freditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.swing.JComponent;

public class FreditorUI extends JComponent {
    public static final int VISIBLE_LINES_ABOVE_CURSOR = 1;
    public static final int VISIBLE_LINES_BELOW_CURSOR = 1;
    public static final int width = Front.font.width;
    public static final int height = Front.font.height;

    private final Freditor freditor;

    private JComponent componentToRepaint = this;
    private int firstVisibleLine;

    public void setComponentToRepaint(JComponent componentToRepaint) {
        this.componentToRepaint = componentToRepaint;
    }

    public int visibleLines() {
        return getHeight() / height;
    }

    public int firstVisibleLine() {
        return firstVisibleLine;
    }

    public int lastVisibleLine() {
        return firstVisibleLine + visibleLines() - 1;
    }

    public int lastLine() {
        return freditor.rows() - 1;
    }

    private void adjustView() {
        int cursorLine = freditor.row();
        if (cursorLine < firstVisibleLine) {
            firstVisibleLine = cursorLine - visibleLines() / 4;
        } else if (cursorLine - VISIBLE_LINES_ABOVE_CURSOR < firstVisibleLine) {
            firstVisibleLine = cursorLine - VISIBLE_LINES_ABOVE_CURSOR;
        } else if (cursorLine > lastVisibleLine()) {
            firstVisibleLine = cursorLine - visibleLines() * 3 / 4;
        } else if (cursorLine + VISIBLE_LINES_BELOW_CURSOR > lastVisibleLine()) {
            firstVisibleLine = cursorLine + VISIBLE_LINES_BELOW_CURSOR - visibleLines() + 1;
        }
        firstVisibleLine = Math.max(0, firstVisibleLine);
        componentToRepaint.repaint();
    }

    public FreditorUI(Flexer flexer, Indenter indenter, int columns, int rows) {
        setPreferredSize(new Dimension(columns * width, rows * height));

        freditor = new Freditor(flexer, indenter);
        // We want to be able to listen to keys...
        setFocusable(true);
        // ...including the TAB key :)
        setFocusTraversalKeysEnabled(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char c = event.getKeyChar();
                charTyped = c;
                if (c >= 32 && c < 127 || c >= 160 && c < 256) {
                    if (!event.isControlDown()) {
                        freditor.insertCharacter(c);
                    }
                }
                adjustView();
            }

            private char charTyped;

            @Override
            public void keyPressed(KeyEvent event) {
                event.consume();
                char previousCharTyped = charTyped;
                charTyped = 0;
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        freditor.onEnter(previousCharTyped);
                        break;

                    case KeyEvent.VK_TAB:
                        freditor.indent();
                        break;

                    case KeyEvent.VK_BACK_SPACE:
                        freditor.deleteLeft();
                        break;

                    case KeyEvent.VK_DELETE:
                        if (event.isShiftDown()) {
                            freditor.cut();
                        } else {
                            freditor.deleteRight();
                        }
                        break;

                    case KeyEvent.VK_INSERT:
                        if (event.isControlDown()) {
                            freditor.copy();
                        } else if (event.isShiftDown()) {
                            freditor.paste();
                        } else {
                            freditor.deleteRight();
                        }
                        break;

                    case KeyEvent.VK_LEFT:
                        if (!event.isControlDown()) {
                            freditor.moveCursorLeft();
                        } else {
                            freditor.moveCursorToPreviousLexeme();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (!event.isControlDown()) {
                            freditor.moveCursorRight();
                        } else {
                            freditor.moveCursorToNextLexeme();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_UP:
                        if (event.isAltDown()) {
                            freditor.moveSelectedLinesUp();
                            break;
                        } else if (event.isControlDown()) {
                            if (firstVisibleLine > 0) {
                                --firstVisibleLine;
                                componentToRepaint.repaint();
                                return;
                            }
                        } else {
                            freditor.moveCursorUp();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_DOWN:
                        if (event.isAltDown()) {
                            freditor.moveSelectedLinesDown();
                            break;
                        } else if (event.isControlDown()) {
                            ++firstVisibleLine;
                            componentToRepaint.repaint();
                            return;
                        } else {
                            freditor.moveCursorDown();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_PAGE_UP:
                        freditor.moveCursorUp(visibleLines());
                        firstVisibleLine -= visibleLines();
                        if (firstVisibleLine < 0) {
                            firstVisibleLine = 0;
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_PAGE_DOWN:
                        freditor.moveCursorDown(visibleLines());
                        firstVisibleLine += visibleLines();
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_HOME:
                        if (event.isControlDown()) {
                            freditor.moveCursorTop();
                        } else {
                            freditor.moveCursorStart();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_END:
                        if (event.isControlDown()) {
                            freditor.moveCursorBottom();
                        } else {
                            freditor.moveCursorEnd();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_D:
                        if (event.isControlDown() && !event.isShiftDown()) {
                            freditor.deleteCurrentLine();
                        }
                        break;

                    case KeyEvent.VK_A:
                        if (event.isControlDown()) {
                            freditor.moveCursorTop();
                            freditor.adjustOrigin();
                            freditor.moveCursorBottom();
                        }
                        break;

                    case KeyEvent.VK_C:
                        if (event.isControlDown()) {
                            freditor.copy();
                        }
                        break;

                    case KeyEvent.VK_X:
                        if (event.isControlDown()) {
                            freditor.cut();
                        }
                        break;

                    case KeyEvent.VK_V:
                        if (event.isControlDown()) {
                            freditor.paste();
                        }
                        break;

                    case KeyEvent.VK_Z:
                        if (event.isControlDown()) {
                            freditor.undo();
                        }
                        break;

                    case KeyEvent.VK_Y:
                        if (event.isControlDown()) {
                            freditor.redo();
                        }
                        break;
                }
                adjustView();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                switch (event.getClickCount()) {
                    case 1:
                        int row = event.getY() / height;
                        int column = event.getX() / width;
                        freditor.setRowAndColumn(row + firstVisibleLine, column);
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case 2:
                        freditor.selectLexemeAtCursor();
                        break;
                }
                componentToRepaint.repaint();
                requestFocusInWindow();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                int x = event.getX() / width;
                int y = event.getY() / height + firstVisibleLine;
                freditor.setRowAndColumn(y, x);
                componentToRepaint.repaint();
                requestFocusInWindow();
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                int direction = event.getWheelRotation();
                int amount = event.isControlDown() ? visibleLines() / 2 : 3;
                firstVisibleLine += direction * amount;
                if (firstVisibleLine < 0) {
                    firstVisibleLine = 0;
                }
                componentToRepaint.repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        int cursorY = (freditor.row() - firstVisibleLine) * height;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        int start = freditor.selectionStart();
        int end = freditor.selectionEnd();
        if (start != end) {
            paintSelection(g, freditor.rowOfPosition(start), freditor.columnOfPosition(start),
                    freditor.rowOfPosition(end), freditor.columnOfPosition(end));
        } else {
            g.setColor(new Color(0xffffaa));
            g.fillRect(0, cursorY, getWidth(), height);
        }
        int y = 0;
        int x = 0;
        out:
        for (int i = freditor.homePositionOfRow(firstVisibleLine); i < freditor.length(); ) {
            int k = freditor.endOfLexeme(i);
            int endState = freditor.intAt(k - 1) >> 16;
            int rgb = freditor.flexer.pickColorForLexeme(endState);
            for (; i < k; ++i) {
                char c = freditor.charAt(i);
                if (c == '\n') {
                    x = 0;
                    y += height;
                    if (y >= getHeight()) break out;
                } else {
                    Front.font.drawCharacter(g, x, y, c, rgb);
                    x += width;
                }
            }
        }
        x = freditor.column() * width;
        g.setColor(Color.BLACK);
        g.drawLine(x, cursorY, x, cursorY + height - 1);
    }

    private void paintSelection(Graphics g, int startRow, int startColumn, int endRow,
                                int endColumn) {
        g.setColor(new Color(0xc8c8ff));
        if (startRow == endRow) {
            // selection is limited to a single line
            g.fillRect(startColumn * width, (startRow - firstVisibleLine) * height,
                    (endColumn - startColumn) * width, height);
        } else {
            // selection spawns multiple lines
            g.fillRect(startColumn * width, (startRow - firstVisibleLine) * height,
                    (freditor.lengthOfRow(startRow) - startColumn) * width, height);
            for (int row = startRow + 1; row < endRow; ++row) {
                g.fillRect(0, (row - firstVisibleLine) * height, freditor.lengthOfRow(row) * width,
                        height);
            }
            g.fillRect(0, (endRow - firstVisibleLine) * height, endColumn * width, height);
        }
    }

    public int cursor() {
        return freditor.cursor();
    }

    public int lineOfPosition(int position) {
        return freditor.rowOfPosition(position);
    }

    public void setCursorTo(int position) {
        freditor.setCursorTo(position);
        adjustView();
    }

    public void setCursorTo(int row, int column) {
        freditor.setCursorTo(row, column);
        adjustView();
    }

    public void setCursorTo(String prefix) {
        freditor.setCursorTo(prefix);
        adjustView();
    }

    public String getText() {
        return freditor.toString();
    }

    public String getLineUntilCursor() {
        return freditor.getLineUntilCursor();
    }

    public void insertString(String s) {
        freditor.insertString(s);
        componentToRepaint.repaint();
    }

    public void indent() {
        freditor.indent();
        componentToRepaint.repaint();
    }

    public void loadFromFile(String pathname) throws IOException {
        freditor.loadFromFile(pathname);
        freditor.adjustOrigin();
        adjustView();
    }

    public void loadFromString(String program) {
        freditor.loadFromString(program);
        freditor.adjustOrigin();
        adjustView();
    }

    public void saveToFile(String pathname) throws IOException {
        freditor.saveToFile(pathname);
    }
}
