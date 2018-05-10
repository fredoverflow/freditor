package freditor;

public abstract class Indenter {
    public abstract int[] corrections(CharZipper text);

    public String synthesizeOnEnterAfter(char previousCharTyped) {
        return "";
    }
}
