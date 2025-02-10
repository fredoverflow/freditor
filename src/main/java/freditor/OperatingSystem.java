package freditor;

public class OperatingSystem {
    public static final boolean isMacintosh = System.getProperty("os.name").toLowerCase().contains("mac");

    // https://cheerpj.com/docs/guides/File-System-support#files-mount-point
    public static final boolean isBrowser = System.getProperty("user.home").equals("/files");
}
