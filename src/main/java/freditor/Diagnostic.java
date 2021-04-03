package freditor;

class Diagnostic {
    public final String message;
    public final int row;
    public final int column;

    Diagnostic(String message, int row, int column) {
        this.message = message;
        this.row = row;
        this.column = column;
    }
}
