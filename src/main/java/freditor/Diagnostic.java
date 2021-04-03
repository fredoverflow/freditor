package freditor;

import java.util.Arrays;

class Diagnostic {
    public final String[] lines;
    public final int row;
    public final int column;
    public final int width;

    Diagnostic(String message, int row, int column) {
        this.lines = message.split("\n");
        this.row = row;
        this.column = column;
        this.width = Arrays.stream(lines)
                .mapToInt(String::length)
                .max()
                .orElse(1);
    }
}
