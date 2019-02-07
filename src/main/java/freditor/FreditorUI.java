package freditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class FreditorUI extends JComponent {
    public static final Color CURRENT_LINE_COLOR = new Color(0xffffaa);
    public static final Color SELECTION_COLOR = new Color(0xc8c8ff);
    public static final Color MATCHING_PARENS_BACKGROUND_COLOR = new Color(0xe0e0e0);

    public static final int ADDITIONAL_LINES = 1;
    public static final int ADDITIONAL_COLUMNS = 8;

    public static final int fontWidth = Front.font.width;
    public static final int fontHeight = Front.font.height;

    private final Freditor freditor;

    private JComponent componentToRepaint = this;
    private int firstVisibleLine;
    private int firstVisibleColumn;

    public void setComponentToRepaint(JComponent componentToRepaint) {
        this.componentToRepaint = componentToRepaint;
    }

    public Consumer<String> onRightClick = ignored -> {
    };

    public int visibleLines() {
        return getHeight() / fontHeight;
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

    private int visibleColumns() {
        return getWidth() / fontWidth;
    }

    public int lastVisibleColumn() {
        return firstVisibleColumn + visibleColumns() - 1;
    }

    private void adjustView() {
        int cursorLine = freditor.row();
        if (cursorLine < firstVisibleLine) {
            firstVisibleLine = cursorLine - visibleLines() / 4;
        } else if (cursorLine - ADDITIONAL_LINES < firstVisibleLine) {
            firstVisibleLine = cursorLine - ADDITIONAL_LINES;
        } else if (cursorLine > lastVisibleLine()) {
            firstVisibleLine = cursorLine - visibleLines() * 3 / 4;
        } else if (cursorLine + ADDITIONAL_LINES > lastVisibleLine()) {
            firstVisibleLine = cursorLine + ADDITIONAL_LINES - visibleLines() + 1;
        }
        firstVisibleLine = Math.max(0, firstVisibleLine);

        final int additionalColumns = Math.min(visibleColumns() / 2, ADDITIONAL_COLUMNS);
        int cursorColumn = freditor.column();
        if (cursorColumn - additionalColumns < firstVisibleColumn) {
            firstVisibleColumn = cursorColumn - additionalColumns;
        } else {
            int column = Math.min(freditor.lengthOfRow(freditor.row()), cursorColumn + additionalColumns);
            if (column > lastVisibleColumn()) {
                firstVisibleColumn = column - visibleColumns();
            }
        }
        firstVisibleColumn = Math.max(0, firstVisibleColumn);

        componentToRepaint.repaint();
    }

    public FreditorUI(Flexer flexer, Indenter indenter, int columns, int rows) {
        setPreferredSize(new Dimension(columns * fontWidth, rows * fontHeight));

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
                        if (event.isAltDown()) {
                            freditor.moveCursorBeforePreviousOpeningParen(event.isShiftDown());
                        } else if (event.isControlDown()) {
                            freditor.moveCursorToPreviousLexeme();
                        } else {
                            freditor.moveCursorLeft();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (event.isAltDown()) {
                            freditor.moveCursorAfterNextClosingParen(event.isShiftDown());
                        } else if (event.isControlDown()) {
                            freditor.moveCursorToNextLexeme();
                        } else {
                            freditor.moveCursorRight();
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
                        int row = event.getY() / fontHeight + firstVisibleLine;
                        int column = event.getX() / fontWidth + firstVisibleColumn;
                        freditor.setRowAndColumn(row, column);
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        if (event.getButton() != MouseEvent.BUTTON1) {
                            onRightClick.accept(lexemeAtCursor());
                        }
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
                int row = event.getY() / fontHeight + firstVisibleLine;
                int column = event.getX() / fontWidth + firstVisibleColumn;
                freditor.setRowAndColumn(Math.max(0, row), Math.max(0, column));
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

    public String lexemeAtCursor() {
        return freditor.lexemeAtCursor();
    }

    public String symbolNearCursor(int symbolFirst) {
        return freditor.symbolNearCursor(symbolFirst);
    }

    @Override
    public void paint(Graphics g) {
        paintBackground(g);
        paintCurrentLineOrSelection(g);
        paintMatchingParensBackground(g);
        paintLexemes(g);
        paintCursor(g);
    }

    private int x(int column) {
        return (column - firstVisibleColumn) * fontWidth;
    }

    private int y(int row) {
        return (row - firstVisibleLine) * fontHeight;
    }

    private void paintBackground(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintCurrentLineOrSelection(Graphics g) {
        int start = freditor.selectionStart();
        int end = freditor.selectionEnd();
        if (start == end) {
            paintCurrentLine(g);
        } else {
            paintSelection(g, freditor.rowOfPosition(start), freditor.columnOfPosition(start),
                    freditor.rowOfPosition(end), freditor.columnOfPosition(end));
        }
    }

    private void paintCurrentLine(Graphics g) {
        g.setColor(CURRENT_LINE_COLOR);
        g.fillRect(0, y(freditor.row()), getWidth(), fontHeight);
    }

    private void paintSelection(Graphics g, int startRow, int startColumn, int endRow,
                                int endColumn) {
        g.setColor(SELECTION_COLOR);
        if (startRow == endRow) {
            paintLineSelection(g, startRow, startColumn, endColumn);
        } else {
            paintMultiLineSelection(g, startRow, startColumn, endRow, endColumn);
        }
    }

    private void paintLineSelection(Graphics g, int row, int startColumn, int endColumn) {
        g.fillRect(x(startColumn), y(row), (endColumn - startColumn) * fontWidth, fontHeight);
    }

    private void paintMultiLineSelection(Graphics g, int startRow, int startColumn, int endRow, int endColumn) {
        paintLineSelection(g, startRow, startColumn, freditor.lengthOfRow(startRow));
        for (int row = startRow + 1; row < endRow; ++row) {
            paintLineSelection(g, row, 0, freditor.lengthOfRow(row));
        }
        paintLineSelection(g, endRow, 0, endColumn);
    }

    private void paintMatchingParensBackground(Graphics g) {
        IntConsumer paint = position -> paintParensBackground(g, position);

        int start = freditor.homePositionOfRow(firstVisibleLine());
        freditor.findOpeningParen(start, paint, doNothing);

        int end = freditor.homePositionOfRow(lastVisibleLine() + 2);
        freditor.findClosingParen(end, paint, doNothing);
    }

    private void paintParensBackground(Graphics g, int position) {
        g.setColor(MATCHING_PARENS_BACKGROUND_COLOR);
        g.fillRect(x(freditor.columnOfPosition(position)), y(freditor.rowOfPosition(position)), fontWidth, fontHeight);
    }

    private static final Runnable doNothing = () -> {
    };

    private void paintLexemes(Graphics g) {
        final int componentWidth = getWidth();
        final int componentHeight = getHeight();
        int x = -firstVisibleColumn * fontWidth;
        int y = 0;
        final int len = freditor.length();
        for (int i = freditor.homePositionOfRow(firstVisibleLine); i < len; ) {
            int k = freditor.endOfLexeme(i);
            int rgb = freditor.flexer.pickColorForLexeme(freditor.stateAt(i - 1), freditor.charAt(i), freditor.stateAt(k - 1));
            for (; i < k; ++i) {
                char c = freditor.charAt(i);
                if (c != '\n') {
                    if (x >= 0) {
                        Front.font.drawCharacter(g, x, y, c, rgb);
                    }
                    x += fontWidth;
                    if (x < componentWidth) continue;
                    i = freditor.endPositionOf(i);
                }
                y += fontHeight;
                if (y >= componentHeight) return;
                x = -firstVisibleColumn * fontWidth;
            }
        }
    }

    private void paintCursor(Graphics g) {
        int cursorX = x(freditor.column());
        int cursorY = y(freditor.row());
        g.setColor(Color.BLACK);
        g.drawLine(cursorX, cursorY, cursorX, cursorY + fontHeight - 1);
    }

    public int cursor() {
        return freditor.cursor();
    }

    public int row() {
        return freditor.row();
    }

    public int column() {
        return freditor.column();
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
        freditor.insert(s);
        componentToRepaint.repaint();
    }

    public void indent() {
        freditor.indent();
        componentToRepaint.repaint();
    }

    public void loadFromFile(String pathname) throws IOException {
        freditor.loadFromFile(pathname);
        adjustView();
    }

    public void loadFromString(String program) {
        freditor.loadFromString(program);
        adjustView();
    }

    public void saveToFile(String pathname) throws IOException {
        freditor.saveToFile(pathname);
    }
}
