package freditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class Autosaver {
    private static final Pattern APPLICATIONS = Pattern.compile("[a-z]{1,16}");
    private static final String EXTENSION = ".txt";

    private final Freditor freditor;

    public final String directory;
    public final String filename;
    public final String pathname;

    public Autosaver(Freditor freditor, String application) {
        if (!APPLICATIONS.matcher(application).matches()) {
            throw new IllegalArgumentException(application + " does not match " + APPLICATIONS);
        }

        this.freditor = freditor;

        directory = System.getProperty("user.home") + File.separator + application + File.separator;
        filename = "!" + application + EXTENSION;
        pathname = directory + filename;
    }

    public void loadOrDefault(String program) {
        try {
            freditor.loadFromFile(pathname);
        } catch (IOException ignored) {
            freditor.loadFromString(program);
        }
    }

    public void save() {
        createDirectory();
        saveAs(pathname);
        saveAs(contentHashPathname());
    }

    private void createDirectory() {
        File dir = new File(directory);
        if (dir.mkdir()) {
            System.out.println("created directory " + dir);
        }
    }

    private void saveAs(String pathname) {
        try {
            System.out.println("saving code as " + pathname);
            freditor.saveToFile(pathname);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String contentHashPathname() {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA");
            byte[] text = freditor.toString().getBytes(StandardCharsets.ISO_8859_1);
            byte[] hash = sha1.digest(text);
            StringBuilder builder = new StringBuilder(directory);
            for (byte x : hash) {
                builder.append("0123456789abcdef".charAt((x >>> 4) & 15));
                builder.append("0123456789abcdef".charAt(x & 15));
            }
            return builder.append(EXTENSION).toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new RuntimeException(impossible);
        }
    }
}
