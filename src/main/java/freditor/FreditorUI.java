package freditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static freditor.Maths.atLeastZero;

public class FreditorUI extends JComponent {
    public static final Color CURRENT_LINE_COLOR = new Color(0xffffaa);
    public static final Color SELECTION_COLOR = new Color(0xc8c8ff);
    public static final Color MATCHING_PARENS_BACKGROUND_COLOR = new Color(0xe0e0e0);

    public static final int ADDITIONAL_LINES = 1;
    public static final int ADDITIONAL_COLUMNS = 8;

    public static final int frontWidth = Front.front.width;
    public static final int frontHeight = Front.front.height;

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
        return getHeight() / frontHeight;
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
        return getWidth() / frontWidth;
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
        firstVisibleLine = atLeastZero(firstVisibleLine);

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
        firstVisibleColumn = atLeastZero(firstVisibleColumn);

        componentToRepaint.repaint();
    }

    private static final int CTRL_OR_META_OR_ALT = InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
    private static final int CTRL_RESPECTIVELY_META = OperatingSystem.isMacintosh ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

    public static boolean isControlRespectivelyCommandDown(InputEvent event) {
        return (event.getModifiersEx() & CTRL_RESPECTIVELY_META) != 0;
    }

    public void simulateEnter() {
        char previousCharTyped = charTyped;
        charTyped = 0;
        freditor.onEnter(previousCharTyped);
    }

    private char charTyped;

    public FreditorUI(Flexer flexer, Indenter indenter, int columns, int rows) {
        setPreferredSize(new Dimension(columns * frontWidth, rows * frontHeight));

        freditor = new Freditor(flexer, indenter);
        // We want to be able to listen to keys...
        setFocusable(true);
        // ...including the TAB key :)
        setFocusTraversalKeysEnabled(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char ch = event.getKeyChar();
                if (ch >= 32 && ch < 127 || ch >= 160 && ch < 256) {
                    if ((event.getModifiersEx() & CTRL_OR_META_OR_ALT) == 0) {
                        freditor.insertCharacter(ch);
                        charTyped = ch;
                    }
                }
                adjustView();
            }

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
                        if (isControlRespectivelyCommandDown(event)) {
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
                        } else if (isControlRespectivelyCommandDown(event)) {
                            freditor.moveCursorToPreviousLexeme();
                        } else {
                            freditor.moveCursorLeft();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (event.isAltDown()) {
                            freditor.moveCursorAfterNextClosingParen(event.isShiftDown());
                        } else if (isControlRespectivelyCommandDown(event)) {
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
                        } else if (isControlRespectivelyCommandDown(event)) {
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
                        } else if (isControlRespectivelyCommandDown(event)) {
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
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.moveCursorTop();
                        } else {
                            freditor.moveCursorStart();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_END:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.moveCursorBottom();
                        } else {
                            freditor.moveCursorEnd();
                        }
                        if (!event.isShiftDown()) freditor.adjustOrigin();
                        break;

                    case KeyEvent.VK_D:
                        if (isControlRespectivelyCommandDown(event) && !event.isShiftDown()) {
                            freditor.deleteCurrentLine();
                        }
                        break;

                    case KeyEvent.VK_A:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.moveCursorTop();
                            freditor.adjustOrigin();
                            freditor.moveCursorBottom();
                        }
                        break;

                    case KeyEvent.VK_C:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.copy();
                        }
                        break;

                    case KeyEvent.VK_X:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.cut();
                        }
                        break;

                    case KeyEvent.VK_V:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.paste();
                        }
                        break;

                    case KeyEvent.VK_Z:
                        if (isControlRespectivelyCommandDown(event)) {
                            freditor.undo();
                        }
                        break;

                    case KeyEvent.VK_Y:
                        if (isControlRespectivelyCommandDown(event)) {
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
                        int row = event.getY() / frontHeight + firstVisibleLine;
                        int column = event.getX() / frontWidth + firstVisibleColumn;
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
                int row = event.getY() / frontHeight + firstVisibleLine;
                int column = event.getX() / frontWidth + firstVisibleColumn;
                freditor.setRowAndColumn(atLeastZero(row), atLeastZero(column));
                componentToRepaint.repaint();
                requestFocusInWindow();
            }
        });

        addMouseWheelListener(event -> {
            int direction = event.getWheelRotation();
            int amount = isControlRespectivelyCommandDown(event) ? visibleLines() / 2 : 3;
            firstVisibleLine = atLeastZero(firstVisibleLine + direction * amount);
            componentToRepaint.repaint();
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (event.getOppositeComponent() instanceof JButton) {
                    // Error dialogs steal the focus from the editor,
                    // but the cursor should remain visible because
                    // it marks the position associated with the error.
                    return;
                }
                repaint();
            }
        });
    }

    public String lexemeAtCursor() {
        return freditor.lexemeAtCursor();
    }

    public String symbolNearCursor(FlexerState symbolTail) {
        return freditor.symbolNearCursor(symbolTail);
    }

    @Override
    public void paint(Graphics g) {
        paintBackground(g);
        paintCurrentLineOrSelection(g);
        paintMatchingParensBackground(g);
        paintLexemes(g);
        if (hasFocus()) {
            paintCursor(g);
        }
    }

    private int x(int column) {
        return (column - firstVisibleColumn) * frontWidth;
    }

    private int y(int row) {
        return (row - firstVisibleLine) * frontHeight;
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
        g.fillRect(0, y(freditor.row()), getWidth(), frontHeight);
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
        g.fillRect(x(startColumn), y(row), (endColumn - startColumn) * frontWidth, frontHeight);
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
        g.fillRect(x(freditor.columnOfPosition(position)), y(freditor.rowOfPosition(position)), frontWidth, frontHeight);
    }

    private static final Runnable doNothing = () -> {
    };

    private void paintLexemes(Graphics g) {
        final int componentWidth = getWidth();
        final int componentHeight = getHeight();
        int x = -firstVisibleColumn * frontWidth;
        int y = 0;
        final int len = freditor.length();
        for (int i = freditor.homePositionOfRow(firstVisibleLine); i < len; ) {
            int k = freditor.endOfLexeme(i);
            int rgb = freditor.flexer.pickColorForLexeme(freditor.stateAt(i - 1), freditor.stateAt(k - 1));
            for (; i < k; ++i) {
                char c = freditor.charAt(i);
                if (c != '\n') {
                    if (x >= 0) {
                        Front.front.drawCharacter(g, x, y, c, rgb);
                    }
                    x += frontWidth;
                    if (x < componentWidth) continue;
                    i = freditor.endPositionOf(i);
                }
                y += frontHeight;
                if (y >= componentHeight) return;
                x = -firstVisibleColumn * frontWidth;
            }
        }
    }

    private void paintCursor(Graphics g) {
        int cursorX = x(freditor.column());
        int cursorY = y(freditor.row());
        g.setColor(Color.BLACK);
        Front.front.drawCharacter(g, cursorX, cursorY, '\177', 0x000000);
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

    public void setCursorTo(String regex, int group) {
        freditor.setCursorTo(regex, group);
        adjustView();
    }

    public String getText() {
        return freditor.toString();
    }

    public String getLineBeforeSelection() {
        return freditor.getLineBeforeSelection();
    }

    public String getTextBeforeSelection() {
        return freditor.getTextBeforeSelection();
    }

    public void insert(CharSequence s) {
        freditor.insert(s);
        componentToRepaint.repaint();
    }

    public void append(CharSequence s) {
        freditor.insertAt(freditor.length(), s);
        componentToRepaint.repaint();
    }

    public void uncommit() {
        freditor.uncommit();
    }

    public void replace(String regex, String replacement) {
        freditor.replace(regex, replacement);
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

    public Autosaver newAutosaver(String application) {
        return new Autosaver(freditor, application);
    }
}
